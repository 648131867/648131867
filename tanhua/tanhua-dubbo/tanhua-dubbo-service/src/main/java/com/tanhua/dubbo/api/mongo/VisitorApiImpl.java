package com.tanhua.dubbo.api.mongo;

import com.tanhua.domain.mongo.RecommendUser;
import com.tanhua.domain.mongo.Visitor;
import com.tanhua.domain.vo.PageResult;
import org.apache.commons.lang3.RandomUtils;
import org.apache.dubbo.config.annotation.Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class VisitorApiImpl implements VisitorApi {

    @Autowired
    private MongoTemplate mongoTemplate;
    /**
     * 添加访客记录
     *
     * @param visitor
     */
    @Override
    public void add(Visitor visitor) {
        mongoTemplate.insert(visitor);
    }

    /**
     * 查询登陆用户最近访客，取前4位
     *
     * @param loginUserId
     * @return
     */
    @Override
    public List<Visitor> findLast4Visitors(Long loginUserId) {
        //1. 构建查询的条件
        Query query = new Query(Criteria.where("userId").is(loginUserId));
        //2. 按时间降序
        query.with(Sort.by(Sort.Order.desc("date")));
        //3. 设置分页,取前4条
        query.limit(4);
        List<Visitor> visitorList = mongoTemplate.find(query, Visitor.class);
        // 缺少缘分值, 在RecommendUser表中 toUserId=登陆用户id, userId=访客的id
        if(!CollectionUtils.isEmpty(visitorList)){
            // 取出所有访客的id
            List<Long> visitorIds = visitorList.stream().map(Visitor::getVisitorUserId).collect(Collectors.toList());
            // 构建批量查询条件
            Query recommendUserQuery = new Query(Criteria.where("toUserId").is(loginUserId)
                    .and("userId").in(visitorIds));
            // 登陆用户与访客的缘分信息
            List<RecommendUser> recommendUserList = mongoTemplate.find(query, RecommendUser.class);
            // 把登陆用户与访客的缘分信息转成map
            Map<Long, Double> scoreMap = new HashMap<>();
            if(!CollectionUtils.isEmpty(recommendUserList)) {
                // 转成map key=访客id, value=缘分值
                scoreMap = recommendUserList.stream().collect(Collectors.toMap(RecommendUser::getUserId, RecommendUser::getScore));
            }
            // 设置访客的缘分值
            for (Visitor visitor : visitorList) {
                Double score = scoreMap.get(visitor.getVisitorUserId());
                if(null == score){
                    // 如果没有查询到数据，则设置随机缘分值
                    score = RandomUtils.nextDouble(60,80);
                }
                visitor.setScore(score);
            }
            /*final Map<Long, Double> scoreMap1 = scoreMap;
            visitorList.forEach(visitor -> {
                Double score = scoreMap1.get(visitor.getVisitorUserId());
                if(null == score){
                    score = RandomUtils.nextDouble(60,80);
                }
                visitor.setScore(score);
            });*/

        }
        //4. 返回结果
        return visitorList;
    }

    /**
     * 分页查询用户最近的访客
     *
     * @param loginUserId
     * @param page
     * @param pageSize
     * @return
     */
    @Override
    public PageResult findPageByUserId(Long loginUserId, Long page, Long pageSize) {
        //1. 构建查询的条件
        Query query = new Query(Criteria.where("userId").is(loginUserId));
        // 查询总数
        long total = mongoTemplate.count(query, Visitor.class);
        List<RecommendUser> recommendUserList = new ArrayList<>();
        if(total > 0) {
            //2. 按时间降序
            query.with(Sort.by(Sort.Order.desc("date")));
            //3. 设置分页
            query.skip((page-1)*pageSize).limit(pageSize.intValue());
            List<Visitor> visitorList = mongoTemplate.find(query, Visitor.class);
            // 缺少缘分值, 在RecommendUser表中 toUserId=登陆用户id, userId=访客的id
            if (!CollectionUtils.isEmpty(visitorList)) {
                // 取出所有访客的id
                List<Long> visitorIds = visitorList.stream().map(Visitor::getVisitorUserId).collect(Collectors.toList());
                // 构建批量查询条件
                Query recommendUserQuery = new Query(Criteria.where("toUserId").is(loginUserId)
                        .and("userId").in(visitorIds));
                // 登陆用户与访客的缘分信息
                recommendUserList = mongoTemplate.find(recommendUserQuery, RecommendUser.class);
            }
        }
        //4. 返回结果
        return PageResult.pageResult(page,pageSize,recommendUserList, total);
    }
}
