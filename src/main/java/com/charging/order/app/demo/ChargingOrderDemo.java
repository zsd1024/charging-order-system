package com.charging.order.app.demo;

import com.charging.order.app.service.ChargingOrderService;
import com.charging.order.domain.model.ChargingOrder;
import com.charging.order.domain.model.PaymentType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

/**
 * 充电订单使用示例
 * Charging Order Usage Demo
 * 
 * 展示如何使用双支付策略状态机处理订单流程
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ChargingOrderDemo {

    private final ChargingOrderService chargingOrderService;

    /**
     * 演示先付后充流程（PrePaid）
     * 
     * 场景：用户使用余额充电，先支付100元，实际充电花费60元，退款40元
     */
    public void demonstratePrePaidFlow() {
        log.info("========== 先付后充流程演示 ==========");

        // 1. 创建先付订单
        ChargingOrder order = chargingOrderService.createOrder(
                "user123",
                "pile456",
                PaymentType.PRE_PAID);
        log.info("Step 1: 创建订单 - 订单ID: {}, 状态: {}", order.getOrderId(), order.getState());

        // 2. 用户支付100元
        order = chargingOrderService.pay(order, new BigDecimal("100.00"));
        log.info("Step 2: 支付完成 - 预付金额: {}, 状态: {}", order.getPrepaidAmount(), order.getState());

        // 3. 开始充电
        order = chargingOrderService.startCharging(order);
        log.info("Step 3: 开始充电 - 状态: {}", order.getState());

        // 4. 充电完成（充了50.5度电，花费60元）
        order = chargingOrderService.finishCharging(order,
                new BigDecimal("50.5"),
                new BigDecimal("60.00"));
        log.info("Step 4: 充电完成 - 充电量: {} kWh, 实际费用: {}, 状态: {}",
                order.getChargingAmount(), order.getOrderAmount(), order.getState());

        // 5. 结算退款（退40元）
        order = chargingOrderService.settle(order);
        log.info("Step 5: 结算完成 - 退款金额: {}, 状态: {}", order.getRefundAmount(), order.getState());

        log.info("========== 先付后充流程完成 ==========\n");
    }

    /**
     * 演示后付费流程（PostPaid / 微信支付分）
     * 
     * 场景：用户使用微信支付分，先授权，充电后扣款55元
     */
    public void demonstratePostPaidFlow() {
        log.info("========== 后付费流程演示 ==========");

        // 1. 创建后付订单
        ChargingOrder order = chargingOrderService.createOrder(
                "user789",
                "pile012",
                PaymentType.POST_PAID);
        log.info("Step 1: 创建订单 - 订单ID: {}, 状态: {}", order.getOrderId(), order.getState());

        // 2. 微信支付分授权
        order = chargingOrderService.authorize(order);
        log.info("Step 2: 授权完成 - 状态: {}", order.getState());

        // 3. 开始充电
        order = chargingOrderService.startCharging(order);
        log.info("Step 3: 开始充电 - 状态: {}", order.getState());

        // 4. 充电完成（充了45.8度电，花费55元）
        order = chargingOrderService.finishCharging(order,
                new BigDecimal("45.8"),
                new BigDecimal("55.00"));
        log.info("Step 4: 充电完成 - 充电量: {} kWh, 订单金额: {}, 状态: {}",
                order.getChargingAmount(), order.getOrderAmount(), order.getState());

        // 5. 自动扣款
        order = chargingOrderService.deduct(order);
        log.info("Step 5: 扣款完成 - 实际扣款: {}, 状态: {}", order.getActualAmount(), order.getState());

        log.info("========== 后付费流程完成 ==========\n");
    }

    /**
     * 演示取消订单
     */
    public void demonstrateCancelOrder() {
        log.info("========== 取消订单演示 ==========");

        // 创建订单
        ChargingOrder order = chargingOrderService.createOrder(
                "user456",
                "pile789",
                PaymentType.PRE_PAID);
        log.info("创建订单 - 订单ID: {}, 状态: {}", order.getOrderId(), order.getState());

        // 取消订单
        order = chargingOrderService.cancelOrder(order);
        log.info("取消订单 - 状态: {}", order.getState());

        log.info("========== 取消订单完成 ==========\n");
    }
}
