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
 * 后付费策略 / 微信支付分策略 (PostPaid Strategy)
 * 
 * 业务流程：
 * 1. 用户下单（CREATED）
 * 2. 先授权/锁定 (AUTHORIZE) -> AUTHORIZED
 * 3. 开始充电 (START_CHARGING) -> CHARGING
 * 4. 充电结束 (FINISH_CHARGING) -> COMPLETED
 * 5. 自动扣款 (DEDUCT) -> CLOSED
 * 
 * 特点：先免密授权，充电，最后扣款
 */
@Slf4j
@Component
public class PostPaidStrategy implements PaymentStrategy {

    private static final String MACHINE_ID = "CHARGING_POSTPAID";

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
                    log.info("PostPaid state machine [{}] initialized", MACHINE_ID);
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

        // 1. CREATED -> AUTHORIZED (授权)
        builder.externalTransition()
                .from(OrderState.CREATED)
                .to(OrderState.AUTHORIZED)
                .on(OrderEvent.AUTHORIZE)
                .when(checkAuthorizationCondition())
                .perform(doAuthorizeAction());

        // 2. AUTHORIZED -> CHARGING (开始充电)
        builder.externalTransition()
                .from(OrderState.AUTHORIZED)
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

        // 4. COMPLETED -> CLOSED (扣款)
        builder.externalTransition()
                .from(OrderState.COMPLETED)
                .to(OrderState.CLOSED)
                .on(OrderEvent.DEDUCT)
                .when(checkCondition())
                .perform(doDeductAction());

        // 5. CREATED -> CANCELLED (取消订单 - 未授权时可取消)
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
        return PaymentType.POST_PAID;
    }

    /**
     * 检查授权条件
     */
    private com.alibaba.cola.statemachine.Condition<ChargingOrder> checkAuthorizationCondition() {
        return (order) -> {
            // 可以添加业务条件检查：例如用户信用分是否足够、是否开通支付分服务等
            log.debug("Checking authorization condition for order: {}", order.getOrderId());
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
     * 执行授权动作
     */
    private com.alibaba.cola.statemachine.Action<OrderState, OrderEvent, ChargingOrder> doAuthorizeAction() {
        return (from, to, event, order) -> {
            log.info("[PostPaid] Order [{}] authorization completed: {} -> {} on event {}",
                    order.getOrderId(), from, to, event);
            // 这里可以添加实际的授权逻辑：调用微信支付分API、锁定信用额度等
        };
    }

    /**
     * 执行开始充电动作
     */
    private com.alibaba.cola.statemachine.Action<OrderState, OrderEvent, ChargingOrder> doStartChargingAction() {
        return (from, to, event, order) -> {
            log.info("[PostPaid] Order [{}] charging started: {} -> {} on event {}",
                    order.getOrderId(), from, to, event);
            // 这里可以添加实际的充电逻辑：通知充电桩、开始计量等
        };
    }

    /**
     * 执行完成充电动作
     */
    private com.alibaba.cola.statemachine.Action<OrderState, OrderEvent, ChargingOrder> doFinishChargingAction() {
        return (from, to, event, order) -> {
            log.info("[PostPaid] Order [{}] charging finished: {} -> {} on event {}. Amount: {} kWh",
                    order.getOrderId(), from, to, event, order.getChargingAmount());
            // 这里可以添加实际的充电结束逻辑：停止计量、计算费用等
        };
    }

    /**
     * 执行扣款动作
     */
    private com.alibaba.cola.statemachine.Action<OrderState, OrderEvent, ChargingOrder> doDeductAction() {
        return (from, to, event, order) -> {
            log.info("[PostPaid] Order [{}] deduction completed: {} -> {} on event {}. Amount: {}",
                    order.getOrderId(), from, to, event, order.getActualAmount());
            // 这里可以添加实际的扣款逻辑：调用扣款API、释放授权、记录账单等
        };
    }

    /**
     * 执行取消订单动作
     */
    private com.alibaba.cola.statemachine.Action<OrderState, OrderEvent, ChargingOrder> doCancelAction() {
        return (from, to, event, order) -> {
            log.info("[PostPaid] Order [{}] cancelled: {} -> {} on event {}",
                    order.getOrderId(), from, to, event);
            // 这里可以添加实际的取消逻辑：释放资源、通知用户等
        };
    }
}
