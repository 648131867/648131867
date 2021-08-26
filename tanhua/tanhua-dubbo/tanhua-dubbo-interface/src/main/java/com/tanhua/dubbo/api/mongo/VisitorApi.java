package com.tanhua.dubbo.api.mongo;

import com.tanhua.domain.mongo.Visitor;
import com.tanhua.domain.vo.PageResult;

import java.util.List;

public interface VisitorApi {
    /**
     * 添加访客记录
     * @param visitor
     */
    void add(Visitor visitor);

    /**
     * 查询登陆用户最近访客，取前4位
     * @param userId
     * @return
     */
    List<Visitor> findLast4Visitors(Long userId);

    /**
     * 分页查询用户最近的访客
     * @param userId
     * @param page
     * @param pageSize
     * @return
     */
    PageResult findPageByUserId(Long userId, Long page, Long pageSize);
}
