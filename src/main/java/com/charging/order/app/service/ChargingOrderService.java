package com.charging.order.app.service;

import com.alibaba.cola.statemachine.StateMachine;
import com.charging.order.domain.model.ChargingOrder;
import com.charging.order.domain.model.OrderEvent;
import com.charging.order.domain.model.OrderState;
import com.charging.order.domain.model.PaymentType;
import com.charging.order.domain.strategy.PaymentStrategy;
import com.charging.order.domain.strategy.PaymentStrategyFactory;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * 充电订单服务
 * Charging Order Service
 * 
 * 基于策略模式和状态机管理订单生命周期
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ChargingOrderService {

    private final PaymentStrategyFactory strategyFactory;

    /**
     * 创建订单
     * Create Order
     * 
     * @param userId         用户ID
     * @param chargingPileId 充电桩ID
     * @param paymentType    支付类型
     * @return 创建的订单
     */
    public ChargingOrder createOrder(String userId, String chargingPileId, PaymentType paymentType) {
        ChargingOrder order = new ChargingOrder();
        order.setOrderId(UUID.randomUUID().toString());
        order.setUserId(userId);
        order.setChargingPileId(chargingPileId);
        order.setPaymentType(paymentType);
        order.setState(OrderState.CREATED);
        order.setChargingAmount(BigDecimal.ZERO);
        order.setOrderAmount(BigDecimal.ZERO);
        order.setCreateTime(LocalDateTime.now());
        order.setUpdateTime(LocalDateTime.now());

        log.info("Created {} order: {}", paymentType, order.getOrderId());
        return order;
    }

    // ==================== PrePaid 模式专用方法 ====================

    /**
     * 支付（先付模式）
     * Pay for PrePaid mode
     * 
     * @param order         订单
     * @param prepaidAmount 预付金额
     * @return 更新后的订单
     */
    public ChargingOrder pay(ChargingOrder order, BigDecimal prepaidAmount) {
        validatePaymentType(order, PaymentType.PRE_PAID);

        order.setPrepaidAmount(prepaidAmount);
        OrderState newState = fireEvent(order, OrderEvent.PAY);
        order.setState(newState);
        order.setUpdateTime(LocalDateTime.now());

        log.info("Order {} paid with amount: {}", order.getOrderId(), prepaidAmount);
        return order;
    }

    /**
     * 结算退款（先付模式）
     * Settle and refund for PrePaid mode
     * 
     * @param order 订单
     * @return 更新后的订单
     */
    public ChargingOrder settle(ChargingOrder order) {
        validatePaymentType(order, PaymentType.PRE_PAID);

        // 计算退款金额 = 预付金额 - 实际订单金额
        BigDecimal refund = order.getPrepaidAmount().subtract(order.getOrderAmount());
        order.setRefundAmount(refund);

        OrderState newState = fireEvent(order, OrderEvent.SETTLE);
        order.setState(newState);
        order.setUpdateTime(LocalDateTime.now());

        log.info("Order {} settled. Refund amount: {}", order.getOrderId(), refund);
        return order;
    }

    // ==================== PostPaid 模式专用方法 ====================

    /**
     * 授权（后付模式）
     * Authorize for PostPaid mode
     * 
     * @param order 订单
     * @return 更新后的订单
     */
    public ChargingOrder authorize(ChargingOrder order) {
        validatePaymentType(order, PaymentType.POST_PAID);

        OrderState newState = fireEvent(order, OrderEvent.AUTHORIZE);
        order.setState(newState);
        order.setUpdateTime(LocalDateTime.now());

        log.info("Order {} authorized", order.getOrderId());
        return order;
    }

    /**
     * 扣款（后付模式）
     * Deduct for PostPaid mode
     * 
     * @param order 订单
     * @return 更新后的订单
     */
    public ChargingOrder deduct(ChargingOrder order) {
        validatePaymentType(order, PaymentType.POST_PAID);

        // 实际扣款金额 = 订单金额
        order.setActualAmount(order.getOrderAmount());

        OrderState newState = fireEvent(order, OrderEvent.DEDUCT);
        order.setState(newState);
        order.setUpdateTime(LocalDateTime.now());

        log.info("Order {} deducted. Amount: {}", order.getOrderId(), order.getActualAmount());
        return order;
    }

    // ==================== 通用方法（两种模式都支持） ====================

    /**
     * 开始充电
     * Start Charging
     * 
     * @param order 订单
     * @return 更新后的订单
     */
    public ChargingOrder startCharging(ChargingOrder order) {
        OrderState newState = fireEvent(order, OrderEvent.START_CHARGING);
        order.setState(newState);
        order.setUpdateTime(LocalDateTime.now());

        log.info("Order {} started charging", order.getOrderId());
        return order;
    }

    /**
     * 完成充电
     * Finish Charging
     * 
     * @param order          订单
     * @param chargingAmount 充电量 (kWh)
     * @param orderAmount    订单金额
     * @return 更新后的订单
     */
    public ChargingOrder finishCharging(ChargingOrder order, BigDecimal chargingAmount, BigDecimal orderAmount) {
        order.setChargingAmount(chargingAmount);
        order.setOrderAmount(orderAmount);

        OrderState newState = fireEvent(order, OrderEvent.FINISH_CHARGING);
        order.setState(newState);
        order.setUpdateTime(LocalDateTime.now());

        log.info("Order {} finished charging. Amount: {} kWh, Cost: {}",
                order.getOrderId(), chargingAmount, orderAmount);
        return order;
    }

    /**
     * 取消订单
     * Cancel Order
     * 
     * @param order 订单
     * @return 更新后的订单
     */
    public ChargingOrder cancelOrder(ChargingOrder order) {
        OrderState newState = fireEvent(order, OrderEvent.CANCEL_ORDER);
        order.setState(newState);
        order.setUpdateTime(LocalDateTime.now());

        log.info("Order {} cancelled", order.getOrderId());
        return order;
    }

    // ==================== 私有辅助方法 ====================

    /**
     * 触发状态机事件
     * Fire state machine event
     * 
     * @param order 订单
     * @param event 事件
     * @return 新状态
     */
    private OrderState fireEvent(ChargingOrder order, OrderEvent event) {
        // 1. 获取对应的策略
        PaymentStrategy strategy = strategyFactory.getStrategy(order.getPaymentType());

        // 2. 构建状态机（每次都重新构建，确保状态机的独立性）
        StateMachine<OrderState, OrderEvent, ChargingOrder> stateMachine = strategy.buildStateMachine();

        // 3. 触发事件
        OrderState newState = stateMachine.fireEvent(order.getState(), event, order);

        log.debug("State machine [{}] fired event {} for order {}: {} -> {}",
                strategy.getMachineId(), event, order.getOrderId(), order.getState(), newState);

        return newState;
    }

    /**
     * 验证支付类型
     * Validate payment type
     * 
     * @param order        订单
     * @param expectedType 期望的支付类型
     * @throws IllegalStateException 如果支付类型不匹配
     */
    private void validatePaymentType(ChargingOrder order, PaymentType expectedType) {
        if (order.getPaymentType() != expectedType) {
            throw new IllegalStateException(
                    String.format("Order %s is %s mode, cannot perform %s mode operation",
                            order.getOrderId(), order.getPaymentType(), expectedType));
        }
    }
}
