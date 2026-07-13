package com.lottery.controller;

import com.lottery.common.ResultCode;
import com.lottery.pojo.vo.ExportResultVO;
import com.lottery.pojo.vo.R;
import com.lottery.pojo.vo.StatsVO;
import com.lottery.service.StatisticsService;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@RestController
@RequestMapping("/api")
public class StatsController {

    private final StatisticsService statisticsService;

    public StatsController(StatisticsService statisticsService) {
        this.statisticsService = statisticsService;
    }

    @GetMapping("/stats")
    public R<StatsVO> getStats(@RequestParam String drawNo) {
        try {
            StatsVO vo = statisticsService.getStats(drawNo);
            return R.success(vo);
        } catch (RuntimeException e) {
            return R.error(ResultCode.ERROR, e.getMessage());
        }
    }

    @GetMapping("/stats/export")
    public R<ExportResultVO> exportExcel(@RequestParam String drawNo) {
        try {
            String filePath = statisticsService.exportExcel(drawNo);
            return R.success(new ExportResultVO(filePath));
        } catch (RuntimeException e) {
            return R.error(ResultCode.ERROR, e.getMessage());
        }
    }

    @GetMapping("/stats/export/download")
    public void downloadExcel(@RequestParam String drawNo, HttpServletResponse response) {
        try {
            String fileName = "lottery_stats_" + drawNo + ".xlsx";
            response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
            response.setHeader("Content-Disposition", "attachment;filename="
                    + URLEncoder.encode(fileName, StandardCharsets.UTF_8).replace("+", "%20"));
            statisticsService.exportCsvToStream(drawNo, response.getOutputStream());
        } catch (IOException e) {
            throw new RuntimeException("export failed: " + e.getMessage());
        }
    }
}