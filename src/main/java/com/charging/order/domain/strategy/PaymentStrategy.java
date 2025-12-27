package com.charging.order.domain.strategy;

import com.alibaba.cola.statemachine.StateMachine;
import com.charging.order.domain.model.ChargingOrder;
import com.charging.order.domain.model.OrderEvent;
import com.charging.order.domain.model.OrderState;
import com.charging.order.domain.model.PaymentType;

/**
 * 支付策略接口
 * Payment Strategy Interface
 * 
 * 定义不同支付模式的策略契约，每种策略负责构建自己的状态机
 */
public interface PaymentStrategy {

    /**
     * 构建状态机
     * Build State Machine for this payment strategy
     * 
     * @return 配置好的状态机实例
     */
    StateMachine<OrderState, OrderEvent, ChargingOrder> buildStateMachine();

    /**
     * 获取状态机ID
     * Get State Machine ID
     * 
     * @return 状态机唯一标识
     */
    String getMachineId();

    /**
     * 获取支付类型
     * Get Payment Type
     * 
     * @return 支付类型枚举
     */
    PaymentType getPaymentType();
}
