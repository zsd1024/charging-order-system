# IntelliJ IDEA 调试配置速查表
## Quick Reference for IntelliJ IDEA Debugging

---

## 🎯 快速开始

### 1. 运行测试（调试模式）

#### 方法一：右键菜单
```
1. 打开测试文件：ChargingOrderServiceTest.java
2. 右键点击测试方法（如 testPrePaidOrderLifecycle）
3. 选择 "Debug 'testPrePaidOrderLifecycle()'"
```

#### 方法二：快捷键
```
1. 光标定位到测试方法
2. 按快捷键：
   - macOS: Ctrl + Shift + D
   - Windows/Linux: Shift + F9
```

---

## 🔴 设置断点的最佳位置

### 位置 1: Service 层方法入口
```java
// ChargingOrderService.java

public ChargingOrder pay(ChargingOrder order, BigDecimal prepaidAmount) {
    validatePaymentType(order, PaymentType.PRE_PAID);  // ⬅️ 第一个断点
    
    order.setPrepaidAmount(prepaidAmount);
    OrderState newState = fireEvent(order, OrderEvent.PAY);  // ⬅️ 第二个断点
    order.setState(newState);
    // ...
}
```

**查看的变量：**
- `order` - 订单对象，查看当前状态、支付类型
- `prepaidAmount` - 预付金额
- `newState` - 状态转换后的新状态

---

### 位置 2: 状态机触发处
```java
// ChargingOrderService.java

private OrderState fireEvent(ChargingOrder order, OrderEvent event) {
    PaymentStrategy strategy = strategyFactory.getStrategy(order.getPaymentType());  // ⬅️ 断点
    
    StateMachine<OrderState, OrderEvent, ChargingOrder> stateMachine = 
        strategy.buildStateMachine();  // ⬅️ 断点
    
    OrderState newState = stateMachine.fireEvent(order.getState(), event, order);  // ⬅️ 断点
    
    return newState;
}
```

**查看的变量：**
- `strategy` - 当前使用的策略（PrePaidStrategy 或 PostPaidStrategy）
- `stateMachine` - 构建的状态机实例
- `order.getState()` - 当前状态
- `event` - 触发的事件
- `newState` - 新状态

---

### 位置 3: 策略类中的状态机构建
```java
// PrePaidStrategy.java

@Override
public StateMachine<OrderState, OrderEvent, ChargingOrder> buildStateMachine() {
    StateMachineBuilder<OrderState, OrderEvent, ChargingOrder> builder = 
        StateMachineBuilderFactory.create();  // ⬅️ 断点

    // CREATED -> PAID (支付)
    builder.externalTransition()
        .from(OrderState.CREATED)
        .to(OrderState.PAID)
        .on(OrderEvent.PAY)
        .when(checkPaymentCondition())  // ⬅️ 断点 - 检查条件
        .perform(doPaymentAction());    // ⬅️ 断点 - 执行动作
    
    return builder.build(MACHINE_ID);
}
```

---

## 🎮 调试控制快捷键

| 操作 | macOS | Windows/Linux | 说明 |
|------|-------|---------------|------|
| **调试运行** | `Ctrl + D` | `Shift + F9` | 以调试模式启动 |
| **单步跳过** | `F8` | `F8` | 执行当前行，不进入方法 |
| **单步进入** | `F7` | `F7` | 进入方法内部 |
| **单步跳出** | `Shift + F8` | `Shift + F8` | 跳出当前方法 |
| **恢复程序** | `Cmd + Option + R` | `F9` | 继续执行到下一个断点 |
| **计算表达式** | `Option + F8` | `Alt + F8` | 计算自定义表达式 |
| **查看断点** | `Cmd + Shift + F8` | `Ctrl + Shift + F8` | 查看所有断点 |
| **切换断点** | `Cmd + F8` | `Ctrl + F8` | 在当前行切换断点 |

---

## 🔍 高级调试技巧

### 1. 条件断点

**场景：** 只想在特定订单 ID 时暂停

