package com.tanhua.dubbo.api.mongo;

import com.tanhua.domain.mongo.RecommendUser;
import com.tanhua.domain.vo.PageResult;
import org.apache.commons.lang3.RandomUtils;
import org.apache.dubbo.config.annotation.Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;

import java.util.ArrayList;
import java.util.List;

@Service
public class RecommendUserApiImpl implements RecommendUserApi {

    @Autowired
    private MongoTemplate mongoTemplate;

    /**
     * 通过用户id 查询今日佳人
     *
     * @param loginUserId
     * @return
     */
    @Override
    public RecommendUser todayBest(Long loginUserId) {
        //1. 构建条件
        Query query = new Query();
        //2. toUser=loginUserId
        query.addCriteria(Criteria.where("toUserId").is(loginUserId));
        //3. 按缘分值降序
        query.with(Sort.by(Sort.Order.desc("score")));
        //4. 只查一条记录,取第一个
        RecommendUser todayBest = mongoTemplate.findOne(query, RecommendUser.class);
        return todayBest;
    }

    /**
     * 通过用户id查询推荐佳人分页查询
     *
     * @param loginUserId
     * @param page
     * @param pagesize
     * @return
     */
    @Override
    public PageResult findPage(Long loginUserId, Long page, Long pagesize) {
        //1. 构建查询条件
        Query query = new Query(Criteria.where("toUserId").is(loginUserId));
        //2. 统计总数
        long total = mongoTemplate.count(query, RecommendUser.class);
        //3. 总数>0才需要查结果集
        List<RecommendUser> recommendUserList = new ArrayList<>();
        if(total > 0) {
            //4. 设置分页
            query.skip((page-1)*pagesize).limit(pagesize.intValue());
            //5. 分页查询，得到分页结果集
            recommendUserList = mongoTemplate.find(query, RecommendUser.class);
        }
        //6. 构建pageResult
        return PageResult.pageResult(page,pagesize,recommendUserList,total);
    }

    /**
     * 查询登陆用户与佳人的缘分值
     *
     * @param loginUserId
     * @param userId
     * @return
     */
    @Override
    public Double queryForScore(Long loginUserId, Long userId) {
        //1. 构建查询条件
        Query query = new Query(Criteria.where("toUserId").is(loginUserId)
                .and("userId").is(userId));
        //2. 查询, 【注意】在推荐好友表，必须存在佳人记录
        RecommendUser recommendUser = mongoTemplate.findOne(query, RecommendUser.class);
        if(null == recommendUser){
            // 没有找到记录, recommendUser表中缺少记录，最好补上数据
            // 给个默认值
            return RandomUtils.nextDouble(60,80);
        }
        //3. 取出缘分值返回
        return recommendUser.getScore();
    }
}
