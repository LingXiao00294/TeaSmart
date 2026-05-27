-- V2: 初始数据

-- 管理员账号 (密码: 123456)
INSERT INTO app_user (username, password, phone, role) VALUES
    ('admin', '$2b$10$bxf/Oxyj53gG/Ru8Gxekuu44pR/gpmHWuEn.PWudMW/M2lsdY4g3W', '13900000000', 'ADMIN');

-- 分类
INSERT INTO category (name, sort_order) VALUES
    ('奶茶', 1),
    ('果茶', 2),
    ('咖啡', 3),
    ('小食', 4);

-- 商品 (category_id: 奶茶=1, 果茶=2, 咖啡=3, 小食=4)
INSERT INTO product (category_id, name, description, price, status) VALUES
    (1, '珍珠奶茶',    '经典珍珠奶茶，香浓醇厚',           8.00, 1),
    (1, '芋泥波波奶茶', '绵密芋泥搭配Q弹波波',            10.00, 1),
    (2, '柠檬茶',     '鲜柠檬手打，酸甜清爽',             7.00, 1),
    (2, '杨枝甘露',   '芒果椰汁西柚粒，热带风味',          12.00, 1),
    (3, '美式咖啡',   '经典美式，醇香浓郁',               9.00, 1),
    (3, '生椰拿铁',   '椰乳搭配浓缩咖啡，丝滑细腻',        11.00, 1),
    (4, '鸡米花',     '外酥里嫩，香脆可口',               6.00, 1),
    (4, '薯条',       '金黄酥脆，经典小食',               5.00, 1);

-- 商品规格: 每商品 9 条 (2杯型 + 4糖度 + 3冰度)
-- 珍珠奶茶 (product_id=1)
INSERT INTO product_spec (product_id, spec_type, spec_name, price_diff) VALUES
    (1, 'cup_size',  '中杯',   0),  -- id=1
    (1, 'cup_size',  '大杯',   2),  -- id=2
    (1, 'sweetness', '全糖',   0),  -- id=3
    (1, 'sweetness', '七分糖', 0),  -- id=4
    (1, 'sweetness', '半糖',   0),  -- id=5
    (1, 'sweetness', '无糖',   0),  -- id=6
    (1, 'ice',       '正常冰', 0),  -- id=7
    (1, 'ice',       '少冰',   0),  -- id=8
    (1, 'ice',       '去冰',   0);  -- id=9

-- 芋泥波波奶茶 (product_id=2)
INSERT INTO product_spec (product_id, spec_type, spec_name, price_diff) VALUES
    (2, 'cup_size',  '中杯',   0),
    (2, 'cup_size',  '大杯',   2),
    (2, 'sweetness', '全糖',   0),
    (2, 'sweetness', '七分糖', 0),
    (2, 'sweetness', '半糖',   0),
    (2, 'sweetness', '无糖',   0),
    (2, 'ice',       '正常冰', 0),
    (2, 'ice',       '少冰',   0),
    (2, 'ice',       '去冰',   0);

-- 柠檬茶 (product_id=3)
INSERT INTO product_spec (product_id, spec_type, spec_name, price_diff) VALUES
    (3, 'cup_size',  '中杯',   0),
    (3, 'cup_size',  '大杯',   2),
    (3, 'sweetness', '全糖',   0),
    (3, 'sweetness', '七分糖', 0),
    (3, 'sweetness', '半糖',   0),
    (3, 'sweetness', '无糖',   0),
    (3, 'ice',       '正常冰', 0),
    (3, 'ice',       '少冰',   0),
    (3, 'ice',       '去冰',   0);

-- 杨枝甘露 (product_id=4)
INSERT INTO product_spec (product_id, spec_type, spec_name, price_diff) VALUES
    (4, 'cup_size',  '中杯',   0),
    (4, 'cup_size',  '大杯',   3),
    (4, 'sweetness', '全糖',   0),
    (4, 'sweetness', '七分糖', 0),
    (4, 'sweetness', '半糖',   0),
    (4, 'sweetness', '无糖',   0),
    (4, 'ice',       '正常冰', 0),
    (4, 'ice',       '少冰',   0),
    (4, 'ice',       '去冰',   0);

-- 美式咖啡 (product_id=5)
INSERT INTO product_spec (product_id, spec_type, spec_name, price_diff) VALUES
    (5, 'cup_size',  '中杯',   0),
    (5, 'cup_size',  '大杯',   3),
    (5, 'sweetness', '全糖',   0),
    (5, 'sweetness', '半糖',   0),
    (5, 'sweetness', '无糖',   0),
    (5, 'ice',       '热',     0),
    (5, 'ice',       '正常冰', 0),
    (5, 'ice',       '去冰',   0);

-- 生椰拿铁 (product_id=6)
INSERT INTO product_spec (product_id, spec_type, spec_name, price_diff) VALUES
    (6, 'cup_size',  '中杯',   0),
    (6, 'cup_size',  '大杯',   3),
    (6, 'sweetness', '全糖',   0),
    (6, 'sweetness', '半糖',   0),
    (6, 'sweetness', '无糖',   0),
    (6, 'ice',       '热',     0),
    (6, 'ice',       '正常冰', 0),
    (6, 'ice',       '去冰',   0);

-- 鸡米花 (product_id=7) — 小食无杯型/糖度/冰度，仅保留一份规格避免空表
INSERT INTO product_spec (product_id, spec_type, spec_name, price_diff) VALUES
    (7, 'cup_size',  '标准', 0);

-- 薯条 (product_id=8)
INSERT INTO product_spec (product_id, spec_type, spec_name, price_diff) VALUES
    (8, 'cup_size',  '标准', 0);

-- 示例轮播图
INSERT INTO banner (image, link, sort_order, status) VALUES
    ('/uploads/banner1.jpg', '/menu', 1, 1),
    ('/uploads/banner2.jpg', '/menu', 2, 1),
    ('/uploads/banner3.jpg', '/menu', 3, 1);
