# 图书馆管理系统

基于 Spring Boot、MyBatis-Plus、MySQL、Vue 3 的图书馆管理系统，包含读者、图书管理员、超级管理员三类角色。

## 技术栈

- 后端：Spring Boot 3.3、Spring Security、JWT、MyBatis-Plus、MySQL 8
- 前端：Vue 3、Vite、Pinia、Vue Router、Axios、Lucide Icons
- 数据库：MySQL，默认账号 `root`，密码 `123456`

## 已实现功能

- 读者：登录、个人资料、修改密码、图书检索、分类查询、图书详情、线上借阅、续借、在借与历史记录、逾期与罚款明细、公告查看。
- 图书管理员：图书新增/编辑接口、上下架接口、库存维护字段、分类/标签/出版社管理、线下借书、还书、人工续借、罚款处理、读者查看、反馈处理、基础统计。
- 超级管理员：管理员新增/禁用、角色权限边界、系统配置、图书/读者/借阅全量管理接口、日志查看、公告发布、批量导入导出与备份恢复预留模块入口。

## 启动方式

1. 初始化数据库：

```bash
mysql -uroot -p123456 < backend/src/main/resources/db/schema.sql
```

2. 启动后端：

```bash
cd backend
mvn spring-boot:run
```

3. 启动前端：

```bash
cd frontend
pnpm install
pnpm dev
```

前端访问：http://localhost:5173/

后端接口：http://localhost:8080/api

## 演示账号

| 角色 | 用户名 | 密码 |
| --- | --- | --- |
| 读者 | `reader` | `reader123` |
| 图书管理员 | `librarian` | `admin123` |
| 超级管理员 | `superadmin` | `root123` |

## 构建验证

```bash
cd backend && mvn -q -DskipTests package
cd frontend && pnpm build
```

前端 `package.json` 使用 `@rollup/wasm-node` 覆盖 Rollup 原生包，以避开部分 macOS/Node 环境中的原生二进制签名问题。
