package com.teasmart.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.teasmart.config.AiConfig;
import com.teasmart.entity.Product;
import com.teasmart.mapper.ProductMapper;
import com.teasmart.vo.RecommendVO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class AiService {

    private static final Logger log = LoggerFactory.getLogger(AiService.class);

    private final AiConfig aiConfig;
    private final ProductMapper productMapper;
    private final ObjectMapper objectMapper;
    private final RestClient restClient;

    public AiService(AiConfig aiConfig, ProductMapper productMapper, ObjectMapper objectMapper) {
        this.aiConfig = aiConfig;
        this.productMapper = productMapper;
        this.objectMapper = objectMapper;
        this.restClient = RestClient.builder().build();
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

            Map<String, Object> body = Map.of(
                    "model", aiConfig.getModel(),
                    "max_tokens", aiConfig.getMaxTokens(),
                    "messages", List.of(
                            Map.of("role", "system", "content", "你是茶小智饮品推荐助手"),
                            Map.of("role", "user", "content", prompt)));

            String response = restClient.post()
                    .uri(aiConfig.getBaseUrl() + "/chat/completions")
                    .header("Authorization", "Bearer " + aiConfig.getApiKey())
                    .header("Content-Type", "application/json")
                    .body(body)
                    .retrieve()
                    .body(String.class);

            return parseRecommendResponse(response, products);
        } catch (Exception e) {
            log.warn("AI推荐失败，使用mock数据: {}", e.getMessage());
            return mockRecommend(products);
        }
    }

    private List<RecommendVO> parseRecommendResponse(String response, List<Product> products) {
        try {
            JsonNode root = objectMapper.readTree(response);
            String content = root.path("choices").path(0).path("message").path("content").asText();

            // 尝试提取 JSON（可能被 ```json 包裹）
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
