package com.lottery.controller;

import com.lottery.common.ResultCode;
import com.lottery.pojo.vo.DrawRequest;
import com.lottery.pojo.vo.DrawResultVO;
import com.lottery.pojo.vo.R;
import com.lottery.service.DrawService;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/api")
public class DrawController {

    private final DrawService drawService;

    public DrawController(DrawService drawService) {
        this.drawService = drawService;
    }

    /**
     * 滚动号码接口，每次返回一组随机号码（不持久化，仅用于前端动画）
     */
    @GetMapping("/draw/rolling")
    public R<List<Integer>> getRollingNumbers() {
        List<Integer> numbers = drawService.generateRollingNumbers();
        return R.success(numbers);
    }

    @PostMapping("/draw")
    public R<DrawResultVO> draw(@RequestBody DrawRequest req) {
        try {
            DrawResultVO result = drawService.draw(req.getDrawNo());
            return R.success(result);
        } catch (RuntimeException e) {
            return R.error(ResultCode.DRAW_ALREADY_EXISTS, e.getMessage());
        }
    }

    @GetMapping("/current-draw")
    public R<String> getCurrentDrawNo() {
        String drawNo = drawService.getCurrentDrawNo();
        return R.success(drawNo);
    }

    @GetMapping("/draw/result")
    public R<DrawResultVO> getDrawResult(@RequestParam String drawNo) {
        try {
            DrawResultVO result = drawService.getDrawResult(drawNo);
            return R.success(result);
        } catch (RuntimeException e) {
            return R.error(ResultCode.DRAW_NOT_FOUND);
        }
    }

    @GetMapping("/latest-draw-result")
    public R<DrawResultVO> getLatestCompletedDraw() {
        DrawResultVO result = drawService.getLatestCompletedDraw();
        if (result == null) {
            return R.error(ResultCode.DRAW_NOT_FOUND);
        }
        return R.success(result);
    }
}