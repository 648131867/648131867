package com.tanhua.dubbo.api.mongo;

import com.tanhua.domain.mongo.Publish;
import com.tanhua.domain.vo.PageResult;

public interface PublishApi {
    /**
     * 发布动态
     * @param publish
     */
    String add(Publish publish);

    /**
     * 陆用户id查询好友动态的数据
     * @param loginUserId
     * @param page
     * @param pageSize
     * @return
     */
    PageResult findFriendPublishByTimeline(Long loginUserId, Long page, Long pageSize);

    /**
     * 分页查询推荐动态
     * @param loginUserId
     * @param page
     * @param pageSize
     * @return
     */
    PageResult findRecommendPublish(Long loginUserId, Long page, Long pageSize);

    /**
     * 查询某个用户的动态
     * @param loginUserId
     * @param page
     * @param pageSize
     * @return
     */
    PageResult findMyPublishList(Long loginUserId, Long page, Long pageSize);

    /**
     * 通过id 查询单条动态
     * @param publishId
     * @return
     */
    Publish findById(String publishId);

    /**
     * 更新动态的状态
     * @param publishId
     * @param i
     */
    void updateState(String publishId, int i);
}
