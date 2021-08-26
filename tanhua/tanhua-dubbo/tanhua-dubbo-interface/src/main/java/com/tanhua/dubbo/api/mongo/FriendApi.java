package com.tanhua.dubbo.api.mongo;

import com.tanhua.domain.mongo.Friend;
import com.tanhua.domain.vo.PageResult;

public interface FriendApi {
    /**
     * 交友
     */
    void makeFriends(Long loginUserId, Long friendId);

    /**
     * 联系人分页查询
     * @param userId
     * @param page
     * @param pageSize
     * @return
     */
    PageResult findPage(Long userId, Long page, Long pageSize);

    /**
     * 通过用户id统计好友数量
     * @param loginUserId
     * @return
     */
    Long countByUserId(Long loginUserId);

    /**
     * 好友分页查询 加上缘分
     * @param userId
     * @param page
     * @param pageSize
     * @return
     */
    PageResult findPageWithScore(Long userId, Long page, Long pageSize);
}
