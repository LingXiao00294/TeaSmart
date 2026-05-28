-- V3: AI 知识库表
CREATE TABLE knowledge (
    id          BIGSERIAL PRIMARY KEY,
    title       VARCHAR(100) NOT NULL,
    content     TEXT         NOT NULL,
    category    VARCHAR(50)  NOT NULL DEFAULT '自定义',
    sort_order  INT          NOT NULL DEFAULT 0,
    status      SMALLINT     NOT NULL DEFAULT 1,
    created_at  TIMESTAMP    NOT NULL DEFAULT now(),
    updated_at  TIMESTAMP    NOT NULL DEFAULT now()
);

CREATE INDEX idx_knowledge_category ON knowledge(category);
CREATE INDEX idx_knowledge_status   ON knowledge(status);

-- 预置知识库数据
INSERT INTO knowledge (title, content, category, sort_order) VALUES
    ('品牌介绍', '茶小智（TeaSmart）是一家专注于新鲜现制茶饮的品牌，坚持使用优质茶叶和新鲜水果，为顾客带来健康美味的饮品体验。', '店铺简介', 1),
    ('营业时间', '每日 9:00 - 22:00，全年无休。', 'FAQ', 1),
    ('配送范围', '目前支持门店自取，外送功能即将上线。', 'FAQ', 2),
    ('会员优惠', '注册即享首单 9 折优惠，累计消费满 100 元升级银卡会员享 8.5 折。', '促销活动', 1),
    ('新品推荐', '生椰拿铁限时特价 ¥9.9，活动截止本月底。', '促销活动', 2);
