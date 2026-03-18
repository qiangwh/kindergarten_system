-- 幼儿园管理系统 - 数据库初始化脚本
-- 适用数据库：TiDB Cloud（MySQL 8.0 兼容）

-- 建库
CREATE DATABASE IF NOT EXISTS kindergarten_db
  CHARACTER SET utf8mb4
  COLLATE utf8mb4_general_ci;
USE kindergarten_db;

-- 1) 系统用户
CREATE TABLE IF NOT EXISTS sys_user (
  id           BIGINT PRIMARY KEY AUTO_INCREMENT,
  username     VARCHAR(50) NOT NULL UNIQUE,
  password     VARCHAR(255) NOT NULL,
  real_name    VARCHAR(50),
  role         VARCHAR(20) NOT NULL DEFAULT 'ADMIN',
  status       TINYINT NOT NULL DEFAULT 1,
  created_at   DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at   DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) COMMENT='系统用户';

-- 2) 班级类型（小班、中班、大班）
CREATE TABLE IF NOT EXISTS class_type (
  id           BIGINT PRIMARY KEY AUTO_INCREMENT,
  type_name    VARCHAR(50) NOT NULL COMMENT '类型名称：小班、中班、大班',
  sort_order   INT NOT NULL DEFAULT 0 COMMENT '排序',
  status       TINYINT NOT NULL DEFAULT 1 COMMENT '状态：1-启用，0-禁用',
  created_at   DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at   DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  UNIQUE KEY uk_class_type_name (type_name)
) COMMENT='班级类型';

-- 3) 班级（具体班级，如：小班1班、小班2班）
CREATE TABLE IF NOT EXISTS class_info (
  id             BIGINT PRIMARY KEY AUTO_INCREMENT,
  class_type_id  BIGINT NOT NULL COMMENT '班级类型ID',
  class_name     VARCHAR(50) NOT NULL COMMENT '班级名称：小班1班、中班2班等',
  grade_year     VARCHAR(10) COMMENT '年级年份：如2026',
  max_students   INT DEFAULT 30 COMMENT '最大学生数',
  current_count  INT DEFAULT 0 COMMENT '当前学生数',
  status         TINYINT NOT NULL DEFAULT 1 COMMENT '状态：1-启用，0-禁用',
  created_at     DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at     DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  UNIQUE KEY uk_class_name_year (class_name, grade_year),
  KEY idx_class_type (class_type_id),
  KEY idx_class_status_grade (status, grade_year, id),
  CONSTRAINT fk_class_type FOREIGN KEY (class_type_id) REFERENCES class_type(id)
) COMMENT='班级';

-- 3) 学期
CREATE TABLE IF NOT EXISTS semester (
  id             BIGINT PRIMARY KEY AUTO_INCREMENT,
  semester_name  VARCHAR(100) NOT NULL,
  start_date     DATE NOT NULL,
  end_date       DATE NOT NULL,
  is_current     TINYINT NOT NULL DEFAULT 0,
  status         TINYINT NOT NULL DEFAULT 1,
  created_at     DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at     DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  CHECK (start_date <= end_date)
) COMMENT='学期';

-- 4) 费用类型（type_code 供退费规则映射）
CREATE TABLE IF NOT EXISTS fee_type (
  id            BIGINT PRIMARY KEY AUTO_INCREMENT,
  type_code     VARCHAR(30) NOT NULL,
  type_name     VARCHAR(50) NOT NULL,
  description   VARCHAR(255),
  sort_order    INT NOT NULL DEFAULT 0,
  status        TINYINT NOT NULL DEFAULT 1,
  created_at    DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at    DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  UNIQUE KEY uk_fee_type_code (type_code)
) COMMENT='费用类型';

-- 6) 学生
CREATE TABLE IF NOT EXISTS student (
  id             BIGINT PRIMARY KEY AUTO_INCREMENT,
  name           VARCHAR(50) NOT NULL COMMENT '学生姓名',
  gender         CHAR(1) COMMENT '性别：M-男，F-女',
  birthday       DATE COMMENT '出生日期',
  class_id       BIGINT NOT NULL COMMENT '班级ID',
  student_no     VARCHAR(50) COMMENT '学号',
  parent_name    VARCHAR(50) COMMENT '家长姓名',
  parent_phone   VARCHAR(20) COMMENT '家长电话',
  parent_wechat  VARCHAR(50) COMMENT '家长微信',
  enroll_date    DATE COMMENT '入园日期',
  leave_date     DATE COMMENT '离园日期',
  status         VARCHAR(20) NOT NULL DEFAULT 'active' COMMENT '状态：active-在读，inactive-离园',
  remark         VARCHAR(500) COMMENT '备注',
  created_at     DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at     DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  KEY idx_student_class (class_id),
  KEY idx_student_class_status (class_id, status),
  KEY idx_student_status (status),
  KEY idx_student_no (student_no),
  CONSTRAINT fk_student_class FOREIGN KEY (class_id) REFERENCES class_info(id)
) COMMENT='学生';

