package com.charging.order.domain.model;

/**
 * 支付类型枚举
 * Payment Type Enum
 */
public enum PaymentType {
    /**
     * 先付后充（先支付，充电后结算退款）
     */
    PRE_PAID,

    /**
     * 后付费/微信支付分（先授权，充电后扣款）
     */
    POST_PAID
}
