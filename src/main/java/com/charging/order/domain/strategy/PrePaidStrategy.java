package com.charging.order.domain.strategy;

import com.alibaba.cola.statemachine.StateMachine;
import com.alibaba.cola.statemachine.builder.StateMachineBuilder;
import com.alibaba.cola.statemachine.builder.StateMachineBuilderFactory;
import com.charging.order.domain.model.ChargingOrder;
import com.charging.order.domain.model.OrderEvent;
import com.charging.order.domain.model.OrderState;
import com.charging.order.domain.model.PaymentType;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * 先付后充策略 (PrePaid Strategy)
 * 
 * 业务流程：
 * 1. 用户下单（CREATED）
 * 2. 先支付 (PAY) -> PAID
 * 3. 开始充电 (START_CHARGING) -> CHARGING
 * 4. 充电结束 (FINISH_CHARGING) -> COMPLETED
 * 5. 结算退差价 (SETTLE) -> CLOSED
 * 
 * 特点：先扣款，后充电，最后退款
 */
@Slf4j
@Component
public class PrePaidStrategy implements PaymentStrategy {

    private static final String MACHINE_ID = "CHARGING_PREPAID";

    /**
     * 状态机实例（单例，懒加载）
     */
    private StateMachine<OrderState, OrderEvent, ChargingOrder> stateMachine;

    @Override
    public StateMachine<OrderState, OrderEvent, ChargingOrder> buildStateMachine() {
        // 使用双重检查锁定实现懒加载单例，避免重复构建
        if (stateMachine == null) {
            synchronized (this) {
                if (stateMachine == null) {
                    stateMachine = createStateMachine();
                    log.info("PrePaid state machine [{}] initialized", MACHINE_ID);
                }
            }
        }
        return stateMachine;
    }

    /**
     * 创建状态机（私有方法，仅初始化时调用一次）
     */
    private StateMachine<OrderState, OrderEvent, ChargingOrder> createStateMachine() {
        StateMachineBuilder<OrderState, OrderEvent, ChargingOrder> builder = StateMachineBuilderFactory.create();

        // 1. CREATED -> PAID (支付)
        builder.externalTransition()
                .from(OrderState.CREATED)
                .to(OrderState.PAID)
                .on(OrderEvent.PAY)
                .when(checkPaymentCondition())
                .perform(doPayAction());

        // 2. PAID -> CHARGING (开始充电)
        builder.externalTransition()
                .from(OrderState.PAID)
                .to(OrderState.CHARGING)
                .on(OrderEvent.START_CHARGING)
                .when(checkCondition())
                .perform(doStartChargingAction());

        // 3. CHARGING -> COMPLETED (完成充电)
        builder.externalTransition()
                .from(OrderState.CHARGING)
                .to(OrderState.COMPLETED)
                .on(OrderEvent.FINISH_CHARGING)
                .when(checkCondition())
                .perform(doFinishChargingAction());

        // 4. COMPLETED -> CLOSED (结算退款)
        builder.externalTransition()
                .from(OrderState.COMPLETED)
                .to(OrderState.CLOSED)
                .on(OrderEvent.SETTLE)
                .when(checkCondition())
                .perform(doSettleAction());

        // 5. CREATED -> CANCELLED (取消订单 - 未支付时可取消)
        builder.externalTransition()
                .from(OrderState.CREATED)
                .to(OrderState.CANCELLED)
                .on(OrderEvent.CANCEL_ORDER)
                .when(checkCondition())
                .perform(doCancelAction());

        return builder.build(MACHINE_ID);
    }

    @Override
    public String getMachineId() {
        return MACHINE_ID;
    }

    @Override
    public PaymentType getPaymentType() {
        return PaymentType.PRE_PAID;
    }

    /**
     * 检查支付条件
     */
    private com.alibaba.cola.statemachine.Condition<ChargingOrder> checkPaymentCondition() {
        return (order) -> {
            // 可以添加业务条件检查：例如用户余额是否充足、订单是否有效等
            log.debug("Checking payment condition for order: {}", order.getOrderId());
            return true;
        };
    }

    /**
     * 通用条件检查
     */
    private com.alibaba.cola.statemachine.Condition<ChargingOrder> checkCondition() {
        return (order) -> {
            // 这里可以添加业务条件检查
            return true;
        };
    }

    /**
     * 执行支付动作
     */
    private com.alibaba.cola.statemachine.Action<OrderState, OrderEvent, ChargingOrder> doPayAction() {
        return (from, to, event, order) -> {
            log.info("[PrePaid] Order [{}] payment completed: {} -> {} on event {}",
                    order.getOrderId(), from, to, event);
            // 这里可以添加实际的支付逻辑：调用支付网关、扣款、记录流水等
        };
    }

    /**
     * 执行开始充电动作
     */
    private com.alibaba.cola.statemachine.Action<OrderState, OrderEvent, ChargingOrder> doStartChargingAction() {
        return (from, to, event, order) -> {
            log.info("[PrePaid] Order [{}] charging started: {} -> {} on event {}",
                    order.getOrderId(), from, to, event);
            // 这里可以添加实际的充电逻辑：通知充电桩、开始计量等
        };
    }

    /**
     * 执行完成充电动作
     */
    private com.alibaba.cola.statemachine.Action<OrderState, OrderEvent, ChargingOrder> doFinishChargingAction() {
        return (from, to, event, order) -> {
            log.info("[PrePaid] Order [{}] charging finished: {} -> {} on event {}. Amount: {} kWh",
                    order.getOrderId(), from, to, event, order.getChargingAmount());
            // 这里可以添加实际的充电结束逻辑：停止计量、计算费用等
        };
    }

    /**
     * 执行结算退款动作
     */
    private com.alibaba.cola.statemachine.Action<OrderState, OrderEvent, ChargingOrder> doSettleAction() {
        return (from, to, event, order) -> {
            log.info("[PrePaid] Order [{}] settlement completed: {} -> {} on event {}. Refund: {}",
                    order.getOrderId(), from, to, event, order.getRefundAmount());
            // 这里可以添加实际的结算逻辑：计算退款、原路退回、记录账单等
        };
    }

    /**
     * 执行取消订单动作
     */
    private com.alibaba.cola.statemachine.Action<OrderState, OrderEvent, ChargingOrder> doCancelAction() {
        return (from, to, event, order) -> {
            log.info("[PrePaid] Order [{}] cancelled: {} -> {} on event {}",
                    order.getOrderId(), from, to, event);
            // 这里可以添加实际的取消逻辑：释放资源、通知用户等
        };
    }
}
