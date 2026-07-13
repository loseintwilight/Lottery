package com.lottery.common;

import com.lottery.mapper.*;
import com.lottery.pojo.entity.Ticket;
import com.lottery.pojo.entity.User;
import com.lottery.pojo.vo.AutoTestResultVO;
import com.lottery.pojo.vo.TestProgressVO;
import com.lottery.pojo.vo.WinnerVO;
import com.lottery.service.DrawService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

@Service
public class AutoTestService {

    private static final Logger log = LoggerFactory.getLogger(AutoTestService.class);
    private static final int TOTAL_USERS = 100000;
    private static final int REG_BATCH = 5000;        // 注册批次：5000条/INSERT
    private static final int TICKET_BATCH = 5000;      // 购彩批次：5000条/INSERT
    private static final int THREAD_COUNT = 20;
    private static final int TICKET_PROGRESS_INTERVAL = 10000; // 每1万张更新一次进度

    private final UserMapper userMapper;
    private final TicketMapper ticketMapper;
    private final LotteryDrawMapper drawMapper;
    private final NotificationMapper notificationMapper;
    private final DrawService drawService;

    /** 当前测试进度，供前端轮询 */
    private volatile TestProgressVO currentProgress = new TestProgressVO();

    public AutoTestService(UserMapper userMapper,
                           TicketMapper ticketMapper,
                           LotteryDrawMapper drawMapper,
                           NotificationMapper notificationMapper,
                           DrawService drawService) {
        this.userMapper = userMapper;
        this.ticketMapper = ticketMapper;
        this.drawMapper = drawMapper;
        this.notificationMapper = notificationMapper;
        this.drawService = drawService;
    }

    // ========== 进度查询 ==========

    public TestProgressVO getProgress() {
        return currentProgress;
    }

    // ========== 自动测试主入口 ==========

    public AutoTestResultVO runAutoTest() {
        long start = System.currentTimeMillis();
        log.info("=== 自动测试开始 ===");
        AutoTestResultVO result = new AutoTestResultVO();

        try {
            // ---- 1. 注册用户 ----
            updateProgress("registering", 0, TOTAL_USERS);
            log.info("开始注册 {} 个用户...", TOTAL_USERS);
            List<Integer> userIds = batchRegisterUsers();
            long regEnd = System.currentTimeMillis();
            log.info("用户注册完成，共 {} 个，耗时: {}ms", userIds.size(), regEnd - start);

            // ---- 2. 购彩 ----
            String drawNo = drawService.getCurrentDrawNo();
            log.info("当前期号: {}", drawNo);

            updateProgress("buying", 0, TOTAL_USERS);
            log.info("开始并发购彩...");
            long buyStart = System.currentTimeMillis();
            int totalTickets = batchBuyTickets(userIds, drawNo);
            long buyEnd = System.currentTimeMillis();
            log.info("购彩完成，共 {} 张，耗时: {}ms", totalTickets, buyEnd - buyStart);

            // ---- 3. 开奖 ----
            updateProgress("drawing", 0, 1);
            log.info("开始开奖...");
            var drawResult = drawService.draw(drawNo);
            List<Integer> winningNumbers = drawResult.getNumbers();
            log.info("开奖完成！中奖号码: {}", winningNumbers);

            // ---- 4. 查询中奖信息 ----
            updateProgress("done", 1, 1);
            List<WinnerVO> allWinners = ticketMapper.selectWinnersByDrawNo(drawNo);
            List<WinnerVO> grandPrize = new ArrayList<>();
            List<WinnerVO> firstPrize = new ArrayList<>();
            for (WinnerVO w : allWinners) {
                if ("特等奖".equals(w.getPrizeLevel())) grandPrize.add(w);
                else firstPrize.add(w);
            }

            long totalTime = System.currentTimeMillis() - start;

            // ---- 5. 组装结果 ----
            result.setDrawNo(drawNo);
            result.setWinningNumbers(winningNumbers);
            result.setTotalUsers(userIds.size());
            result.setTotalTickets(totalTickets);
            result.setGrandPrizeWinners(grandPrize);
            result.setFirstPrizeWinners(firstPrize);
            result.setDuration(totalTime);
            result.setSummary(String.format(
                    "自动测试完成！注册用户: %d, 购彩: %d张, 期号: %s, 特等奖: %d人, 一等奖: %d人, 耗时: %.1fs",
                    userIds.size(), totalTickets, drawNo, grandPrize.size(), firstPrize.size(), totalTime / 1000.0));

            log.info("=== {} ===", result.getSummary());
            return result;

        } catch (Exception e) {
            log.error("自动测试失败", e);
            updateProgress("error", 0, 0);
            throw new RuntimeException("auto test failed: " + e.getMessage());
        }
    }

    // ========== 批量注册（1000条/INSERT） ==========

