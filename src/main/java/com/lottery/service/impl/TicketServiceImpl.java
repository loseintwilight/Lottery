package com.lottery.service.impl;

import com.lottery.mapper.TicketMapper;
import com.lottery.mapper.UserMapper;
import com.lottery.pojo.entity.Ticket;
import com.lottery.pojo.entity.User;
import com.lottery.pojo.vo.TicketHistoryVO;
import com.lottery.pojo.vo.PageVO;
import com.lottery.pojo.vo.TicketVO;
import com.lottery.service.TicketService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class TicketServiceImpl implements TicketService {

    private final TicketMapper ticketMapper;
    private final UserMapper userMapper;

    private static final BigDecimal PRICE_PER_BET = BigDecimal.valueOf(2);

    public TicketServiceImpl(TicketMapper ticketMapper, UserMapper userMapper) {
        this.ticketMapper = ticketMapper;
        this.userMapper = userMapper;
    }

    @Override
    @Transactional
    public TicketVO buyTicket(Integer userId, String drawNo, List<Integer> numbers, Integer betCount) {
        // 校验用户
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new RuntimeException("user not found");
        }

        // 校验号码
        if (numbers == null || numbers.size() != 7) {
            throw new RuntimeException("must select 7 numbers");
        }
        Set<Integer> uniqueNumbers = new HashSet<>(numbers);
        if (uniqueNumbers.size() != 7) {
            throw new RuntimeException("numbers must not duplicate");
        }
        for (int n : numbers) {
            if (n < 1 || n > 36) {
                throw new RuntimeException("numbers must be between 1 and 36");
            }
        }

        // 计算金额
        BigDecimal amount = PRICE_PER_BET.multiply(BigDecimal.valueOf(betCount));

        // 扣款
        int rows = userMapper.deductBalance(userId, amount);
        if (rows == 0) {
            throw new RuntimeException("balance not enough");
        }

        // 保存彩票
        String numbersStr = numbers.stream()
                .map(String::valueOf)
                .collect(Collectors.joining(","));

        Ticket ticket = new Ticket();
        ticket.setUserId(userId);
        ticket.setDrawNo(drawNo);
        ticket.setNumbers(numbersStr);
        ticket.setBetCount(betCount);
        ticket.setAmount(amount);
        ticket.setStatus(0);
        ticketMapper.insert(ticket);

        TicketVO vo = new TicketVO();
        vo.setTicketId(ticket.getId());
        vo.setDrawNo(drawNo);
        vo.setNumbers(numbersStr);
        vo.setBetCount(betCount);
        vo.setAmount(amount);
        return vo;
    }

    @Override
    public PageVO<TicketHistoryVO> getHistory(Integer userId, int page, int size) {
        int offset = (page - 1) * size;
        List<TicketHistoryVO> list = ticketMapper.selectHistoryByUserId(userId, offset, size);
        int total = ticketMapper.countByUserId(userId);

        PageVO<TicketHistoryVO> pageVO = new PageVO<>();
        pageVO.setList(list);
        pageVO.setTotal(total);
        pageVO.setPage(page);
        pageVO.setSize(size);
        pageVO.setPages((int) Math.ceil((double) total / size));
        return pageVO;
    }
}