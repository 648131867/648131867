package com.tanhua.dubbo.api;

import com.tanhua.domain.vo.PageResult;

public interface BlackListApi {
    /**
     * 通过登陆用户id，分页查询登陆用户的黑名单列表
     * @param userId
     * @param page
     * @param pageSize
     * @return
     */
    PageResult findPageByUserId(Long userId, Long page, Long pageSize);

    /**
     * 移除黑名单
     * @param loginUserId
     * @param blackUserId
     */
    void delete(Long loginUserId, Long blackUserId);
}