-- 6) 缴费记录（含收据）
CREATE TABLE IF NOT EXISTS payment_record (
  id                 BIGINT PRIMARY KEY AUTO_INCREMENT,
  student_id         BIGINT NOT NULL COMMENT '学生ID',
  semester_id        BIGINT NOT NULL COMMENT '学期ID',
  fee_type_id        BIGINT NOT NULL COMMENT '费用类型ID',
  amount             DECIMAL(10,2) NOT NULL COMMENT '缴费金额',
  pay_date           DATE NOT NULL DEFAULT (CURRENT_DATE) COMMENT '缴费日期',
  receipt_no         VARCHAR(100) COMMENT '收据编号',
  receipt_images     JSON COMMENT '收据图片列表（JSON数组）',
  receipt_status     TINYINT NOT NULL DEFAULT 0 COMMENT '收据状态：0-无收据，1-待审核，2-已审核',
  remark             VARCHAR(500) COMMENT '备注',
  created_at         DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at         DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  KEY idx_pay_student (student_id),
  KEY idx_pay_semester_student (semester_id, student_id),
  KEY idx_pay_semester_fee_type (semester_id, fee_type_id),
  KEY idx_pay_semester (semester_id),
  KEY idx_pay_fee_type (fee_type_id),
  KEY idx_pay_date (pay_date),
  KEY idx_pay_receipt_status (receipt_status),
  CONSTRAINT fk_payment_student FOREIGN KEY (student_id) REFERENCES student(id),
  CONSTRAINT fk_payment_semester FOREIGN KEY (semester_id) REFERENCES semester(id),
  CONSTRAINT fk_payment_fee_type FOREIGN KEY (fee_type_id) REFERENCES fee_type(id)
) COMMENT='缴费记录';

-- 7) 考勤
CREATE TABLE IF NOT EXISTS attendance (
  id            BIGINT PRIMARY KEY AUTO_INCREMENT,
  student_id    BIGINT NOT NULL,
  class_id      BIGINT NOT NULL,
  attend_date   DATE NOT NULL,
  status        VARCHAR(20) NOT NULL COMMENT 'present/absent/leave',
  remark        VARCHAR(200),
  created_at    DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at    DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  UNIQUE KEY uk_att_student_date (student_id, attend_date),
  KEY idx_att_class_date (class_id, attend_date),
  KEY idx_att_student_status_date (student_id, status, attend_date),
  KEY idx_att_status (status),
  CONSTRAINT fk_att_student FOREIGN KEY (student_id) REFERENCES student(id),
  CONSTRAINT fk_att_class FOREIGN KEY (class_id) REFERENCES class_info(id)
) COMMENT='考勤';

-- 8) 退费规则配置（核心）
CREATE TABLE IF NOT EXISTS refund_rule_config (
  id             BIGINT PRIMARY KEY AUTO_INCREMENT,
  rule_type      VARCHAR(30) NOT NULL COMMENT 'LEAVE_5_9 / LEAVE_10_PLUS',
  fee_type_code  VARCHAR(30) NOT NULL COMMENT 'meal / education',
  daily_rate     DECIMAL(10,2) NOT NULL,
  min_days       INT NOT NULL,
  max_days       INT NULL,
  semester_id    BIGINT NULL COMMENT 'NULL=全局规则',
  status         TINYINT NOT NULL DEFAULT 1,
  created_at     DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at     DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  KEY idx_refund_rule_semester (semester_id),
  KEY idx_refund_rule_type (rule_type),
  CONSTRAINT fk_refund_rule_semester FOREIGN KEY (semester_id) REFERENCES semester(id)
) COMMENT='退费规则配置';

-- 9) 退费结果快照（可选）
CREATE TABLE IF NOT EXISTS refund_record (
  id                BIGINT PRIMARY KEY AUTO_INCREMENT,
  student_id        BIGINT NOT NULL,
  semester_id       BIGINT NOT NULL,
  refund_type       VARCHAR(20) NOT NULL DEFAULT 'LEAVE' COMMENT '退费类型：LEAVE-请假退费，DROPOUT-离园退费',
  leave_days_total  INT NULL COMMENT '请假总天数（请假退费时使用）',
  actual_attend_days INT NULL COMMENT '实际在园天数（离园退费时使用）',
  refund_days       INT NULL COMMENT '应退天数（离园退费时使用）',
  leave_date        DATE NULL COMMENT '离园日期（离园退费时使用）',
  refund_amount     DECIMAL(10,2) NOT NULL DEFAULT 0.00,
  detail_json       JSON NULL COMMENT '分段明细快照',
  calculated_at     DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  calculated_by     BIGINT NULL,
  created_at        DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at        DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  KEY idx_refund_student_semester (student_id, semester_id),
  KEY idx_refund_type (refund_type),
  CONSTRAINT fk_refund_student FOREIGN KEY (student_id) REFERENCES student(id),
  CONSTRAINT fk_refund_semester FOREIGN KEY (semester_id) REFERENCES semester(id),
  CONSTRAINT fk_refund_user FOREIGN KEY (calculated_by) REFERENCES sys_user(id)
) COMMENT='退费结果快照（支持请假退费和离园退费）';

-- ========= 初始数据 =========

-- 管理员（密码: admin123，BCrypt加密）
INSERT INTO sys_user (username, password, real_name, role)
VALUES ('admin', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iAt6Z5EO', '管理员', 'ADMIN');

-- 班级类型
INSERT INTO class_type (type_name, sort_order) VALUES
('小班', 1), ('中班', 2), ('大班', 3);

-- 班级（示例数据）
INSERT INTO class_info (class_type_id, class_name, grade_year, max_students) VALUES
(1, '小班1班', '2026', 25),
(1, '小班2班', '2026', 25),
(2, '中班1班', '2026', 30),
(2, '中班2班', '2026', 30),
(3, '大班1班', '2026', 35),
(3, '大班2班', '2026', 35);

-- 费用类型（推荐 code）
INSERT INTO fee_type (type_code, type_name, sort_order) VALUES
('education', '学费', 1),
('meal', '餐费', 2),
('misc', '杂费', 3);

-- 退费规则（全局默认）
INSERT INTO refund_rule_config (rule_type, fee_type_code, daily_rate, min_days, max_days)
VALUES
('LEAVE_5_9', 'meal', 12.7, 5, 9),
('LEAVE_10_PLUS', 'education', 66.3, 10, NULL),
('LEAVE_10_PLUS', 'meal', 61.7, 10, NULL);
