package com.tanhua.dubbo.api;

import com.tanhua.domain.db.UserInfo;
import com.tanhua.dubbo.mapper.UserInfoMapper;
import org.apache.dubbo.config.annotation.Service;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

@Service
public class UserInfoApiImpl implements UserInfoApi {

    @Autowired
    private UserInfoMapper userInfoMapper;

    /**
     * 用户注册添加信息
     *
     * @param userInfo
     */
    @Override
    public void add(UserInfo userInfo) {
        userInfoMapper.insert(userInfo);
    }

    /**
     * 更新用户头像
     *
     * @param userInfo
     */
    @Override
    public void update(UserInfo userInfo) {
        // updateById(对象) update 对象对应的表 set 字段名(属性值不为null)=值 where id=?
        userInfoMapper.updateById(userInfo);
    }

    /**
     * 通过is查询用户详情
     *
     * @param loginUserId
     * @return
     */
    @Override
    public UserInfo findById(Long loginUserId) {
        return userInfoMapper.selectById(loginUserId);
    }

    /**
     * 批量查询用户详情
     *
     * @param blackUserIds
     * @return
     */
    @Override
    public List<UserInfo> findByBatchId(List<Long> blackUserIds) {
        return userInfoMapper.selectBatchIds(blackUserIds);
    }
}
