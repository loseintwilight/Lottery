package com.lottery.common;

import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.write.style.column.LongestMatchColumnWidthStyleStrategy;
import com.lottery.mapper.TicketMapper;
import com.lottery.mapper.UserMapper;
import com.lottery.pojo.entity.Ticket;
import com.lottery.pojo.entity.User;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Component
public class ExcelExporter {

    private final UserMapper userMapper;
    private final TicketMapper ticketMapper;

    public ExcelExporter(UserMapper userMapper, TicketMapper ticketMapper) {
        this.userMapper = userMapper;
        this.ticketMapper = ticketMapper;
    }

    /**
     * 导出 xlsx 到文件（供旧接口调用）
     */
    public String exportStats(String drawNo) throws IOException {
        Path exportDir = Paths.get("exports");
        if (!Files.exists(exportDir)) {
            Files.createDirectories(exportDir);
        }
        String fileName = "lottery_stats_" + drawNo + "_"
                + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"))
                + ".xlsx";
        Path filePath = exportDir.resolve(fileName);
        try (OutputStream os = Files.newOutputStream(filePath)) {
            writeXlsx(drawNo, os);
        }
        return filePath.toAbsolutePath().toString();
    }

    /**
     * 导出 xlsx 到输出流（浏览器下载用）
     */
    public void exportStatsToStream(String drawNo, OutputStream os) {
        writeXlsx(drawNo, os);
    }

    /**
     * 导出 xlsx 到输出流（别名）
     */
    public void exportCsvToStream(String drawNo, OutputStream os) {
        writeXlsx(drawNo, os);
    }

    private void writeXlsx(String drawNo, OutputStream os) {
        List<List<String>> rows = buildDataList(drawNo);
        EasyExcel.write(os)
                .sheet("中奖统计")
                .registerWriteHandler(new LongestMatchColumnWidthStyleStrategy())
                .doWrite(rows);
    }

    private List<List<String>> buildDataList(String drawNo) {
        List<Ticket> tickets = ticketMapper.selectByDrawNo(drawNo);
        List<List<String>> rows = new ArrayList<>(tickets.size() + 10);

        // ---- 汇总信息 ----
        int totalUsers = ticketMapper.countUsersByDrawNo(drawNo);
        int grandPrize = ticketMapper.countGrandPrizeByDrawNo(drawNo);
        int firstPrize = ticketMapper.countFirstPrizeByDrawNo(drawNo);
        int noPrize = ticketMapper.countNoPrizeByDrawNo(drawNo);

        rows.add(Arrays.asList("期号", drawNo, "", "", "", "", ""));
        rows.add(Arrays.asList("总用户数", String.valueOf(totalUsers), "", "", "", "", ""));
        rows.add(Arrays.asList("总彩票数", String.valueOf(tickets.size()), "", "", "", "", ""));
        rows.add(Arrays.asList("特等奖", String.valueOf(grandPrize), "", "", "", "", ""));
        rows.add(Arrays.asList("一等奖", String.valueOf(firstPrize), "", "", "", "", ""));
        rows.add(Arrays.asList("未中奖", String.valueOf(noPrize), "", "", "", "", ""));
        rows.add(Collections.nCopies(7, "")); // 空行分隔

        // ---- 批量加载用户信息 ----
        Set<Integer> userIds = tickets.stream()
                .map(Ticket::getUserId)
                .collect(Collectors.toSet());
        List<User> users = userMapper.selectByIds(new ArrayList<>(userIds));
        Map<Integer, String> userMap = new HashMap<>();
        for (User u : users) {
            userMap.put(u.getId(), u.getUsername());
        }

        // ---- 表头 ----
        rows.add(Arrays.asList("用户ID", "用户名", "所选号码", "倍数", "金额", "状态", "购买时间"));

        // ---- 数据行 ----
        for (Ticket t : tickets) {
            String username = userMap.getOrDefault(t.getUserId(), "N/A");
            String statusDesc = switch (t.getStatus()) {
                case 0 -> "未开奖";
                case 1 -> "未中奖";
                case 2 -> "特等奖";
                case 3 -> "一等奖";
                default -> "未知";
            };

            rows.add(Arrays.asList(
                    String.valueOf(t.getUserId()),
                    username,
                    t.getNumbers(),
                    String.valueOf(t.getBetCount()),
                    t.getAmount().toString(),
                    statusDesc,
                    t.getBuyTime().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))
            ));
        }

        return rows;
    }
}