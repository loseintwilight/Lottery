package com.lottery.pojo.vo;

import lombok.Data;

@Data
public class StatsVO {
    private String drawNo;
    private int totalBets;
    private double totalAmount;
    private int grandPrizeCount;
    private int firstPrizeCount;
    private int noPrizeCount;
}