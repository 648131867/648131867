package com.tanhua.dubbo.api.mongo;

import com.tanhua.domain.mongo.FollowUser;
import com.tanhua.domain.mongo.Video;
import com.tanhua.domain.vo.PageResult;

public interface VideoApi {
    /**
     * 发布小视频
     * @param video
     */
    void add(Video video);

    /**
     * 小视频列表分页查询
     * @param page
     * @param pageSize
     * @return
     */
    PageResult findPage(Long page, Long pageSize);

    /**
     * 关注视频的作者
     * @param followUser
     */
    void followUser(FollowUser followUser);

    /**
     * 取消关注视频的作者
     * @param followUser
     */
    void unfollowUser(FollowUser followUser);
}
