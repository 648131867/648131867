package com.tanhua.dubbo.api;

import com.tanhua.domain.db.UserInfo;

import java.util.List;

public interface UserInfoApi {
    /**
     * 用户注册添加信息
     * @param userInfo
     */
    void add(UserInfo userInfo);

    /**
     * 更新用户头像
     * @param userInfo
     */
    void update(UserInfo userInfo);

    /**
     * 通过is查询用户详情
     * @param loginUserId
     * @return
     */
    UserInfo findById(Long loginUserId);

    /**
     * 批量查询用户详情
     * @param blackUserIds
     * @return
     */
    List<UserInfo> findByBatchId(List<Long> blackUserIds);
}
