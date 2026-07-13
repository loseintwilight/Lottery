package com.lottery.controller;

import com.lottery.common.AutoTestService;
import com.lottery.common.ResultCode;
import com.lottery.pojo.vo.AutoTestResultVO;
import com.lottery.pojo.vo.R;
import com.lottery.pojo.vo.TestProgressVO;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
public class TestController {

    private final AutoTestService autoTestService;

    public TestController(AutoTestService autoTestService) {
        this.autoTestService = autoTestService;
    }

    @PostMapping("/auto-test")
    public R<AutoTestResultVO> runAutoTest() {
        try {
            AutoTestResultVO result = autoTestService.runAutoTest();
            return R.success(result);
        } catch (Exception e) {
            return R.error(ResultCode.ERROR, "auto test failed: " + e.getMessage());
        }
    }

    @GetMapping("/auto-test-progress")
    public R<TestProgressVO> getProgress() {
        return R.success(autoTestService.getProgress());
    }

    @PostMapping("/clear-data")
    public R<String> clearData() {
        try {
            String result = autoTestService.clearAllData();
            return R.success(result);
        } catch (Exception e) {
            return R.error(ResultCode.ERROR, "clear data failed: " + e.getMessage());
        }
    }
}