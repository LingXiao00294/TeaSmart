package com.teasmart.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.teasmart.config.AiConfig;
import com.teasmart.entity.Product;
import com.teasmart.mapper.ProductMapper;
import com.teasmart.vo.RecommendVO;
import okhttp3.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Service
public class AiService {

    private static final Logger log = LoggerFactory.getLogger(AiService.class);
    private static final MediaType JSON_MEDIA = MediaType.get("application/json; charset=utf-8");

    private final AiConfig aiConfig;
    private final ProductMapper productMapper;
    private final KnowledgeService knowledgeService;
    private final ObjectMapper objectMapper;
    private final OkHttpClient httpClient;

    public AiService(AiConfig aiConfig, ProductMapper productMapper,
                     KnowledgeService knowledgeService, ObjectMapper objectMapper) {
        this.aiConfig = aiConfig;
        this.productMapper = productMapper;
        this.knowledgeService = knowledgeService;
        this.objectMapper = objectMapper;
        this.httpClient = new OkHttpClient.Builder()
                .connectTimeout(10, TimeUnit.SECONDS)
                .writeTimeout(10, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS)
                .build();
    }

    public List<RecommendVO> recommend() {
        List<Product> products = productMapper.selectList(
                new LambdaQueryWrapper<Product>()
                        .eq(Product::getStatus, 1)
                        .eq(Product::getDeleted, 0));

        if (!aiConfig.isAvailable()) {
            return mockRecommend(products);
        }

        try {
            String productList = products.stream()
                    .map(p -> p.getId() + ". " + p.getName() + "（" + p.getDescription() + "，¥" + p.getPrice() + "）")
                    .collect(Collectors.joining("\n"));

            String prompt = "你是茶小智饮品店的推荐助手。以下是我们的菜单：\n" + productList
                    + "\n\n请从以上商品中推荐3款，只返回严格JSON数组，不要包含任何其他文字或Markdown。"
                    + "格式：[{\"productId\":数字,\"reason\":\"推荐理由\"}]"
                    + "\n注意：productId必须是上面列表中存在的数字，不要编造。";

            String knowledge = knowledgeService.buildKnowledgeContext();
            String systemPrompt = "你是茶小智饮品推荐助手。" + knowledge;

            Map<String, Object> bodyMap = new HashMap<>();
            bodyMap.put("model", aiConfig.getModel());
            bodyMap.put("max_tokens", 2000);
            bodyMap.put("messages", List.of(
                    Map.of("role", "system", "content", systemPrompt),
                    Map.of("role", "user", "content", prompt)));

            Request request = new Request.Builder()
                    .url(aiConfig.getBaseUrl() + "/chat/completions")
                    .addHeader("Authorization", "Bearer " + aiConfig.getApiKey())
                    .post(RequestBody.create(objectMapper.writeValueAsString(bodyMap), JSON_MEDIA))
                    .build();

            try (Response response = httpClient.newCall(request).execute()) {
                if (!response.isSuccessful() || response.body() == null) {
                    log.warn("AI推荐API返回: {}", response.code());
                    return mockRecommend(products);
                }
                String responseBody = response.body().string();
                if (responseBody.isBlank()) {
                    log.warn("AI推荐API返回空响应");
                    return mockRecommend(products);
                }
                return parseRecommendResponse(responseBody, products);
            }
        } catch (Exception e) {
            log.warn("AI推荐失败，使用mock数据: {}", e.getMessage());
            return mockRecommend(products);
        }
    }

