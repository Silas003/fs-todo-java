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
    private Duration commandTimeout;

    @Value("${REDIS_SSL_ENABLED:false}")
    private boolean redisSslEnabled;



    @Value("${spring.data.redis.lettuce.pool.max-active:8}")
    private int poolMaxActive;

    @Value("${spring.data.redis.lettuce.pool.max-idle:8}")
    private int poolMaxIdle;

    @Value("${spring.data.redis.lettuce.pool.min-idle:2}")
    private int poolMinIdle;

    @Value("${spring.data.redis.lettuce.pool.max-wait:1000}")
    private Duration poolMaxWait;


    @Value("${spring.cache.redis.time-to-live:60000}")
    private long defaultTtlMs;


    @Bean
    public RedisConnectionFactory redisConnectionFactory() {
        RedisStandaloneConfiguration server = new RedisStandaloneConfiguration(redisHost, redisPort);

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
        cfg.setMaxWait(poolMaxWait);
        cfg.setTestOnBorrow(true);
        cfg.setTestWhileIdle(true);
        return cfg;
    }

    // ── RedisTemplate ────────────────────────────────────────────────────

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

    // ── Cache manager ────────────────────────────────────────────────────

    @Bean
    public RedisCacheManager cacheManager(RedisConnectionFactory connectionFactory) {
        RedisCacheConfiguration base = RedisCacheConfiguration.defaultCacheConfig()
                .serializeKeysWith(SerializationPair.fromSerializer(new StringRedisSerializer()))
                .serializeValuesWith(SerializationPair.fromSerializer(jsonSerializer()))
                .disableCachingNullValues();

        return RedisCacheManager.builder(connectionFactory)
                // Default TTL from spring.cache.redis.time-to-live
                .cacheDefaults(base.entryTtl(Duration.ofMillis(defaultTtlMs)))
                .withInitialCacheConfigurations(Map.of(
                        // Single todo lookups — view and edit pages
                        "todo",       base.entryTtl(Duration.ofMinutes(10)),
                        // Category list — used on every form; changes rarely
                        "categories", base.entryTtl(Duration.ofHours(1))
                ))
                .build();
    }

    // ── Jackson serializer ───────────────────────────────────────────────

    private GenericJackson2JsonRedisSerializer jsonSerializer() {
        ObjectMapper mapper = JsonMapper.builder()
                .addModule(new JavaTimeModule())
                .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
                .activateDefaultTyping(
                        BasicPolymorphicTypeValidator.builder()
                                .allowIfSubType("com.amalitech.todoApi")
                                .allowIfSubType("java.util")
                                .allowIfSubType("java.time")
                                .build(),
                        ObjectMapper.DefaultTyping.NON_FINAL
                )
                .build();
        return new GenericJackson2JsonRedisSerializer(mapper);
    }
}