```java
// 在断点上右键 → 添加条件
order.getOrderId().equals("specific-order-id")

// 或者只在状态为 CREATED 时暂停
order.getState() == OrderState.CREATED
```

**步骤：**
1. 在代码行左侧设置断点（红色圆点）
2. 右键点击断点
3. 输入条件表达式
4. 点击 "Done"

---

### 2. 日志断点（不暂停执行）

**场景：** 想记录执行流程但不想暂停程序

**步骤：**
1. 设置断点
2. 右键断点 → 取消勾选 "Suspend"
3. 勾选 "Evaluate and log"
4. 输入表达式，例如：
   ```
   "Order " + order.getOrderId() + " state: " + order.getState()
   ```

---

### 3. 监视表达式（Watches）

**在 Debug 面板添加：**

```java
// 添加到 Watches 面板
order.getOrderId()
order.getState()
order.getPaymentType()
order.getPrepaidAmount()
order.getOrderAmount()

// 复杂表达式
order.getPrepaidAmount().subtract(order.getOrderAmount())  // 计算退款
```

---

### 4. 方法断点

**场景：** 想在任何调用该方法时暂停

**步骤：**
1. 在方法签名行设置断点
2. 右键断点 → 勾选 "Method entry" 和 "Method exit"
3. 可以查看方法进入和退出时的状态

---

### 5. 异常断点

**场景：** 想在抛出特定异常时暂停

**步骤：**
1. Run → View Breakpoints (Cmd+Shift+F8)
2. 点击 "+" → Java Exception Breakpoints
3. 输入异常类名，如 `IllegalStateException`
4. 选择 "Any exception" 或 "Caught exception" 或 "Uncaught exception"

---

## 📊 调试面板使用

### Debug 工具窗口

```
┌─────────────────────────────────────────────────────┐
│  Debugger                                           │
├─────────────────────────────────────────────────────┤
│  ▶ Frames (调用栈)                                   │
│    └─ testPrePaidOrderLifecycle()                   │
│       └─ ChargingOrderService.pay()  ← 当前位置      │
│          └─ ChargingOrderService.fireEvent()        │
├─────────────────────────────────────────────────────┤
│  ▶ Variables (变量)                                  │
│    └─ this = ChargingOrderService@123               │
│    └─ order = ChargingOrder@456                     │
│       └─ orderId = "9233362f-8c5b-..."              │
│       └─ state = CREATED                            │
│       └─ paymentType = PRE_PAID                     │
│    └─ prepaidAmount = 100.00                        │
├─────────────────────────────────────────────────────┤
│  ▶ Watches (监视)                                    │
│    └─ order.getState() = CREATED                    │
│    └─ order.getPaymentType() = PRE_PAID             │
└─────────────────────────────────────────────────────┘
```

---

## 🧪 调试测试的典型流程

### 示例：调试 testPrePaidOrderLifecycle

```java
@Test
void testPrePaidOrderLifecycle() {
    // Step 1: 设置断点在这里 ⬅️
    ChargingOrder order = chargingOrderService.createOrder(
            "user123", "pile456", PaymentType.PRE_PAID);
    
    // Step 2: 设置断点在这里 ⬅️
    order = chargingOrderService.pay(order, new BigDecimal("100.00"));
    assertEquals(OrderState.PAID, order.getState());
    
    // Step 3: 按 F8 单步执行，观察状态变化
    order = chargingOrderService.startCharging(order);
    
    // Step 4: 按 F7 进入方法内部
    order = chargingOrderService.finishCharging(order,
            new BigDecimal("50.5"), new BigDecimal("60.00"));
    
    // Step 5: 在 Watches 中添加 order.getRefundAmount()
    order = chargingOrderService.settle(order);
    
    // 验证
    assertEquals(OrderState.CLOSED, order.getState());
}
```

**调试步骤：**

1. **设置断点**
   - 在 `createOrder` 调用处
   - 在 `pay` 调用处
   - 在 `ChargingOrderService.fireEvent` 方法中

