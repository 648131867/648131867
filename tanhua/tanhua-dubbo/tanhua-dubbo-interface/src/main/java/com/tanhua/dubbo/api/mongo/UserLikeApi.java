package com.tanhua.dubbo.api.mongo;

import com.tanhua.domain.vo.PageResult;

public interface UserLikeApi {
    /**
     * 统计用户喜欢的数量
     * @param loginUserId
     * @return
     */
    Long loveCount(Long loginUserId);

    /**
     * 统计用户的粉丝数量
     * @param loginUserId
     * @return
     */
    Long fanCount(Long loginUserId);

    /**
     * 分页查询登陆用户喜欢的佳人
     * @param userId
     * @param page
     * @param pageSize
     * @return
     */
    PageResult findPageOneSideLike(Long userId, Long page, Long pageSize);

    /**
     * 分页查询登陆用户的粉丝
     * @param userId
     * @param page
     * @param pageSize
     * @return
     */
    PageResult findPageFens(Long userId, Long page, Long pageSize);

    /**
     * 粉丝 喜欢
     * @param loginUserId
     * @param fensId
     * @return
     */
    boolean fansLike(Long loginUserId, Long fensId);
}
