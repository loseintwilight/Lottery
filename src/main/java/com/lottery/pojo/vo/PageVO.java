package com.lottery.pojo.vo;

import lombok.Data;

import java.util.List;

@Data
public class PageVO<T> {
    private List<T> list;
    private int total;
    private int page;
    private int size;
    private int pages;
}