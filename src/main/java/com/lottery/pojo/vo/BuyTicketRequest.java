package com.lottery.pojo.vo;

import lombok.Data;

import java.util.List;

@Data
public class BuyTicketRequest {
    private Integer userId;
    private String drawNo;
    private List<Integer> numbers;
    private Integer betCount;
}