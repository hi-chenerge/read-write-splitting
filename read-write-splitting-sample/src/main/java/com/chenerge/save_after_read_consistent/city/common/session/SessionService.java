package com.chenerge.save_after_read_consistent.city.common.session;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;

@Component
public class SessionService {
    @Autowired
    private StringRedisTemplate redisTemplate;

    /**
     * 模拟添加2个用户的session
     */
    @PostConstruct
    public void initSession() {
        redisTemplate.opsForValue().set("1", "user1");
        redisTemplate.opsForValue().set("2", "user2");
    }

    /**
     * 根据token得到user
     *
     * @param token
     * @return
     */
    public String getUserByToken(String token) {
        return redisTemplate.opsForValue().get(token);
        //        Object userId = redisService.get(token);

        //        return userId != null ? (String) userId : null;
    }
}
