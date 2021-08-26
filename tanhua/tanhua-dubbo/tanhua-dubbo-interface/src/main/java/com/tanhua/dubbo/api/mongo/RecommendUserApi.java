package com.tanhua.dubbo.api.mongo;

import com.tanhua.domain.mongo.RecommendUser;
import com.tanhua.domain.vo.PageResult;

public interface RecommendUserApi {
    /**
     * 通过用户id 查询今日佳人
     * @param loginUserId
     * @return
     */
    RecommendUser todayBest(Long loginUserId);

    /**
     * 通过用户id查询推荐佳人分页查询
     * @param loginUserId
     * @param page
     * @param pagesize
     * @return
     */
    PageResult findPage(Long loginUserId, Long page, Long pagesize);

    /**
     * 查询登陆用户与佳人的缘分值
     * @param loginUserId
     * @param userId
     * @return
     */
    Double queryForScore(Long loginUserId, Long userId);
}
