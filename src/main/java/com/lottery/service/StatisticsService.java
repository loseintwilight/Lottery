package com.lottery.service;

import com.lottery.pojo.vo.StatsVO;

import java.io.OutputStream;

public interface StatisticsService {
    StatsVO getStats(String drawNo);

    String exportExcel(String drawNo);

    void exportExcelToStream(String drawNo, OutputStream os);

    void exportCsvToStream(String drawNo, OutputStream os);
}