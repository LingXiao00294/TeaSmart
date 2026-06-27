package com.teasmart.controller;

import com.teasmart.common.Result;
import com.teasmart.dto.ChatRequest;
import com.teasmart.service.AiService;
import com.teasmart.vo.RecommendVO;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

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

    @PostMapping(value = "/chat", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter chat(@RequestBody ChatRequest req) {
        String message = req.getMessage() == null ? "" : req.getMessage();
        List<ChatRequest.ChatMessage> history =
                req.getHistory() == null ? List.of() : req.getHistory();
        return aiService.chat(message, history);
    }
}
