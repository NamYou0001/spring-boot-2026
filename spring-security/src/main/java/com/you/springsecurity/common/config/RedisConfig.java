package com.you.springsecurity.common.config;

import com.you.springsecurity.user.dto.response.PermissionResponse;
import com.you.springsecurity.user.dto.response.RoleResponse;

import lombok.RequiredArgsConstructor;

import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.serializer.JacksonJsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;

import tools.jackson.databind.JavaType;
import tools.jackson.databind.ObjectMapper;

import java.time.Duration;
import java.util.List;
import java.util.Map;

@Configuration
@EnableCaching
@RequiredArgsConstructor
public class RedisConfig {

    private final RedisConnectionFactory redisConnectionFactory;

    @Bean
    public ObjectMapper redisObjectMapper() {
        return new ObjectMapper();
    }

    @Bean
    public CacheManager cacheManager(ObjectMapper redisObjectMapper) {

        // Default Cache Configuration
        RedisCacheConfiguration defaultConfig = RedisCacheConfiguration.defaultCacheConfig().entryTtl(Duration.ofMinutes(30)).disableCachingNullValues();

        // Permission List Type
        JavaType permissionListType = redisObjectMapper.getTypeFactory().constructCollectionType(List.class, PermissionResponse.class);

        JacksonJsonRedisSerializer<List<PermissionResponse>> permissionSerializer = new JacksonJsonRedisSerializer<>(redisObjectMapper, permissionListType);

        // Role List Type
        JavaType roleListType = redisObjectMapper.getTypeFactory().constructCollectionType(List.class, RoleResponse.class);

        JacksonJsonRedisSerializer<List<RoleResponse>> roleSerializer = new JacksonJsonRedisSerializer<>(redisObjectMapper, roleListType);

        // Permission Cache
        RedisCacheConfiguration permissionConfig = defaultConfig.serializeValuesWith(RedisSerializationContext.SerializationPair.fromSerializer(permissionSerializer));

        // Role Cache
        RedisCacheConfiguration roleConfig = defaultConfig.serializeValuesWith(RedisSerializationContext.SerializationPair.fromSerializer(roleSerializer));

        return RedisCacheManager.builder(redisConnectionFactory).cacheDefaults(defaultConfig).withInitialCacheConfigurations(Map.of("permissions", permissionConfig, "roles", roleConfig)).build();
    }
}
