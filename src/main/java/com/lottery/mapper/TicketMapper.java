package com.lottery.mapper;

import com.lottery.pojo.entity.Ticket;
import com.lottery.pojo.vo.TicketHistoryVO;
import com.lottery.pojo.vo.WinnerVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface TicketMapper {

    int insert(Ticket ticket);

    int batchInsertTickets(@Param("list") List<Ticket> tickets);

    Ticket selectById(Long id);

    List<Ticket> selectByDrawNo(String drawNo);

    int updateStatus(@Param("id") Long id, @Param("status") Integer status);

    int countByDrawNo(String drawNo);

    int countGrandPrizeByDrawNo(String drawNo);

    int countFirstPrizeByDrawNo(String drawNo);

    int countNoPrizeByDrawNo(String drawNo);

    int countUsersByDrawNo(String drawNo);

    List<TicketHistoryVO> selectHistoryByUserId(@Param("userId") Integer userId,
                                                @Param("offset") int offset,
                                                @Param("limit") int limit);

    int countByUserId(Integer userId);

    List<WinnerVO> selectWinnersByDrawNo(@Param("drawNo") String drawNo);

    int deleteAll();
}