2. **启动调试**
   - 右键测试方法 → Debug

3. **观察变量**
   - 在 Variables 面板查看 `order` 对象
   - 注意 `state` 字段的变化

4. **单步执行**
   - 按 F8 跳过当前行
   - 按 F7 进入 `pay` 方法内部
   - 观察状态从 CREATED → PAID 的转换

5. **检查状态机**
   - 进入 `fireEvent` 方法
   - 查看选择的策略是否正确（应该是 PrePaidStrategy）
   - 查看状态机转换的日志

---

## 🎨 调试视图定制

### 自定义对象显示

**创建 `toString()` 方法：**

```java
// ChargingOrder.java
@Override
public String toString() {
    return String.format("Order[id=%s, state=%s, type=%s, amount=%s]",
        orderId, state, paymentType, orderAmount);
}
```

这样在 Variables 面板中会显示更友好的信息。

---

## 📝 调试检查清单

### ✅ 开始调试前

- [ ] 确认代码已编译（Build → Build Project）
- [ ] 确认测试依赖已安装（mvn clean install）
- [ ] 设置好关键位置的断点
- [ ] 添加好监视表达式

### ✅ 调试过程中

- [ ] 检查变量值是否符合预期
- [ ] 观察调用栈确认执行路径
- [ ] 注意日志输出（Console 面板）
- [ ] 记录发现的问题

### ✅ 调试结束后

- [ ] 移除临时断点
- [ ] 清理测试代码中的调试输出
- [ ] 记录解决方案
- [ ] 运行完整测试套件验证

---

## 🚀 常见调试场景

### 场景 1: 状态转换不符合预期

**问题：** 调用 `pay()` 后状态没有变成 PAID

**调试步骤：**

1. 在 `ChargingOrderService.pay()` 设置断点
2. 单步执行到 `fireEvent(order, OrderEvent.PAY)`
3. **进入** `fireEvent` 方法（F7）
4. 检查 `strategy` 是否为 `PrePaidStrategy`
5. **进入** `strategy.buildStateMachine()`
6. 检查状态机是否定义了 `CREATED → PAID` 的转换
7. 检查 `when()` 条件是否返回 true
8. 查看 Console 中的状态机日志

---

### 场景 2: 跨策略调用异常

**问题：** 对 PostPaid 订单调用 `pay()` 方法

**调试步骤：**

1. 在 `validatePaymentType()` 设置断点
2. 查看 `order.getPaymentType()` 的值
3. 查看 `expectedType` 的值
4. 确认是否匹配

**预期结果：**
```
order.getPaymentType() = POST_PAID
expectedType = PRE_PAID
→ 抛出 IllegalStateException
```

---

### 场景 3: 金额计算错误

**问题：** 退款金额不正确

**调试步骤：**

1. 在 `ChargingOrderService.settle()` 设置断点
2. 在 Watches 添加：
   ```
   order.getPrepaidAmount()
   order.getOrderAmount()
   order.getPrepaidAmount().subtract(order.getOrderAmount())
   ```
3. 单步执行计算逻辑
4. 对比计算结果和预期值

---

## 💡 调试技巧总结

### ✅ DO

- ✅ 在方法入口和关键逻辑处设置断点
- ✅ 使用条件断点过滤特定场景
- ✅ 善用 Watches 监控关键变量
- ✅ 结合日志和断点一起使用
- ✅ 逐步缩小问题范围

### ❌ DON'T

- ❌ 设置过多断点导致效率低下
- ❌ 忽略 Console 中的日志信息
- ❌ 在不理解的情况下跳过代码
- ❌ 修改代码后不重新编译
- ❌ 调试完不清理临时代码

---

## 📚 相关资源

- [IntelliJ IDEA 官方调试指南](https://www.jetbrains.com/help/idea/debugging-code.html)
- [Java 调试技巧](https://www.baeldung.com/java-debugging-intellij)
- [COLA 状态机文档](https://github.com/alibaba/COLA)

---

**Happy Debugging! 🐛→✨**
