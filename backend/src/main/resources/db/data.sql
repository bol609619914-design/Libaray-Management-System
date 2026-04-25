USE library_management;

INSERT INTO categories (name, code, description)
VALUES ('文学', 'LITERATURE', '小说、散文、诗歌等文学读物'),
       ('计算机', 'TECH', '软件工程、数据库、人工智能等技术书籍'),
       ('历史', 'HISTORY', '历史、人文与社会研究')
ON DUPLICATE KEY UPDATE name = VALUES(name);

INSERT INTO publishers (name, contact)
SELECT '人民邮电出版社', 'service@ptpress.com.cn'
WHERE NOT EXISTS (SELECT 1 FROM publishers WHERE name = '人民邮电出版社');

INSERT INTO tags (name)
VALUES ('热门'), ('经典'), ('新书')
ON DUPLICATE KEY UPDATE name = VALUES(name);

INSERT INTO system_configs (config_key, config_value, description)
VALUES ('borrow.days', '30', '默认借阅天数'),
       ('renew.max.count', '2', '最大续借次数'),
       ('fine.per.day', '0.50', '每日逾期罚金')
ON DUPLICATE KEY UPDATE config_value = VALUES(config_value);
