package com.tanhua.dubbo.api.mongo;

import com.tanhua.domain.mongo.RecommendUser;
import com.tanhua.domain.mongo.UserLike;
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
public class UserLikeApiImpl implements UserLikeApi {

    @Autowired
    private MongoTemplate mongoTemplate;

    @Autowired
    private FriendApi friendApi;

    /**
     * 统计用户喜欢的数量
     *
     * @param loginUserId
     * @return
     */
    @Override
    public Long loveCount(Long loginUserId) {
        Query query = new Query(Criteria.where("userId").is(loginUserId));
        return mongoTemplate.count(query, UserLike.class);
    }

    /**
     * 统计用户的粉丝数量
     *
     * @param loginUserId
     * @return
     */
    @Override
    public Long fanCount(Long loginUserId) {
        // 我被别喜欢
        Query query = new Query(Criteria.where("likeUserId").is(loginUserId));
        return mongoTemplate.count(query, UserLike.class);
    }

    /**
     * 分页查询登陆用户喜欢的佳人
     *
     * @param loginUserId
     * @param page
     * @param pageSize
     * @return
     */
    @Override
    public PageResult findPageOneSideLike(Long loginUserId, Long page, Long pageSize) {
        //1. 构建UserLike查询的条件
        Query userLikeQuery = new Query(Criteria.where("userId").is(loginUserId));
        //2. 统计总数
        long total = mongoTemplate.count(userLikeQuery, UserLike.class);
        //3. 总数>0
        List<RecommendUser> recommendUserList = new ArrayList<>();
        if(total > 0) {
            //4. 设置分页
            userLikeQuery.skip((page-1)*pageSize).limit(pageSize.intValue());
            //5. 设置降序
            userLikeQuery.with(Sort.by(Sort.Order.desc("created")));
            //6. 查询结果集
            List<UserLike> userLikeList = mongoTemplate.find(userLikeQuery, UserLike.class);
            //7. 查询登陆用户与这些佳人的缘分值
            //7.1 获取所有的佳人ids
            List<Long> userLikeIds = userLikeList.stream().map(UserLike::getLikeUserId).collect(Collectors.toList());
            //7.2 构建推荐用户表查询条件
            Query recommendUserQuery = new Query(Criteria.where("toUserId").is(loginUserId)
                    .and("userId").in(userLikeIds));
            //7.3 查询推荐表数据
            recommendUserList = mongoTemplate.find(recommendUserQuery, RecommendUser.class);
        }
        //8 构建pageResult返回
        return PageResult.pageResult(page,pageSize,recommendUserList,total);
    }

    /**
     * 分页查询登陆用户的粉丝
     *
     * @param loginUserId
     * @param page
     * @param pageSize
     * @return
     */
    @Override
    public PageResult findPageFens(Long loginUserId, Long page, Long pageSize) {
        //1. 构建UserLike查询的条件, 登陆用户是被喜欢
        Query userLikeQuery = new Query(Criteria.where("likeUserId").is(loginUserId));
        //2. 统计总数
        long total = mongoTemplate.count(userLikeQuery, UserLike.class);
        //3. 总数>0
        List<RecommendUser> recommendUserList = new ArrayList<>();
        if(total > 0) {
            //4. 设置分页
            userLikeQuery.skip((page-1)*pageSize).limit(pageSize.intValue());
            //5. 设置降序
            userLikeQuery.with(Sort.by(Sort.Order.desc("created")));
            //6. 查询结果集
            List<UserLike> fensList = mongoTemplate.find(userLikeQuery, UserLike.class);
            //7. 查询登陆用户与这些粉丝的缘分值
            //7.1 获取所有的粉丝ids
            List<Long> fensIds = fensList.stream().map(UserLike::getUserId).collect(Collectors.toList());
            //7.2 构建推荐用户表查询条件
            Query recommendUserQuery = new Query(Criteria.where("toUserId").is(loginUserId)
                    .and("userId").in(fensIds));
            //7.3 查询推荐表数据
            recommendUserList = mongoTemplate.find(recommendUserQuery, RecommendUser.class);
        }
        //8 构建pageResult返回
        return PageResult.pageResult(page,pageSize,recommendUserList,total);
    }

    /**
     * 粉丝 喜欢
     *
     * @param loginUserId
     * @param fensId
     * @return
     */
    @Override
    public boolean fansLike(Long loginUserId, Long fensId) {
        //1. 先判断粉丝是否真的喜欢我
        Query query = new Query(Criteria.where("likeUserId").is(loginUserId).and("userId").is(fensId));
        if(mongoTemplate.exists(query, UserLike.class)) {
            //2. 如果她还是喜欢我，则要删除这条粉丝记录，添加互为好友
            mongoTemplate.remove(query, UserLike.class);
            // 添加互为好友
            friendApi.makeFriends(loginUserId, fensId);
            return true;
        }else {
            //3. 如果不喜欢我了，则要添加我喜欢她记录
            UserLike userLike = new UserLike();
            userLike.setUserId(loginUserId);
            userLike.setLikeUserId(fensId);
            userLike.setCreated(System.currentTimeMillis());
            mongoTemplate.insert(userLike);
        }
        return false;
    }
}
