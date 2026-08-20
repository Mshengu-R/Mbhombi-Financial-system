package com.banking.frauddetectionservice.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import org.springframework.data.redis.core.RedisTemplate;


/**
 * Redis configuration - provides a RedisTemplate bean configured with String keys and JSON values.
 * This makes storing simple objects (OTP strings, small DTOs) safe and readable from other services.
 */
@Configuration
public class RedisConfig {

    @Bean
    public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory connectionFactory) {
      
        RedisTemplate<String, Object> template = new RedisTemplate<>();
        template.setConnectionFactory(connectionFactory);

        StringRedisSerializer stringSerializer = new StringRedisSerializer();

        GenericJackson2JsonRedisSerializer jsonSerializer = new GenericJackson2JsonRedisSerializer();

        // Use plain String serializer for keys
        template.setKeySerializer(stringSerializer);
        template.setHashKeySerializer(stringSerializer);

        // Use Jackson-based JSON serializer for values to make stored objects interoperable
        template.setValueSerializer(jsonSerializer);
        template.setHashValueSerializer(jsonSerializer);

       template.afterPropertiesSet();
        
       return template;
    }
    
}
