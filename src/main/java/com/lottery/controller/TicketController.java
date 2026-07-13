package com.lottery.controller;

import com.lottery.common.ResultCode;
import com.lottery.pojo.vo.*;
import com.lottery.service.TicketService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
public class TicketController {

    private final TicketService ticketService;

    public TicketController(TicketService ticketService) {
        this.ticketService = ticketService;
    }

    @PostMapping("/buy")
    public R<TicketVO> buyTicket(@RequestBody BuyTicketRequest req) {
        try {
            TicketVO vo = ticketService.buyTicket(req.getUserId(), req.getDrawNo(), req.getNumbers(), req.getBetCount());
            return R.success(vo);
        } catch (RuntimeException e) {
            String msg = e.getMessage();
            if ("balance not enough".equals(msg)) {
                return R.error(ResultCode.BALANCE_NOT_ENOUGH);
            }
            return R.error(ResultCode.ERROR, msg);
        }
    }

    @GetMapping("/history")
    public R<PageVO<TicketHistoryVO>> getHistory(@RequestParam Integer userId,
                                                  @RequestParam(defaultValue = "1") int page,
                                                  @RequestParam(defaultValue = "10") int size) {
        PageVO<TicketHistoryVO> result = ticketService.getHistory(userId, page, size);
        return R.success(result);
    }
}