# 幼儿园管理系统（kindergarten-system）

一个面向幼儿园日常运营的管理系统，聚焦**缴费收据管理 + 考勤联动退费 + 收费统计看板**。

---

## 1. 项目简介

本项目用于规范幼儿园收费与学期管理流程，核心覆盖：

- 学生、班级、学期、费用类型等基础数据管理
- 缴费记录管理（含收据图片上传与存储）
- 学期收费汇总、多学期收费对比、按班级/费用类型统计
- 与考勤联动的请假退费计算（支持规则配置）

当前仓库以**需求与设计文档**为主，适合作为后端/前端实现阶段的基础蓝图。

---

## 2. 技术栈（规划）

### 后端

- Java 17
- Spring Boot 3.2.x
- MyBatis Plus 3.5.x
- Spring Security 6.x + JWT
- TiDB Cloud（MySQL 8.0 兼容）
- Cloudflare R2 + AWS S3 SDK

### 前端

- Vue 3 + Vite 5
- Element Plus
- Axios
- Vue Router 4
- Pinia

### 部署

- Render（后端）
- Cloudflare Pages（前端）
- TiDB Cloud（数据库）
- Cloudflare R2（对象存储）

---

## 3. 核心业务能力

1. **缴费与收据管理**
   - 缴费记录增删改查、分页筛选
   - 收据图片上传与回显

2. **收费汇总与对比**
   - 本学期/上学期总收费
   - 多学期收费对比
   - 按班级、费用类型汇总

3. **请假退费计算（考勤联动）**
   - 连续请假 5–9 天：退伙食费
   - 连续请假 10 天及以上：退保教费 + 伙食费
   - 日均费用支持配置（`refund_rule_config`）

---

## 4. 数据库设计摘要

已提供完整数据库设计与初始化 SQL（TiDB/MySQL8 可执行），核心表包括：

- `sys_user`（系统用户）
- `class_info`（班级）
- `semester`（学期）
- `fee_type`（费用类型，含 `type_code`）
- `student`（学生）
- `payment_record`（缴费记录）
- `attendance`（考勤）
- `refund_rule_config`（退费规则配置）
- `refund_record`（可选，退费结果快照）

> 详见：`docs/01-数据库设计.md`

---

## 5. 文档导航

- [00-项目概览](docs/00-项目概览.md)
- [01-数据库设计](docs/01-数据库设计.md)
- [01-数据库设计-退费规则补充](docs/01-数据库设计-退费规则补充.md)
- [02-开发计划](docs/02-开发计划.md)
- [03-后端接口开发（前后端对齐）](docs/03-后端接口开发.md)
- [06-缴费&收据管理模块补充需求](docs/06-收据收费需求.md)

---

## 6. 开发计划（摘要）

项目按 8 个阶段推进：

1. 项目初始化 + 数据库
2. 登录认证
3. 基础数据管理
4. 缴费与收据管理（含收费汇总、退费计算）
5. 考勤管理
6. 统计面板
7. 前端 Vue3 页面实现
8. 部署上线

> 详见：`docs/02-开发计划.md`

---

## 7. 当前状态

- 当前仓库主要为**产品/设计文档阶段**。
- 数据库设计文档已落地，可直接用于 Phase 1 初始化。

backend/kindergarten-system/
├── pom.xml                          # Maven 配置（Spring Boot 3.2.12 + MyBatis Plus 3.5.5 + JWT）
├── src/main/
│   ├── java/com/kindergarten/system/
│   │   ├── KindergartenSystemApplication.java
│   │   ├── common/
│   │   │   ├── config/
│   │   │   │   ├── MyBatisPlusConfig.java    # 分页插件 + MapperScan
│   │   │   │   ├── MyMetaObjectHandler.java  # 自动填充 createdAt/updatedAt
│   │   │   │   └── WebMvcConfig.java         # CORS 跨域配置
│   │   │   ├── exception/
│   │   │   │   ├── BusinessException.java    # 业务异常
│   │   │   │   └── GlobalExceptionHandler.java # 全局异常处理
│   │   │   └── result/
│   │   │       ├── Result.java               # 统一返回格式
│   │   │       ├── ResultCode.java           # 错误码枚举
│   │   │       └── PageResult.java           # 分页返回格式
│   │   └── entity/
│   │       └── BaseEntity.java               # 实体基类
│   └── resources/
│       ├── application.yml                   # 主配置
│       ├── application-dev.yml               # 开发环境（已配置 TiDB）
│       ├── application-prod.yml              # 生产环境
│       └── db/schema.sql                     # 数据库初始化脚本
