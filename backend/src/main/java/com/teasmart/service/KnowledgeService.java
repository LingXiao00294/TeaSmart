package com.teasmart.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.teasmart.common.BusinessException;
import com.teasmart.dto.KnowledgeDTO;
import com.teasmart.entity.Knowledge;
import com.teasmart.mapper.KnowledgeMapper;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class KnowledgeService {

    private final KnowledgeMapper knowledgeMapper;

    public KnowledgeService(KnowledgeMapper knowledgeMapper) {
        this.knowledgeMapper = knowledgeMapper;
    }

    public List<Knowledge> listAll() {
        return knowledgeMapper.selectList(
                new LambdaQueryWrapper<Knowledge>()
                        .orderByAsc(Knowledge::getCategory)
                        .orderByAsc(Knowledge::getSortOrder));
    }

    public List<Knowledge> listActive() {
        return knowledgeMapper.selectList(
                new LambdaQueryWrapper<Knowledge>()
                        .eq(Knowledge::getStatus, 1)
                        .orderByAsc(Knowledge::getCategory)
                        .orderByAsc(Knowledge::getSortOrder));
    }

    /** 将启用的知识库条目格式化为 prompt 文本 */
    public String buildKnowledgeContext() {
        List<Knowledge> items = listActive();
        if (items.isEmpty()) return "";

        StringBuilder sb = new StringBuilder("\n\n以下是店铺知识库信息，请在回答时参考：\n");
        String currentCategory = "";
        for (Knowledge k : items) {
            if (!k.getCategory().equals(currentCategory)) {
                currentCategory = k.getCategory();
                sb.append("\n【").append(currentCategory).append("】\n");
            }
            sb.append("- ").append(k.getTitle()).append("：").append(k.getContent()).append("\n");
        }
        return sb.toString();
    }

    public Knowledge create(KnowledgeDTO dto) {
        Knowledge k = new Knowledge();
        k.setTitle(dto.getTitle());
        k.setContent(dto.getContent());
        k.setCategory(dto.getCategory());
        k.setSortOrder(dto.getSortOrder());
        k.setStatus(dto.getStatus());
        knowledgeMapper.insert(k);
        return k;
    }

    public Knowledge update(Long id, KnowledgeDTO dto) {
        Knowledge k = knowledgeMapper.selectById(id);
        if (k == null) throw BusinessException.notFound("知识条目不存在");
        k.setTitle(dto.getTitle());
        k.setContent(dto.getContent());
        k.setCategory(dto.getCategory());
        k.setSortOrder(dto.getSortOrder());
        k.setStatus(dto.getStatus());
        knowledgeMapper.updateById(k);
        return k;
    }

    public void delete(Long id) {
        Knowledge k = knowledgeMapper.selectById(id);
        if (k == null) throw BusinessException.notFound("知识条目不存在");
        knowledgeMapper.deleteById(id);
    }
}
