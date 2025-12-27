# Charging Order System

充电订单系统 - 基于 DDD 架构和 COLA 状态机

## 项目简介

这是一个充电桩订单管理系统，采用领域驱动设计（DDD）架构，使用阿里巴巴 COLA 状态机管理订单状态流转。

## 技术栈

- **Java**: 1.8 (Java 8)
- **Spring Boot**: 2.7.18
- **COLA Statemachine**: 4.3.2
- **Lombok**: 自动生成样板代码
- **Maven**: 项目构建工具

## 项目结构

```
charging-order-system/
├── src/
│   ├── main/
│   │   ├── java/com/charging/order/
│   │   │   ├── adapter/           # 适配器层（Web层）
│   │   │   │   └── web/          # REST API 控制器
│   │   │   ├── app/              # 应用服务层
│   │   │   │   └── service/      # 业务服务
│   │   │   ├── domain/           # 领域层
│   │   │   │   ├── model/        # 领域模型
│   │   │   │   └── statemachine/ # 状态机配置
│   │   │   └── infrastructure/   # 基础设施层
│   │   │       └── persistence/  # 数据持久化
│   │   └── resources/
│   │       └── application.properties
│   └── test/
└── pom.xml
```

## DDD 分层说明

### 1. Adapter Layer (适配器层)
- **职责**: 处理外部请求，提供 REST API 接口
- **包含**: ChargingOrderController - 订单管理 HTTP 接口

### 2. Application Layer (应用服务层)
- **职责**: 编排业务流程，协调领域对象
- **包含**: ChargingOrderService - 订单业务逻辑服务

### 3. Domain Layer (领域层)
- **职责**: 核心业务逻辑和规则
- **包含**: 
  - ChargingOrder - 充电订单领域模型
  - OrderState - 订单状态枚举
  - OrderEvent - 订单事件枚举
  - OrderStateMachineConfig - COLA 状态机配置

### 4. Infrastructure Layer (基础设施层)
- **职责**: 提供技术支撑，如数据持久化
- **包含**: ChargingOrderRepository - 订单数据仓储

## 订单状态流转

```
CREATED (已创建)
    ↓ START_CHARGING
CHARGING (充电中)
    ↓ FINISH_CHARGING
COMPLETED (已完成)
    ↓ CLOSE_ORDER
CLOSED (已关闭)

CREATED (已创建)
    ↓ CANCEL_ORDER
CANCELLED (已取消)
    ↓ CLOSE_ORDER
CLOSED (已关闭)
```

## 快速开始

### 1. 构建项目

```bash
mvn clean install
```

### 2. 运行应用

```bash
mvn spring-boot:run
```

应用将在 `http://localhost:8080` 启动

### 3. API 接口

#### 创建订单
```bash
POST /api/orders?userId=user123&chargingPileId=pile456
```

#### 开始充电
```bash
POST /api/orders/{orderId}/start
Content-Type: application/json

{订单对象}
```

#### 完成充电
```bash
POST /api/orders/{orderId}/finish?chargingAmount=50.5&orderAmount=60.00
Content-Type: application/json

{订单对象}
```

#### 取消订单
```bash
POST /api/orders/{orderId}/cancel
Content-Type: application/json

{订单对象}
```

#### 关闭订单
```bash
POST /api/orders/{orderId}/close
Content-Type: application/json

{订单对象}
```

## 开发指南

### 扩展状态机

在 `OrderStateMachineConfig` 中添加新的状态转换：

```java
builder.externalTransition()
    .from(源状态)
    .to(目标状态)
    .on(触发事件)
    .when(条件检查)
    .perform(执行动作);
```

### 添加新的业务逻辑

1. 在 domain 层定义领域模型和业务规则
2. 在 app 层实现业务服务
3. 在 adapter 层暴露 API 接口
4. 在 infrastructure 层实现技术细节

## 注意事项

- 当前使用内存存储订单数据，重启后数据会丢失
- 生产环境建议集成数据库（MySQL、PostgreSQL 等）
- 可以添加 Spring Data JPA 依赖实现真正的持久化

## 下一步建议

1. 集成数据库持久化
2. 添加单元测试和集成测试
3. 实现完整的异常处理机制
4. 添加日志记录和监控
5. 实现分布式事务支持
6. 添加 API 文档（Swagger/OpenAPI）
