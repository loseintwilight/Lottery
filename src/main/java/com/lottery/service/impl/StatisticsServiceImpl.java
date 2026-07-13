package com.lottery.service.impl;

import com.lottery.common.ExcelExporter;
import com.lottery.mapper.LotteryDrawMapper;
import com.lottery.mapper.NotificationMapper;
import com.lottery.mapper.TicketMapper;
import com.lottery.pojo.entity.LotteryDraw;
import com.lottery.pojo.vo.StatsVO;
import com.lottery.service.StatisticsService;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.OutputStream;

@Service
public class StatisticsServiceImpl implements StatisticsService {

    private final TicketMapper ticketMapper;
    private final NotificationMapper notificationMapper;
    private final LotteryDrawMapper drawMapper;
    private final ExcelExporter excelExporter;

    public StatisticsServiceImpl(TicketMapper ticketMapper,
                                 NotificationMapper notificationMapper,
                                 LotteryDrawMapper drawMapper,
                                 ExcelExporter excelExporter) {
        this.ticketMapper = ticketMapper;
        this.notificationMapper = notificationMapper;
        this.drawMapper = drawMapper;
        this.excelExporter = excelExporter;
    }

    @Override
    public StatsVO getStats(String drawNo) {
        LotteryDraw draw = drawMapper.selectByDrawNo(drawNo);
        if (draw == null) {
            throw new RuntimeException("draw not found");
        }
        StatsVO vo = new StatsVO();
        vo.setDrawNo(drawNo);
        vo.setTotalBets(draw.getTotalBets());
        vo.setTotalAmount(draw.getTotalAmount().doubleValue());
        vo.setGrandPrizeCount(ticketMapper.countGrandPrizeByDrawNo(drawNo));
        vo.setFirstPrizeCount(ticketMapper.countFirstPrizeByDrawNo(drawNo));
        vo.setNoPrizeCount(ticketMapper.countNoPrizeByDrawNo(drawNo));
        return vo;
    }

    @Override
    public String exportExcel(String drawNo) {
        try {
            return excelExporter.exportStats(drawNo);
        } catch (IOException e) {
            throw new RuntimeException("export failed: " + e.getMessage());
        }
    }

    @Override
    public void exportExcelToStream(String drawNo, OutputStream os) {
        excelExporter.exportStatsToStream(drawNo, os);
    }

    @Override
    public void exportCsvToStream(String drawNo, OutputStream os) {
        excelExporter.exportCsvToStream(drawNo, os);
    }
}