package com.amalitech.todoApi.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.databind.jsontype.BasicPolymorphicTypeValidator;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import io.lettuce.core.ClientOptions;
import io.lettuce.core.SocketOptions;
import org.apache.commons.pool2.impl.GenericObjectPoolConfig;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.connection.lettuce.LettucePoolingClientConfiguration;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext.SerializationPair;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import java.time.Duration;
import java.util.Map;

@Configuration
@EnableCaching
@PropertySource(value = "classpath:application.yml", factory = YamlPropertySourceFactory.class)
public class CacheConfig {

    @Value("${spring.data.redis.host}")
    private String redisHost;

    @Value("${spring.data.redis.port}")
    private int redisPort;

    @Value("${spring.data.redis.timeout:2000}")
    private long commandTimeoutMs;

    @Value("${REDIS_SSL_ENABLED:false}")
    private boolean redisSslEnabled;

    @Value("${spring.data.redis.lettuce.pool.max-active:8}")
    private int poolMaxActive;

    @Value("${spring.data.redis.lettuce.pool.max-idle:8}")
    private int poolMaxIdle;

    @Value("${spring.data.redis.lettuce.pool.min-idle:2}")
    private int poolMinIdle;

    @Value("${spring.data.redis.lettuce.pool.max-wait:1000}")
    private long poolMaxWaitMs;

    @Value("${spring.cache.redis.time-to-live:60000}")
    private long defaultTtlMs;



    @Bean
    public RedisConnectionFactory redisConnectionFactory() {
        RedisStandaloneConfiguration server = new RedisStandaloneConfiguration(redisHost, redisPort);

        Duration commandTimeout = Duration.ofMillis(commandTimeoutMs);

        LettucePoolingClientConfiguration.LettucePoolingClientConfigurationBuilder builder =
                LettucePoolingClientConfiguration.builder()
                        .poolConfig(poolConfig())
                        .commandTimeout(commandTimeout)
                        .clientOptions(ClientOptions.builder()
                                .socketOptions(SocketOptions.builder()
                                        .connectTimeout(commandTimeout)
                                        .build())
                                .build());

        if (redisSslEnabled) {
            builder.useSsl().disablePeerVerification();
        }

        return new LettuceConnectionFactory(server, builder.build());
    }

    private GenericObjectPoolConfig<?> poolConfig() {
        GenericObjectPoolConfig<?> cfg = new GenericObjectPoolConfig<>();
        cfg.setMaxTotal(poolMaxActive);
        cfg.setMaxIdle(poolMaxIdle);
        cfg.setMinIdle(poolMinIdle);
        cfg.setMaxWait(Duration.ofMillis(poolMaxWaitMs));
        cfg.setTestOnBorrow(true);
        cfg.setTestWhileIdle(true);
        return cfg;
    }


    @Bean
    public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory connectionFactory) {
        StringRedisSerializer keySerializer = new StringRedisSerializer();
        GenericJackson2JsonRedisSerializer valueSerializer = jsonSerializer();

        RedisTemplate<String, Object> template = new RedisTemplate<>();
        template.setConnectionFactory(connectionFactory);
        template.setKeySerializer(keySerializer);
        template.setHashKeySerializer(keySerializer);
        template.setValueSerializer(valueSerializer);
        template.setHashValueSerializer(valueSerializer);
        template.afterPropertiesSet();
        return template;
    }



    @Bean
    public RedisCacheManager cacheManager(RedisConnectionFactory connectionFactory) {
        RedisCacheConfiguration base = RedisCacheConfiguration.defaultCacheConfig()
                .serializeKeysWith(SerializationPair.fromSerializer(new StringRedisSerializer()))
                .serializeValuesWith(SerializationPair.fromSerializer(jsonSerializer()))
                .disableCachingNullValues();

        return RedisCacheManager.builder(connectionFactory)
                .cacheDefaults(base.entryTtl(Duration.ofMillis(defaultTtlMs)))
                .withInitialCacheConfigurations(Map.of(
                        "todo",       base.entryTtl(Duration.ofMinutes(10)),
                        "categories", base.entryTtl(Duration.ofHours(1))
                ))
                .build();
    }



    private GenericJackson2JsonRedisSerializer jsonSerializer() {
        ObjectMapper mapper = JsonMapper.builder()
                .addModule(new JavaTimeModule())
                .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
                .activateDefaultTyping(
                        BasicPolymorphicTypeValidator.builder()
                                .allowIfSubType("com.amalitech.todoApi")
                                .allowIfSubType("java.util")
                                .allowIfSubType("java.time")
                                .allowIfSubType("org.hibernate.collection")
                                .build(),
                        ObjectMapper.DefaultTyping.NON_FINAL
                )
                .build();
        return new GenericJackson2JsonRedisSerializer(mapper);
    }
}