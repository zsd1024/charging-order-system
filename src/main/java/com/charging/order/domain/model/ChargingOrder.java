package com.charging.order.domain.model;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 充电订单领域模型
 * Charging Order Domain Model
 */
@Data
public class ChargingOrder {

    /**
     * 订单ID
     */
    private String orderId;

    /**
     * 用户ID
     */
    private String userId;

    /**
     * 充电桩ID
     */
    private String chargingPileId;

    /**
     * 订单状态
     */
    private OrderState state;

    /**
     * 充电量 (kWh)
     */
    private BigDecimal chargingAmount;

    /**
     * 订单金额
     */
    private BigDecimal orderAmount;

    /**
     * 支付类型
     */
    private PaymentType paymentType;

    /**
     * 预付金额（先付模式）
     */
    private BigDecimal prepaidAmount;

    /**
     * 实际扣款金额（后付模式）
     */
    private BigDecimal actualAmount;

    /**
     * 退款金额（先付模式）
     */
    private BigDecimal refundAmount;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    private LocalDateTime updateTime;
}
