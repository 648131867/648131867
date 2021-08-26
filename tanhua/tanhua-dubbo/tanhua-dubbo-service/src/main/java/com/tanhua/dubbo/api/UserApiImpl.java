package com.tanhua.dubbo.api;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.tanhua.domain.db.User;
import com.tanhua.dubbo.mapper.UserMapper;
import org.apache.dubbo.config.annotation.Service;
import org.springframework.beans.factory.annotation.Autowired;

import javax.jws.soap.SOAPBinding;
import java.util.Date;

@Service
public class UserApiImpl implements UserApi {

    @Autowired
    private UserMapper userMapper;

    /**
     * 通过手机号码查询
     *
     * @param phone
     * @return
     */
    @Override
    public User findByMobile(String phone) {
        // 构建查询条件
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        // 手机号码相同
        queryWrapper.eq("mobile", phone);
        // 只查一条记录，如果有多条则报错
        return userMapper.selectOne(queryWrapper);
    }

    /**
     * 创建用户
     *
     * @param user
     */
    @Override
    public Long save(User user) {
        // 由mybatis plus自动填充处理了，注释掉
        //user.setCreated(new Date());
        //user.setUpdated(new Date());

        // 插入数据库
        userMapper.insert(user);
        return user.getId();
    }
}
