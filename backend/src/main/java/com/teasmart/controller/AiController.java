package com.teasmart.controller;

import com.teasmart.common.Result;
import com.teasmart.service.AiService;
import com.teasmart.vo.RecommendVO;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/ai")
public class AiController {

    private final AiService aiService;

    public AiController(AiService aiService) {
        this.aiService = aiService;
    }

    @PostMapping("/recommend")
    public Result<List<RecommendVO>> recommend() {
        return Result.ok(aiService.recommend());
    }
}
