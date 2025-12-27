package com.charging.order.domain.model;

/**
 * 订单状态枚举
 * Order State Enum
 */
public enum OrderState {
    /**
     * 已创建
     */
    CREATED,

    /**
     * 已支付（先付模式）
     */
    PAID,

    /**
     * 已授权（后付模式）
     */
    AUTHORIZED,

    /**
     * 充电中
     */
    CHARGING,

    /**
     * 充电完成
     */
    COMPLETED,

    /**
     * 已取消
     */
    CANCELLED,

    /**
     * 已关闭
     */
    CLOSED
}
