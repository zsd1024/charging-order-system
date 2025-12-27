package com.charging.order.infrastructure.persistence;

import com.charging.order.domain.model.ChargingOrder;
import org.springframework.stereotype.Repository;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 充电订单仓储实现（内存版本）
 * Charging Order Repository (In-Memory Implementation)
 * 
 * 注：这是一个简单的内存实现，生产环境应该使用数据库
 */
@Repository
public class ChargingOrderRepository {

    private final Map<String, ChargingOrder> orderStore = new ConcurrentHashMap<>();

    /**
     * 保存订单
     */
    public ChargingOrder save(ChargingOrder order) {
        orderStore.put(order.getOrderId(), order);
        return order;
    }

    /**
     * 根据ID查询订单
     */
    public ChargingOrder findById(String orderId) {
        return orderStore.get(orderId);
    }

    /**
     * 删除订单
     */
    public void delete(String orderId) {
        orderStore.remove(orderId);
    }

    /**
     * 检查订单是否存在
     */
    public boolean exists(String orderId) {
        return orderStore.containsKey(orderId);
    }
}
