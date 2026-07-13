package com.lottery.service;

import com.lottery.pojo.vo.PageVO;
import com.lottery.pojo.vo.TicketHistoryVO;
import com.lottery.pojo.vo.TicketVO;

import java.util.List;

public interface TicketService {
    TicketVO buyTicket(Integer userId, String drawNo, List<Integer> numbers, Integer betCount);

    PageVO<TicketHistoryVO> getHistory(Integer userId, int page, int size);
}