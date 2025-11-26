package glue.Gachi_Sanchaek.common.redis.service;

import java.time.Duration;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class RedisService {
    private final RedisTemplate<String, String> redisTemplate;

    public void save(String key, String value, Long ms) {
        redisTemplate.opsForValue().set(key, value, Duration.ofMillis(ms));
    }

    public String get(String key) {
        return redisTemplate.opsForValue().get(key);
    }
}