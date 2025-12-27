package com.charging.order.adapter.web;

import com.charging.order.app.service.ChargingOrderService;
import com.charging.order.domain.model.ChargingOrder;
import com.charging.order.domain.model.PaymentType;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;

/**
 * 充电订单控制器
 * Charging Order Controller
 */
@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
public class ChargingOrderController {

    private final ChargingOrderService chargingOrderService;

    /**
     * 创建订单
     */
    @PostMapping
    public ResponseEntity<ChargingOrder> createOrder(
            @RequestParam String userId,
            @RequestParam String chargingPileId,
            @RequestParam PaymentType paymentType) {

        System.out.println("Pro日志】收到下单请求，用户ID: \" + userId");

        ChargingOrder order = chargingOrderService.createOrder(userId, chargingPileId, paymentType);
        return ResponseEntity.ok(order);
    }

    /**
     * 支付（先付模式）
     */
    @PostMapping("/{orderId}/pay")
    public ResponseEntity<ChargingOrder> pay(
            @PathVariable String orderId,
            @RequestBody ChargingOrder order,
            @RequestParam BigDecimal prepaidAmount) {
        ChargingOrder updatedOrder = chargingOrderService.pay(order, prepaidAmount);
        return ResponseEntity.ok(updatedOrder);
    }

    /**
     * 授权（后付模式）
     */
    @PostMapping("/{orderId}/authorize")
    public ResponseEntity<ChargingOrder> authorize(
            @PathVariable String orderId,
            @RequestBody ChargingOrder order) {
        ChargingOrder updatedOrder = chargingOrderService.authorize(order);
        return ResponseEntity.ok(updatedOrder);
    }

    /**
     * 开始充电
     */
    @PostMapping("/{orderId}/start")
    public ResponseEntity<ChargingOrder> startCharging(
            @PathVariable String orderId,
            @RequestBody ChargingOrder order) {
        ChargingOrder updatedOrder = chargingOrderService.startCharging(order);
        return ResponseEntity.ok(updatedOrder);
    }

    /**
     * 完成充电
     */
    @PostMapping("/{orderId}/finish")
    public ResponseEntity<ChargingOrder> finishCharging(
            @PathVariable String orderId,
            @RequestBody ChargingOrder order,
            @RequestParam BigDecimal chargingAmount,
            @RequestParam BigDecimal orderAmount) {
        ChargingOrder updatedOrder = chargingOrderService.finishCharging(order, chargingAmount, orderAmount);
        return ResponseEntity.ok(updatedOrder);
    }

    /**
     * 结算退款（先付模式）
     */
    @PostMapping("/{orderId}/settle")
    public ResponseEntity<ChargingOrder> settle(
            @PathVariable String orderId,
            @RequestBody ChargingOrder order) {
        ChargingOrder updatedOrder = chargingOrderService.settle(order);
        return ResponseEntity.ok(updatedOrder);
    }

    /**
     * 扣款（后付模式）
     */
    @PostMapping("/{orderId}/deduct")
    public ResponseEntity<ChargingOrder> deduct(
            @PathVariable String orderId,
            @RequestBody ChargingOrder order) {
        ChargingOrder updatedOrder = chargingOrderService.deduct(order);
        return ResponseEntity.ok(updatedOrder);
    }

    /**
     * 取消订单
     */
    @PostMapping("/{orderId}/cancel")
    public ResponseEntity<ChargingOrder> cancelOrder(
            @PathVariable String orderId,
            @RequestBody ChargingOrder order) {
        ChargingOrder updatedOrder = chargingOrderService.cancelOrder(order);
        return ResponseEntity.ok(updatedOrder);
    }
}