    private List<Integer> batchRegisterUsers() {
        // 获取已存在的最大用户ID，从下一个开始递增
        Integer maxId = userMapper.selectMaxId();
        int startIdx = (maxId == null ? 0 : maxId) + 1;
        log.info("用户ID起始值: {}", startIdx);

        List<Integer> userIds = new ArrayList<>(TOTAL_USERS);
        List<User> batch = new ArrayList<>(REG_BATCH);

        for (int i = startIdx; i < startIdx + TOTAL_USERS; i++) {
            User user = new User();
            user.setUsername("T" + i);
            user.setPassword(PasswordUtil.encrypt("123"));
            user.setPhone("1380000" + String.format("%04d", i - 1));
            user.setBalance(BigDecimal.valueOf(1000));
            batch.add(user);

            if (batch.size() == REG_BATCH) {
                userMapper.batchInsertUsers(batch);
                for (User u : batch) {
                    userIds.add(u.getId());
                }
                batch.clear();
                updateProgress("registering", userIds.size(), TOTAL_USERS);
            }
        }
        // 剩余
        if (!batch.isEmpty()) {
            userMapper.batchInsertUsers(batch);
            for (User u : batch) userIds.add(u.getId());
        }

        return userIds;
    }

    // ========== 并发批量购彩 ==========

    private int batchBuyTickets(List<Integer> userIds, String drawNo) throws InterruptedException {
        ThreadPoolExecutor executor = new ThreadPoolExecutor(
                THREAD_COUNT, THREAD_COUNT,
                60L, TimeUnit.SECONDS,
                new LinkedBlockingQueue<>(1024),
                new ThreadFactory() {
                    private final AtomicInteger threadNum = new AtomicInteger(1);
                    @Override
                    public Thread newThread(Runnable r) {
                        Thread t = new Thread(r, "lottery-buy-thread-" + threadNum.getAndIncrement());
                        t.setDaemon(true);
                        return t;
                    }
                },
                new ThreadPoolExecutor.AbortPolicy()
        );
        AtomicInteger ticketCount = new AtomicInteger(0);
        int usersPerThread = TOTAL_USERS / THREAD_COUNT;
        CountDownLatch latch = new CountDownLatch(THREAD_COUNT);

        for (int t = 0; t < THREAD_COUNT; t++) {
            final int threadIdx = t;
            final int startIdx = t * usersPerThread;
            final int endIdx = (t == THREAD_COUNT - 1) ? userIds.size() : (t + 1) * usersPerThread;

            executor.submit(() -> {
                try {
                    Random rand = new Random();
                    List<Ticket> ticketBatch = new ArrayList<>(TICKET_BATCH);

                    for (int i = startIdx; i < endIdx; i++) {
                        List<Integer> numbers = generateRandomNumbers(rand);
                        String numbersStr = numbers.stream()
                                .map(String::valueOf)
                                .collect(Collectors.joining(","));

                        int betCount = rand.nextInt(5) + 1;
                        BigDecimal amount = BigDecimal.valueOf(betCount * 2L);

                        Ticket ticket = new Ticket();
                        ticket.setUserId(userIds.get(i));
                        ticket.setDrawNo(drawNo);
                        ticket.setNumbers(numbersStr);
                        ticket.setBetCount(betCount);
                        ticket.setAmount(amount);
                        ticket.setStatus(0);
                        ticket.setBuyTime(LocalDateTime.now());

                        ticketBatch.add(ticket);

                        // 攒够批次，批量写入
                        if (ticketBatch.size() >= TICKET_BATCH) {
                            ticketMapper.batchInsertTickets(ticketBatch);
                            ticketCount.addAndGet(ticketBatch.size());
                            ticketBatch.clear();

                            // 每1万张更新进度
                            if (ticketCount.get() % TICKET_PROGRESS_INTERVAL == 0) {
                                int current = Math.min(ticketCount.get(), TOTAL_USERS);
                                updateProgress("buying", current, TOTAL_USERS);
                                log.info("已购买 {} 张彩票 (线程{})", ticketCount.get(), threadIdx);
                            }
                        }
                    }

                    // 写入剩余彩票
                    if (!ticketBatch.isEmpty()) {
                        ticketMapper.batchInsertTickets(ticketBatch);
                        ticketCount.addAndGet(ticketBatch.size());
                    }

                } catch (Exception e) {
                    log.error("线程{}购票失败", threadIdx, e);
                } finally {
                    latch.countDown();
                }
            });
        }

        latch.await();
        executor.shutdown();
        return ticketCount.get();
    }

    // ========== 工具方法 ==========

    private List<Integer> generateRandomNumbers(Random rand) {
        Set<Integer> set = new HashSet<>();
        while (set.size() < 7) {
            set.add(rand.nextInt(36) + 1);
        }
        List<Integer> list = new ArrayList<>(set);
        Collections.sort(list);
        return list;
    }

    private void updateProgress(String phase, int current, int total) {
        TestProgressVO p = new TestProgressVO();
        p.setPhase(phase);
        p.setCurrent(current);
        p.setTotal(total);
        p.setFinished("done".equals(phase));
        p.setError("error".equals(phase));
        this.currentProgress = p;
    }

    public String clearAllData() {
        long start = System.currentTimeMillis();
        log.info("=== 开始清空数据 ===");
        try {
            notificationMapper.deleteAll();
            ticketMapper.deleteAll();
            drawMapper.deleteAll();
            userMapper.deleteAll();
            String msg = String.format("数据已清空，耗时: %dms", System.currentTimeMillis() - start);
            log.info("=== {} ===", msg);
            return msg;
        } catch (Exception e) {
            log.error("清空数据失败", e);
            throw new RuntimeException("clear data failed: " + e.getMessage());
        }
    }
}