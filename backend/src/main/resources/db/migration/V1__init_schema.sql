-- V1: 初始化茶小智数据库表结构

-- 用户表
CREATE TABLE app_user (
    id          BIGSERIAL PRIMARY KEY,
    username    VARCHAR(50)  NOT NULL UNIQUE,
    password    VARCHAR(255) NOT NULL,
    phone       VARCHAR(20),
    avatar      VARCHAR(255),
    role        VARCHAR(20)  NOT NULL DEFAULT 'USER' CHECK (role IN ('USER', 'ADMIN')),
    created_at  TIMESTAMP    NOT NULL DEFAULT now(),
    updated_at  TIMESTAMP    NOT NULL DEFAULT now()
);

-- 商品分类表
CREATE TABLE category (
    id          BIGSERIAL PRIMARY KEY,
    name        VARCHAR(50) NOT NULL,
    sort_order  INT         NOT NULL DEFAULT 0,
    status      SMALLINT    NOT NULL DEFAULT 1,
    deleted     SMALLINT    NOT NULL DEFAULT 0
);

-- 商品表
CREATE TABLE product (
    id          BIGSERIAL PRIMARY KEY,
    category_id BIGINT       NOT NULL REFERENCES category(id),
    name        VARCHAR(100) NOT NULL,
    description TEXT,
    image       VARCHAR(255),
    price       NUMERIC(10,2) NOT NULL,
    status      SMALLINT     NOT NULL DEFAULT 1,
    deleted     SMALLINT     NOT NULL DEFAULT 0,
    created_at  TIMESTAMP    NOT NULL DEFAULT now(),
    updated_at  TIMESTAMP    NOT NULL DEFAULT now()
);

-- 商品规格表
CREATE TABLE product_spec (
    id          BIGSERIAL PRIMARY KEY,
    product_id  BIGINT       NOT NULL REFERENCES product(id),
    spec_type   VARCHAR(20)  NOT NULL,
    spec_name   VARCHAR(50)  NOT NULL,
    price_diff  NUMERIC(10,2) NOT NULL DEFAULT 0
);

-- 购物车表
CREATE TABLE cart_item (
    id          BIGSERIAL PRIMARY KEY,
    user_id     BIGINT      NOT NULL REFERENCES app_user(id),
    product_id  BIGINT      NOT NULL REFERENCES product(id),
    spec_key    VARCHAR(100) NOT NULL,
    quantity    INT         NOT NULL DEFAULT 1,
    created_at  TIMESTAMP   NOT NULL DEFAULT now(),
    updated_at  TIMESTAMP   NOT NULL DEFAULT now(),
    UNIQUE(user_id, product_id, spec_key)
);

-- 购物车规格关联表
CREATE TABLE cart_item_spec (
    cart_item_id BIGINT NOT NULL REFERENCES cart_item(id) ON DELETE CASCADE,
    spec_id      BIGINT NOT NULL REFERENCES product_spec(id),
    PRIMARY KEY (cart_item_id, spec_id)
);

-- 订单表
CREATE TABLE tea_order (
    id           BIGSERIAL PRIMARY KEY,
    order_no     VARCHAR(32)   NOT NULL UNIQUE,
    user_id      BIGINT        NOT NULL REFERENCES app_user(id),
    total_amount NUMERIC(10,2) NOT NULL,
    status       SMALLINT      NOT NULL DEFAULT 0 CHECK (status IN (0, 1, 2, 3, 4)),
    remark       VARCHAR(255),
    created_at   TIMESTAMP     NOT NULL DEFAULT now(),
    updated_at   TIMESTAMP     NOT NULL DEFAULT now()
);

-- 订单明细表
CREATE TABLE order_item (
    id           BIGSERIAL PRIMARY KEY,
    order_id     BIGINT        NOT NULL REFERENCES tea_order(id),
    product_id   BIGINT        NOT NULL,
    product_name VARCHAR(100)  NOT NULL,
    spec_info    VARCHAR(255),
    price        NUMERIC(10,2) NOT NULL,
    quantity     INT           NOT NULL
);

-- 轮播图表
CREATE TABLE banner (
    id          BIGSERIAL PRIMARY KEY,
    image       VARCHAR(255) NOT NULL,
    link        VARCHAR(255),
    sort_order  INT          NOT NULL DEFAULT 0,
    status      SMALLINT     NOT NULL DEFAULT 1,
    deleted     SMALLINT     NOT NULL DEFAULT 0
);

-- 索引
CREATE INDEX idx_product_category_id ON product(category_id);
CREATE INDEX idx_product_status      ON product(status);
CREATE INDEX idx_product_deleted     ON product(deleted);
CREATE INDEX idx_cart_item_user_id   ON cart_item(user_id);
CREATE INDEX idx_order_user_id       ON tea_order(user_id);
CREATE INDEX idx_order_status        ON tea_order(status);
CREATE INDEX idx_order_created_at    ON tea_order(created_at);
CREATE INDEX idx_banner_status       ON banner(status);
