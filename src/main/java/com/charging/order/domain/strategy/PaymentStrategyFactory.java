package com.charging.order.domain.strategy;

import com.charging.order.domain.model.PaymentType;
import org.springframework.stereotype.Component;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;

/**
 * 支付策略工厂
 * Payment Strategy Factory
 * 
 * 根据支付类型返回对应的支付策略实例
 * 使用 Spring 依赖注入自动管理所有策略实例
 */
@Component
public class PaymentStrategyFactory {

    private final Map<PaymentType, PaymentStrategy> strategyMap;

    /**
     * 构造函数注入所有策略实例
     * Spring 会自动注入所有 PaymentStrategy 类型的 Bean
     * 
     * @param strategies 所有支付策略实例列表
     */
    public PaymentStrategyFactory(List<PaymentStrategy> strategies) {
        this.strategyMap = new EnumMap<>(PaymentType.class);

        // 将策略按照支付类型分类存储
        for (PaymentStrategy strategy : strategies) {
            strategyMap.put(strategy.getPaymentType(), strategy);
        }
    }

    /**
     * 根据支付类型获取对应的策略
     * Get strategy by payment type
     * 
     * @param paymentType 支付类型
     * @return 对应的支付策略
     * @throws IllegalArgumentException 如果支付类型不支持
     */
    public PaymentStrategy getStrategy(PaymentType paymentType) {
        PaymentStrategy strategy = strategyMap.get(paymentType);

        if (strategy == null) {
            throw new IllegalArgumentException(
                    "Unsupported payment type: " + paymentType);
        }

        return strategy;
    }

    /**
     * 检查是否支持指定的支付类型
     * Check if payment type is supported
     * 
     * @param paymentType 支付类型
     * @return true if supported, false otherwise
     */
    public boolean isSupported(PaymentType paymentType) {
        return strategyMap.containsKey(paymentType);
    }
}
