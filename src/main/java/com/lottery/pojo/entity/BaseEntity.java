package com.lottery.pojo.entity;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class BaseEntity {
    private String createBy;
    private String updateBy;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}