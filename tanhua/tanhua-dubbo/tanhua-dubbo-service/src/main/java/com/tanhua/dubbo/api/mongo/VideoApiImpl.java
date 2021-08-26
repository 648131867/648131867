package com.tanhua.dubbo.api.mongo;

import com.tanhua.domain.mongo.FollowUser;
import com.tanhua.domain.mongo.Video;
import com.tanhua.domain.vo.PageResult;
import com.tanhua.domain.vo.VideoVo;
import org.apache.dubbo.config.annotation.Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;

import java.util.ArrayList;
import java.util.List;

@Service
public class VideoApiImpl implements VideoApi {

    @Autowired
    private MongoTemplate mongoTemplate;

    /**
     * 发布小视频
     *
     * @param video
     */
    @Override
    public void add(Video video) {
        video.setCreated(System.currentTimeMillis());
        mongoTemplate.insert(video);
    }

    /**
     * 小视频列表分页查询
     *
     * @param page
     * @param pageSize
     * @return
     */
    @Override
    public PageResult findPage(Long page, Long pageSize) {
        //1. 构建查询条件
        Query query = new Query();
        //2. 统计总数
        long total = mongoTemplate.count(query, Video.class);
        //3. 总数>0
        List<Video> videoList = new ArrayList<>();
        if(total > 0) {
            //4. 设置分页
            query.skip((page-1)*pageSize).limit(pageSize.intValue());
            //5. 按创建的时间降序
            query.with(Sort.by(Sort.Order.desc("created")));
            //6. 分页查询
            videoList = mongoTemplate.find(query, Video.class);
        }
        //7. 构建PageResult并返回
        return PageResult.pageResult(page, pageSize, videoList, total);
    }

    /**
     * 关注视频的作者
     *
     * @param followUser
     */
    @Override
    public void followUser(FollowUser followUser) {
        followUser.setCreated(System.currentTimeMillis());
        mongoTemplate.insert(followUser);
    }

    /**
     * 取消关注视频的作者
     *
     * @param followUser
     */
    @Override
    public void unfollowUser(FollowUser followUser) {
        //1. 构建删除的条件
        Query query = new Query(Criteria.where("userId").is(followUser.getUserId())
                .and("followUserId").is(followUser.getFollowUserId()));
        //2. 删除
        mongoTemplate.remove(query, FollowUser.class);
    }
}
