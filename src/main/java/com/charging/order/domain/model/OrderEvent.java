package com.charging.order.domain.model;

/**
 * 订单事件枚举
 * Order Event Enum
 */
public enum OrderEvent {
    /**
     * 支付（先付模式）
     */
    PAY,

    /**
     * 授权（后付模式）
     */
    AUTHORIZE,

    /**
     * 开始充电
     */
    START_CHARGING,

    /**
     * 完成充电
     */
    FINISH_CHARGING,

    /**
     * 结算退款（先付模式）
     */
    SETTLE,

    /**
     * 扣款（后付模式）
     */
    DEDUCT,

    /**
     * 取消订单
     */
    CANCEL_ORDER
}
