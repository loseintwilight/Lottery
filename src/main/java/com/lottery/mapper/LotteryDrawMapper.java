package com.lottery.mapper;

import com.lottery.pojo.entity.LotteryDraw;
import org.apache.ibatis.annotations.Mapper;

import java.math.BigDecimal;

@Mapper
public interface LotteryDrawMapper {

    int insert(LotteryDraw draw);

    LotteryDraw selectByDrawNo(String drawNo);

    LotteryDraw selectLatest();

    LotteryDraw selectLatestCompleted();

    int updateByDrawNo(LotteryDraw draw);

    int deleteAll();
}
