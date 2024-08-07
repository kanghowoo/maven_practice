package com.mide.gangsaeng.common.redis;

import java.time.Duration;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.lettuce.core.ClientOptions;
import io.lettuce.core.ClientOptions.DisconnectedBehavior;
import io.lettuce.core.RedisClient;
import io.lettuce.core.RedisURI;
import io.lettuce.core.api.StatefulRedisConnection;
import io.lettuce.core.api.sync.RedisCommands;
import jakarta.annotation.PreDestroy;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Configuration
public class LettuceConfig {
    private RedisClient redisClient;
    private StatefulRedisConnection<String, String> connection;

    @Bean
    public RedisCommands<String, String> syncCommands() {
        this.redisClient = RedisClient.create(getRedisURI());
        redisClient.setOptions(ClientOptions.builder()
                                            .disconnectedBehavior(
                                                    DisconnectedBehavior.REJECT_COMMANDS)
                                            .build());

        this.connection = redisClient.connect();
        return this.connection.sync();
    }

    @PreDestroy
    public void destroy() {
        if (connection != null) {
            connection.close();
        }

        if (redisClient != null) {
            redisClient.shutdown();
        }
    }

    private RedisURI getRedisURI() {
        return new RedisURI("localhost", 6379, Duration.ofSeconds(3));
    }
}
