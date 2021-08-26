package com.tanhua.manage.service;

import cn.hutool.crypto.SecureUtil;
import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.tanhua.commons.exception.TanHuaException;
import com.tanhua.manage.domain.Admin;
import com.tanhua.manage.exception.BusinessException;
import com.tanhua.manage.mapper.AdminMapper;
import com.tanhua.manage.utils.JwtUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Service
@Slf4j
public class AdminService extends ServiceImpl<AdminMapper, Admin> {

    private static final String CACHE_KEY_CAP_PREFIX = "MANAGE_CAP_";
    public static final String CACHE_KEY_TOKEN_PREFIX="MANAGE_TOKEN_";

    @Autowired
    private RedisTemplate redisTemplate;

    @Autowired
    private JwtUtils jwtUtils;

    /**
     * 保存生成的验证码
     * @param uuid
     * @param code
     */
    public void saveCode(String uuid, String code) {
        String key = CACHE_KEY_CAP_PREFIX + uuid;
        // 缓存验证码，10分钟后失效
        redisTemplate.opsForValue().set(key,code, Duration.ofMinutes(10));
    }

    /**
     * 获取登陆用户信息
     * @return
     */
    public Admin getByToken(String authorization) {
        String token = authorization.replaceFirst("Bearer ","");
        String tokenKey = CACHE_KEY_TOKEN_PREFIX + token;
        String adminString = (String) redisTemplate.opsForValue().get(tokenKey);
        Admin admin = null;
        if(StringUtils.isNotEmpty(adminString)) {
            admin = JSON.parseObject(adminString, Admin.class);
            // 延长有效期 30分钟
            redisTemplate.expire(tokenKey,30, TimeUnit.MINUTES);
        }
        return admin;
    }

    /**
     * 登陆
     * @param paramMap
     * @return
     */
    public Map<String, String> login(Map<String, String> paramMap) {
        //1. 校验验证码
        String uuid = paramMap.get("uuid");
        String username = paramMap.get("username");
        String password = paramMap.get("password");
        //1.1 取redis中的验证码
        String key = CACHE_KEY_CAP_PREFIX + uuid;
        String codeInRedis = (String) redisTemplate.opsForValue().get(key);
        if(null == codeInRedis){
            throw new BusinessException("验证码已失效，请重新获取");
        }
        //1.2 比较提交过来与redis的验证码
        String verificationCode = paramMap.get("verificationCode");
        if(!StringUtils.equals(codeInRedis, verificationCode)){
            throw new BusinessException("验证码不正确，请重新输入!");
        }
        //1.3 验证码相同则删除redis中的key
        redisTemplate.delete(key);
        //2. 校验用户是否存在， 通过用户名查询
        Admin admin = query().eq("username", username).one();
        //2.1 不存在或密码不正确 则报错
        // 加密扣的密码
        password = SecureUtil.md5(password);
        if(null == admin || !StringUtils.equals(admin.getPassword(), password)){
            throw new BusinessException("用户名或密码错误");
        }
        //2.2 存在且密码正确 生成token
        String token = jwtUtils.createJWT(admin.getUsername(), admin.getId());
        // 用户信息存入redis，有效期30分钟
        String adminJsonString = JSON.toJSONString(admin);
        String tokenKey = CACHE_KEY_TOKEN_PREFIX + token;
        //3. 构建map<token>返回值
        redisTemplate.opsForValue().set(tokenKey, adminJsonString, 30, TimeUnit.MINUTES);
        Map<String,String> resultMap = new HashMap<>();
        resultMap.put("token", token);
        return resultMap;
    }

    /**
     * 退出登陆
     * @param token
     */
    public void logout(String token) {
        log.info("token:" + token);
        //Bearer eyJhbGciOiJIUzI1NiJ9.eyJpZCI6MSwiaWF0IjoxNjI3ODg2ODA4LCJ1c2VybmFtZSI6ImFkbWluIn0.2EZNif4Imr_nv_dSsXLHOwZ-0HFGyVo40IrJBwPEQeY
        token = token.replace("Bearer ", "");
        String tokenKey = CACHE_KEY_TOKEN_PREFIX + token;
        Boolean delete = redisTemplate.delete(tokenKey);
        log.info("delete result: " + delete);
    }
}
