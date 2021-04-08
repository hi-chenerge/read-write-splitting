package com.chenerge.save_after_read_consistent.city.common.session;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

/**
 * 某token对应的可读取master次数的操作类
 * 模拟了redis的原子自减操作
 */
@Service
public class CurrentUserReadMasterService {
    @Autowired
    private StringRedisTemplate redisTemplate;

    private static final String DEFAULT_READ_MASTER_CNT = "3";

    /**
     * 设置某个token的的读取master的次数
     * 在执行插入/更新/删除操作之后需要执行
     *
     * @param token
     */
    public void setCnt(String token) {
        String key = "read_master_cnt:" + token;
        redisTemplate.opsForValue().set(key, DEFAULT_READ_MASTER_CNT);
    }

    public void setCnt(String token, int readMasterCnt) {
        String key = "read_master_cnt:" + token;
        redisTemplate.opsForValue().set(key, String.valueOf(readMasterCnt));
    }

    /**
     * 得到某个token的读取master的次数减1
     * 如果返回的结果<=0, 则不用强制走master
     *
     * @param token
     * @return
     */
    public int getAndDecrementCnt(String token) {
        String key = "read_master_cnt:" + token;
        Long result = redisTemplate.opsForValue().increment(key, -1);

        return result.intValue();
    }
}
