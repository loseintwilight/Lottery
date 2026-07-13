package com.lottery.service;

import com.lottery.pojo.vo.DrawResultVO;

import java.util.List;

public interface DrawService {
    DrawResultVO draw(String drawNo);

    String getCurrentDrawNo();

    DrawResultVO getDrawResult(String drawNo);

    DrawResultVO getLatestCompletedDraw();

    List<Integer> generateRollingNumbers();
}