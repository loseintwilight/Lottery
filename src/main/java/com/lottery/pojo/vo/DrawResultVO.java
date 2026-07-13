package com.lottery.pojo.vo;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class DrawResultVO {
    private String drawNo;
    private List<Integer> numbers;
    private LocalDateTime drawTime;
    private int totalBets;
    private double totalAmount;
    private StatsVO statistics;
    private List<WinnerVO> grandPrizeWinners;
    private List<WinnerVO> firstPrizeWinners;
}