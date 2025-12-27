package com.charging.order;

import com.charging.order.app.service.ChargingOrderService;
import com.charging.order.domain.model.ChargingOrder;
import com.charging.order.domain.model.OrderState;
import com.charging.order.domain.model.PaymentType;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 充电订单服务测试
 * Charging Order Service Test
 * 
 * 测试双支付策略的完整流程
 */
@SpringBootTest
class ChargingOrderServiceTest {

    @Autowired
    private ChargingOrderService chargingOrderService;

    /**
     * 测试先付后充（PrePaid）完整生命周期
     * 
     * 流程：创建 -> 支付 -> 充电 -> 结束 -> 结算退款 -> 关闭
     */
    @Test
    void testPrePaidOrderLifecycle() {
        // 1. 创建先付订单
        ChargingOrder order = chargingOrderService.createOrder(
                "user123", "pile456", PaymentType.PRE_PAID);
        assertNotNull(order);
        assertEquals(OrderState.CREATED, order.getState());
        assertEquals(PaymentType.PRE_PAID, order.getPaymentType());

        // 2. 支付（预付100元）
        order = chargingOrderService.pay(order, new BigDecimal("100.00"));
        assertEquals(OrderState.PAID, order.getState());
        assertEquals(new BigDecimal("100.00"), order.getPrepaidAmount());

        // 3. 开始充电
        order = chargingOrderService.startCharging(order);
        assertEquals(OrderState.CHARGING, order.getState());

        // 4. 完成充电（实际充了50.5度电，花费60元）
        order = chargingOrderService.finishCharging(order,
                new BigDecimal("50.5"), new BigDecimal("60.00"));
        assertEquals(OrderState.COMPLETED, order.getState());
        assertEquals(new BigDecimal("50.5"), order.getChargingAmount());
        assertEquals(new BigDecimal("60.00"), order.getOrderAmount());

        // 5. 结算退款（应该退40元）
        order = chargingOrderService.settle(order);
        assertEquals(OrderState.CLOSED, order.getState());
        assertEquals(new BigDecimal("40.00"), order.getRefundAmount());
    }

    /**
     * 测试后付费（PostPaid）完整生命周期
     * 
     * 流程：创建 -> 授权 -> 充电 -> 结束 -> 扣款 -> 关闭
     */
    @Test
    void testPostPaidOrderLifecycle() {
        // 1. 创建后付订单
        ChargingOrder order = chargingOrderService.createOrder(
                "user456", "pile789", PaymentType.POST_PAID);
        assertNotNull(order);
        assertEquals(OrderState.CREATED, order.getState());
        assertEquals(PaymentType.POST_PAID, order.getPaymentType());

        // 2. 授权（微信支付分）
        order = chargingOrderService.authorize(order);
        assertEquals(OrderState.AUTHORIZED, order.getState());

        // 3. 开始充电
        order = chargingOrderService.startCharging(order);
        assertEquals(OrderState.CHARGING, order.getState());

        // 4. 完成充电（充了45.8度电，花费55元）
        order = chargingOrderService.finishCharging(order,
                new BigDecimal("45.8"), new BigDecimal("55.00"));
        assertEquals(OrderState.COMPLETED, order.getState());
        assertEquals(new BigDecimal("45.8"), order.getChargingAmount());
        assertEquals(new BigDecimal("55.00"), order.getOrderAmount());

        // 5. 扣款（实际扣款55元）
        order = chargingOrderService.deduct(order);
        assertEquals(OrderState.CLOSED, order.getState());
        assertEquals(new BigDecimal("55.00"), order.getActualAmount());
    }

    /**
     * 测试取消先付订单
     * 
     * 流程：创建 -> 取消
     */
    @Test
    void testCancelPrePaidOrder() {
        // 1. 创建先付订单
        ChargingOrder order = chargingOrderService.createOrder(
                "user111", "pile222", PaymentType.PRE_PAID);
        assertEquals(OrderState.CREATED, order.getState());

        // 2. 取消订单（未支付前可以取消）
        order = chargingOrderService.cancelOrder(order);
        assertEquals(OrderState.CANCELLED, order.getState());
    }

    /**
     * 测试取消后付订单
     * 
     * 流程：创建 -> 取消
     */
    @Test
    void testCancelPostPaidOrder() {
        // 1. 创建后付订单
        ChargingOrder order = chargingOrderService.createOrder(
                "user333", "pile444", PaymentType.POST_PAID);
        assertEquals(OrderState.CREATED, order.getState());

        // 2. 取消订单（未授权前可以取消）
        order = chargingOrderService.cancelOrder(order);
        assertEquals(OrderState.CANCELLED, order.getState());
    }

    /**
     * 测试跨策略调用验证
     * 
     * 验证：先付订单不能调用后付方法，反之亦然
     */
    @Test
    void testCrossStrategyValidation() {
        // 1. 创建先付订单
        ChargingOrder prePaidOrder = chargingOrderService.createOrder(
                "user555", "pile666", PaymentType.PRE_PAID);

        // 2. 尝试对先付订单调用后付方法 - 应该抛出异常
        assertThrows(IllegalStateException.class, () -> {
            chargingOrderService.authorize(prePaidOrder);
        });

        // 3. 创建后付订单
        ChargingOrder postPaidOrder = chargingOrderService.createOrder(
                "user777", "pile888", PaymentType.POST_PAID);

        // 4. 尝试对后付订单调用先付方法 - 应该抛出异常
        assertThrows(IllegalStateException.class, () -> {
            chargingOrderService.pay(postPaidOrder, new BigDecimal("100.00"));
        });
    }
}
