package com.tanhua.dubbo.api.mongo;

import com.tanhua.domain.db.Question;
import com.tanhua.domain.mongo.Friend;
import com.tanhua.domain.mongo.RecommendUser;
import com.tanhua.domain.vo.PageResult;
import org.apache.dubbo.config.annotation.Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class FriendApiImpl implements FriendApi {

    @Autowired
    private MongoTemplate mongoTemplate;

    /**
     * 交友
     */
    @Override
    public void makeFriends(Long loginUserId, Long friendId) {
        //1. 添加好友 (登陆用户 与 佳人)。
        long timeMillis = System.currentTimeMillis();
        Friend friend = new Friend();
        friend.setUserId(loginUserId);
        friend.setFriendId(friendId);
        friend.setCreated(timeMillis);
        // 判断不是好友
        Query query = new Query(Criteria.where("userId").is(loginUserId)
        .and("friendId").is(friendId));
        if (!mongoTemplate.exists(query, Friend.class)) {
            // 添加为好
            mongoTemplate.insert(friend);
        }
        //2. 添加好友 (佳人 与 登陆用户)。
        // 判断对方是否已经加我为好友了
        Query queryFriend = new Query(Criteria.where("userId").is(friendId)
                .and("friendId").is(loginUserId));
        if (!mongoTemplate.exists(queryFriend, Friend.class)) {
            friend = new Friend();
            friend.setUserId(friendId);
            friend.setFriendId(loginUserId);
            friend.setCreated(timeMillis);
            // 添加为好
            mongoTemplate.insert(friend);
        }
    }

    /**
     * 联系人分页查询
     *
     * @param loginUserId
     * @param page
     * @param pageSize
     * @return
     */
    @Override
    public PageResult findPage(Long loginUserId, Long page, Long pageSize) {
        //1. 查User_friend, 条件?
        // 构建查询条件
        Query query = new Query(Criteria.where("userId").is(loginUserId));
        //2. 统计总数
        long total = mongoTemplate.count(query, Friend.class);
        List<Friend> friendList = new ArrayList<>();
        //3. 总数>0分页查询
        if(total > 0) {
            //4. 设置分页参数
            query.skip((page-1) * pageSize).limit(pageSize.intValue());
            //5. 按时间降序
            query.with(Sort.by(Sort.Order.desc("created")));
            //6. 查询获取结果集
            friendList = mongoTemplate.find(query, Friend.class);
        }
        //7. 构建pageResult返回
        return PageResult.pageResult(page,pageSize,friendList, total);
    }

    /**
     * 通过用户id统计好友数量
     *
     * @param loginUserId
     * @return
     */
    @Override
    public Long countByUserId(Long loginUserId) {
        Query query = new Query(Criteria.where("userId").is(loginUserId));
        return mongoTemplate.count(query, Friend.class);
    }

    /**
     * 好友分页查询 加上缘分
     *
     * @param loginUserId
     * @param page
     * @param pageSize
     * @return
     */
    @Override
    public PageResult findPageWithScore(Long loginUserId, Long page, Long pageSize) {
        //1. 查User_friend, 条件?
        // 构建查询条件
        Query query = new Query(Criteria.where("userId").is(loginUserId));
        //2. 统计总数
        long total = mongoTemplate.count(query, Friend.class);
        List<RecommendUser> recommendUserList = new ArrayList<>();
        //3. 总数>0分页查询
        if(total > 0) {
            //4. 设置分页参数
            query.skip((page-1) * pageSize).limit(pageSize.intValue());
            //5. 按时间降序
            query.with(Sort.by(Sort.Order.desc("created")));
            //6. 查询获取结果集
            List<Friend> friendList = mongoTemplate.find(query, Friend.class);
            //7. 查询登陆用户与好友之间的缘分值
            //7.0 取出所有好友的ids
            List<Long> friendIds = friendList.stream().map(Friend::getFriendId).collect(Collectors.toList());
            //7.1 构建查询推荐表的条件
            Query recommendUserQuery = new Query(Criteria.where("toUserId").is(loginUserId)
                    .and("userId").in(friendIds));
            //7.2 查询
            recommendUserList = mongoTemplate.find(recommendUserQuery, RecommendUser.class);
        }
        //7. 构建pageResult返回
        return PageResult.pageResult(page,pageSize,recommendUserList, total);
    }
}