    public SseEmitter chat(String message) {
        SseEmitter emitter = new SseEmitter(60000L);

        if (!aiConfig.isAvailable()) {
            sendSseAndComplete(emitter, "AI 功能暂未配置 API Key，无法使用聊天功能。");
            return emitter;
        }

        new Thread(() -> {
            boolean dataSent = false;
            try {
                String knowledge = knowledgeService.buildKnowledgeContext();
                String chatSystemPrompt = "你是茶小智饮品店的智能点单助手。你可以推荐饮品、回答关于饮品的问题、介绍口味特点。回答要简洁友好，控制在200字以内。" + knowledge;

                Map<String, Object> bodyMap = new HashMap<>();
                bodyMap.put("model", aiConfig.getModel());
                bodyMap.put("max_tokens", 2000);
                bodyMap.put("stream", true);
                bodyMap.put("messages", List.of(
                        Map.of("role", "system", "content", chatSystemPrompt),
                        Map.of("role", "user", "content", message)));

                Request request = new Request.Builder()
                        .url(aiConfig.getBaseUrl() + "/chat/completions")
                        .addHeader("Authorization", "Bearer " + aiConfig.getApiKey())
                        .post(RequestBody.create(objectMapper.writeValueAsString(bodyMap), JSON_MEDIA))
                        .build();

                try (Response response = httpClient.newCall(request).execute()) {
                    if (!response.isSuccessful() || response.body() == null) {
                        sendSseAndComplete(emitter, "AI 服务返回错误: " + response.code());
                        return;
                    }

                    try (var reader = new BufferedReader(
                            new InputStreamReader(response.body().byteStream(), StandardCharsets.UTF_8))) {
                        String line;
                        while ((line = reader.readLine()) != null) {
                            if (line.startsWith("data: ")) {
                                String data = line.substring(6).trim();
                                if ("[DONE]".equals(data)) break;
                                try {
                                    JsonNode root = objectMapper.readTree(data);
                                    String content = root.path("choices").path(0)
                                            .path("delta").path("content").asText("");
                                    if (!content.isEmpty()) {
                                        emitter.send(SseEmitter.event().data(content));
                                        dataSent = true;
                                    }
                                } catch (Exception ignored) {
                                }
                            }
                        }
                    }
                }
                emitter.send(SseEmitter.event().data("[DONE]"));
                emitter.complete();
            } catch (Exception e) {
                log.warn("AI聊天失败: {}", e.getMessage());
                if (!dataSent) {
                    try {
                        emitter.send(SseEmitter.event().data("抱歉，AI 服务暂时不可用。"));
                        emitter.send(SseEmitter.event().data("[DONE]"));
                        emitter.complete();
                    } catch (Exception ex) {
                        emitter.completeWithError(ex);
                    }
                } else {
                    try { emitter.complete(); } catch (Exception ignored) {}
                }
            }
        }).start();

        return emitter;
    }

    private void sendSseAndComplete(SseEmitter emitter, String message) {
        try {
            emitter.send(SseEmitter.event().data(message));
            emitter.send(SseEmitter.event().data("[DONE]"));
            emitter.complete();
        } catch (Exception e) {
            emitter.completeWithError(e);
        }
    }

    private List<RecommendVO> parseRecommendResponse(String response, List<Product> products) {
        try {
            JsonNode root = objectMapper.readTree(response);
            String content = root.path("choices").path(0).path("message").path("content").asText();

            if (content.isBlank()) {
                log.warn("AI推荐content为空，可能是推理模型token不足");
                return mockRecommend(products);
            }

            String json = content;
            if (content.contains("```")) {
                int start = content.indexOf("[");
                int end = content.lastIndexOf("]");
                if (start >= 0 && end > start) {
                    json = content.substring(start, end + 1);
                }
            }

            List<Map<String, Object>> items = objectMapper.readValue(json, new TypeReference<>() {});
            Set<Long> validIds = products.stream().map(Product::getId).collect(Collectors.toSet());
            Map<Long, Product> productMap = products.stream()
                    .collect(Collectors.toMap(Product::getId, p -> p));

            List<RecommendVO> result = new ArrayList<>();
            for (Map<String, Object> item : items) {
                Long pid = Long.valueOf(item.get("productId").toString());
                if (validIds.contains(pid)) {
                    Product p = productMap.get(pid);
                    RecommendVO vo = new RecommendVO();
                    vo.setProductId(pid);
                    vo.setName(p.getName());
                    vo.setReason(item.getOrDefault("reason", "推荐品尝").toString());
                    result.add(vo);
                }
            }

            if (!result.isEmpty()) return result;
        } catch (Exception e) {
            log.warn("解析AI推荐响应失败: {}", e.getMessage());
        }

        return mockRecommend(products);
    }

    private List<RecommendVO> mockRecommend(List<Product> products) {
        List<String> reasons = List.of("人气爆款，口感醇厚，不容错过", "清爽解腻，夏日必备", "经典口味，好评如潮");
        return products.stream().limit(3).map(p -> {
            RecommendVO vo = new RecommendVO();
            vo.setProductId(p.getId());
            vo.setName(p.getName());
            int idx = products.indexOf(p);
            vo.setReason(reasons.get(idx % reasons.size()));
            return vo;
        }).collect(Collectors.toList());
    }
}
