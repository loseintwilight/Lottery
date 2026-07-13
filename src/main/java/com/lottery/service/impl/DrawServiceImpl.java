package com.lottery.service.impl;

import com.lottery.mapper.LotteryDrawMapper;
import com.lottery.mapper.NotificationMapper;
import com.lottery.mapper.TicketMapper;
import com.lottery.pojo.entity.LotteryDraw;
import com.lottery.pojo.entity.Notification;
import com.lottery.pojo.entity.Ticket;
import com.lottery.pojo.vo.DrawResultVO;
import com.lottery.pojo.vo.NotificationVO;
import com.lottery.pojo.vo.WinnerVO;
import com.lottery.service.DrawService;
import com.lottery.service.StatisticsService;
import com.lottery.websocket.LotteryWebSocketHandler;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class DrawServiceImpl implements DrawService {

    private final LotteryDrawMapper drawMapper;
    private final TicketMapper ticketMapper;
    private final NotificationMapper notificationMapper;
    private final StatisticsService statisticsService;
    private final LotteryWebSocketHandler webSocketHandler;

    public DrawServiceImpl(LotteryDrawMapper drawMapper,
                           TicketMapper ticketMapper,
                           NotificationMapper notificationMapper,
                           StatisticsService statisticsService,
                           LotteryWebSocketHandler webSocketHandler) {
        this.drawMapper = drawMapper;
        this.ticketMapper = ticketMapper;
        this.notificationMapper = notificationMapper;
        this.statisticsService = statisticsService;
        this.webSocketHandler = webSocketHandler;
    }

    @Override
    @Transactional
    public DrawResultVO draw(String drawNo) {
        LotteryDraw existing = drawMapper.selectByDrawNo(drawNo);
        if (existing != null && existing.getNumbers() != null) {
            throw new RuntimeException("draw already exists for this period");
        }

        // 生成 7 个不重复的中奖号码（1~36）
        List<Integer> winningNumbers = generateNumbers();

        // 查出该期所有 ticket
        List<Ticket> tickets = ticketMapper.selectByDrawNo(drawNo);
        int totalBets = tickets.stream().mapToInt(Ticket::getBetCount).sum();
        BigDecimal totalAmount = tickets.stream()
                .map(Ticket::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // 逐一匹配中奖
        List<Notification> notifications = new ArrayList<>();

        for (Ticket ticket : tickets) {
            int matchCount = countMatch(ticket.getNumbers(), winningNumbers);
            int status;
            String prizeLevel = null;
            BigDecimal prizeAmount = BigDecimal.ZERO;

            if (matchCount == 7) {
                status = 2; // 特等奖
                prizeLevel = "特等奖";
                prizeAmount = BigDecimal.valueOf(5000000);
            } else if (matchCount == 6) {
                status = 3; // 一等奖
                prizeLevel = "一等奖";
                prizeAmount = BigDecimal.valueOf(100000);
            } else {
                status = 1; // 未中奖
            }

            ticketMapper.updateStatus(ticket.getId(), status);

            if (prizeLevel != null) {
                Notification n = new Notification();
                n.setUserId(ticket.getUserId());
                n.setDrawNo(drawNo);
                n.setTicketId(ticket.getId());
                n.setPrizeLevel(prizeLevel);
                n.setPrizeAmount(prizeAmount);
                notifications.add(n);
            }
        }

        // 批量写入通知
        if (!notifications.isEmpty()) {
            notificationMapper.batchInsert(notifications);
        }

        // 保存/更新开奖记录
        String numbersStr = winningNumbers.stream()
                .map(String::valueOf)
                .collect(Collectors.joining(","));

        if (existing == null) {
            LotteryDraw draw = new LotteryDraw();
            draw.setDrawNo(drawNo);
            draw.setNumbers(numbersStr);
            draw.setDrawTime(LocalDateTime.now());
            draw.setTotalBets(totalBets);
            draw.setTotalAmount(totalAmount);
            drawMapper.insert(draw);
        } else {
            existing.setNumbers(numbersStr);
            existing.setDrawTime(LocalDateTime.now());
            existing.setTotalBets(totalBets);
            existing.setTotalAmount(totalAmount);
            drawMapper.updateByDrawNo(existing);
        }

        // 查询通知VO并推送给在线用户
        if (!notifications.isEmpty()) {
            for (Notification n : notifications) {
                List<NotificationVO> unread = notificationMapper.selectUnreadByUserId(n.getUserId());
                if (!unread.isEmpty()) {
                    webSocketHandler.pushNotification(n.getUserId(), unread);
                }
            }
        }

        // 查询中奖者信息
        List<WinnerVO> allWinners = ticketMapper.selectWinnersByDrawNo(drawNo);
        List<WinnerVO> grandPrize = new ArrayList<>();
        List<WinnerVO> firstPrize = new ArrayList<>();
        for (WinnerVO w : allWinners) {
            if ("特等奖".equals(w.getPrizeLevel())) grandPrize.add(w);
            else firstPrize.add(w);
        }

        // 组装结果
        DrawResultVO result = new DrawResultVO();
        result.setDrawNo(drawNo);
        result.setNumbers(winningNumbers);
        result.setDrawTime(LocalDateTime.now());
        result.setTotalBets(totalBets);
        result.setTotalAmount(totalAmount.doubleValue());
        result.setStatistics(statisticsService.getStats(drawNo));
        result.setGrandPrizeWinners(grandPrize);
        result.setFirstPrizeWinners(firstPrize);

        // 广播开奖结果给所有在线用户
        Map<String, Object> drawResultMap = new HashMap<>();
        drawResultMap.put("drawNo", result.getDrawNo());
        drawResultMap.put("numbers", result.getNumbers());
        drawResultMap.put("drawTime", result.getDrawTime().toString());
        webSocketHandler.broadcastDrawResult(drawResultMap);

        return result;
    }

    @Override
    public String getCurrentDrawNo() {
        LotteryDraw latest = drawMapper.selectLatest();
        if (latest == null) {
            return LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd")) + "001";
        }
        String lastNo = latest.getDrawNo();
        String today = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        if (lastNo.startsWith(today)) {
            int seq = Integer.parseInt(lastNo.substring(8)) + 1;
            return today + String.format("%03d", seq);
        } else {
            return today + "001";
        }
    }

    @Override
    public DrawResultVO getDrawResult(String drawNo) {
        LotteryDraw draw = drawMapper.selectByDrawNo(drawNo);
        if (draw == null || draw.getNumbers() == null) {
            throw new RuntimeException("draw not found");
        }

        List<Integer> numbers = Arrays.stream(draw.getNumbers().split(","))
                .map(Integer::parseInt)
                .collect(Collectors.toList());

        List<WinnerVO> allWinners = ticketMapper.selectWinnersByDrawNo(drawNo);
        List<WinnerVO> grandPrize = new ArrayList<>();
        List<WinnerVO> firstPrize = new ArrayList<>();
        for (WinnerVO w : allWinners) {
            if ("特等奖".equals(w.getPrizeLevel())) grandPrize.add(w);
            else firstPrize.add(w);
        }

        DrawResultVO result = new DrawResultVO();
        result.setDrawNo(draw.getDrawNo());
        result.setNumbers(numbers);
        result.setDrawTime(draw.getDrawTime());
        result.setTotalBets(draw.getTotalBets());
        result.setTotalAmount(draw.getTotalAmount().doubleValue());
        result.setStatistics(statisticsService.getStats(drawNo));
        result.setGrandPrizeWinners(grandPrize);
        result.setFirstPrizeWinners(firstPrize);
        return result;
    }

    private List<Integer> generateNumbers() {
        List<Integer> all = new ArrayList<>();
        for (int i = 1; i <= 36; i++) all.add(i);
        Collections.shuffle(all);
        List<Integer> result = all.subList(0, 7);
        Collections.sort(result);
        return result;
    }

    private int countMatch(String ticketNumbers, List<Integer> winningNumbers) {
        Set<Integer> winSet = new HashSet<>(winningNumbers);
        int count = 0;
        String[] nums = ticketNumbers.split(",");
        for (String num : nums) {
            if (winSet.contains(Integer.parseInt(num.trim()))) {
                count++;
            }
        }
        return count;
    }

    @Override
    public DrawResultVO getLatestCompletedDraw() {
        LotteryDraw draw = drawMapper.selectLatestCompleted();
        if (draw == null || draw.getNumbers() == null) {
            return null;
        }
        return getDrawResult(draw.getDrawNo());
    }

    @Override
    public List<Integer> generateRollingNumbers() {
        return generateNumbers();
    }
}