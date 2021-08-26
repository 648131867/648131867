package com.tanhua.dubbo.api;

import com.tanhua.domain.db.User;

public interface UserApi {
    /**
     * 通过手机号码查询
     * @param phone
     * @return
     */
    User findByMobile(String phone);

    /**
     * 创建用户
     * @param user
     */
    Long save(User user);
}
