package com.charging.order; // 确保包名对

import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
public class HelloController {

    @Autowired
    private JdbcTemplate jdbcTemplate; // 用于测试 MySQL

    @Autowired
    private StringRedisTemplate redisTemplate; // 用于测试 Redis

    @GetMapping("/test/db")
    public String testConnection() {
        // 1. 测试 MySQL
        // 简单的查询：查询当前数据库时间
        List<Map<String, Object>> result = jdbcTemplate.queryForList("SELECT NOW() as server_time");
        String mysqlTime = result.get(0).get("server_time").toString();

        // 2. 测试 Redis
        // 写入一个 key，再读出来
        redisTemplate.opsForValue().set("hello", "world from tencent cloud");
        String redisValue = redisTemplate.opsForValue().get("hello");

        return "🎉 验证成功！\n" +
                "MySQL 时间: " + mysqlTime + "\n" +
                "Redis 读取: " + redisValue;
    }
}