package com.tanhua.server.service;

import com.alibaba.fastjson.JSON;
import com.tanhua.commons.templates.HuanXinTemplate;
import com.tanhua.domain.db.Question;
import com.tanhua.domain.db.User;
import com.tanhua.domain.db.UserInfo;
import com.tanhua.domain.mongo.RecommendUser;
import com.tanhua.domain.vo.*;
import com.tanhua.dubbo.api.QuestionApi;
import com.tanhua.dubbo.api.UserInfoApi;
import com.tanhua.dubbo.api.mongo.RecommendUserApi;
import com.tanhua.dubbo.api.mongo.UserLocationApi;
import com.tanhua.server.interceptor.UserHolder;
import org.apache.commons.lang3.RandomUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.dubbo.config.annotation.Reference;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 交友业务
 */
@Service
public class RecommendUserService {

    @Reference
    private RecommendUserApi recommendUserApi;

    @Reference
    private UserInfoApi userInfoApi;

    @Reference
    private QuestionApi questionApi;

    @Reference
    private UserLocationApi userLocationApi;

    @Autowired
    private HuanXinTemplate huanXinTemplate;

    /**
     * 查询今日佳人
     * @return
     */
    public RecommendUserVo todayBest() {
        //1. 获取登陆用户id
        Long loginUserId = UserHolder.getUserId();
        //2. 调用api查询今日佳人
        RecommendUser todayBest = recommendUserApi.todayBest(loginUserId);
        // 没有推荐用户
        if(null == todayBest){
            // 使用客服
            todayBest = new RecommendUser();
            todayBest.setUserId(RandomUtils.nextLong(1,99)); // 1-99 都是客服
            todayBest.setScore(RandomUtils.nextDouble(60,80)); // 随机缘分值
        }
        //3. 通过佳人id查询佳人详情
        UserInfo userInfo = userInfoApi.findById(todayBest.getUserId());
        //4. 构建vo
        RecommendUserVo vo = new RecommendUserVo();
        // 复制属性
        BeanUtils.copyProperties(userInfo, vo);
        // tags处理
        vo.setTags(StringUtils.split(userInfo.getTags(), ","));
        // 缘分值
        vo.setFateValue(todayBest.getScore().longValue());
        //5. 返回
        return vo;
    }

    /**
     * 首页好友推荐
     * @param queryParam
     * @return
     */
    public PageResult<RecommendUserVo> recommendList(RecommendUserQueryParam queryParam) {
        //1. 获取登陆用户id
        Long loginUserId = UserHolder.getUserId();
        //2. 调用api分页查询给登陆用户推荐的佳人列表
        PageResult pageResult = recommendUserApi.findPage(loginUserId, queryParam.getPage(), queryParam.getPagesize());
        //3. 取出分页的结果集
        List<RecommendUser> recommendUserList = pageResult.getItems();
        //4. 没有推荐数据，给默认客服
        if(CollectionUtils.isEmpty(recommendUserList)){
            //给默认客服作为推荐好友
            recommendUserList = getDefaultRecommendUsers();
            pageResult.setPages(1l);
            pageResult.setCounts(10l);
        }
        //5. 获取佳人的id集合
        List<Long> userIds = recommendUserList.stream().map(RecommendUser::getUserId).collect(Collectors.toList());
        //6. 批量查询佳人信息
        List<UserInfo> userInfoList = userInfoApi.findByBatchId(userIds);
        //7. 把佳人信息list转成map<key=佳人的id, value=userInfo>
        Map<Long, UserInfo> userInfoMap = userInfoList.stream().collect(Collectors.toMap(UserInfo::getId, u -> u));
        //8. 转成vo
        List<RecommendUserVo> voList = recommendUserList.stream().map(recommendUser -> {
            RecommendUserVo vo = new RecommendUserVo();
            // 佳人的详情
            UserInfo userInfo = userInfoMap.get(recommendUser.getUserId());
            // 复制属性
            BeanUtils.copyProperties(userInfo, vo);
            // tags处理
            vo.setTags(StringUtils.split(userInfo.getTags(), ","));
            // 缘分值
            vo.setFateValue(recommendUser.getScore().longValue());
            return vo;
        }).collect(Collectors.toList());
        //9. 设置回pageResult
        pageResult.setItems(voList);
        //10. 返回
        return pageResult;
    }

    /**
     * 给默认的推荐用户
     * @return
     */
    private List<RecommendUser> getDefaultRecommendUsers() {
        List<RecommendUser> list = new ArrayList<>();
        for (long i = 1; i <= 10; i++) {
            RecommendUser recommendUser = new RecommendUser();
            recommendUser.setUserId(i);
            recommendUser.setScore(RandomUtils.nextDouble(60,80));
            list.add(recommendUser);
        }
        return list;
    }

    /**
     * 查看佳人信息
     * @param userId
     * @return
     */
    public RecommendUserVo getPersonalInfo(Long userId) {
        //1. 查询登陆用户与佳人的缘分值
        Long loginUserId = UserHolder.getUserId();
        Double score = recommendUserApi.queryForScore(loginUserId, userId);
        //2. 查询佳人的详情
        UserInfo userInfo = userInfoApi.findById(userId);
        //3. 转vo
        RecommendUserVo vo = new RecommendUserVo();
        // 复制属性
        BeanUtils.copyProperties(userInfo, vo);
        // tags处理
        vo.setTags(StringUtils.split(userInfo.getTags(), ","));
        // 缘分值
        vo.setFateValue(score.longValue());
        //4. 返回
        return vo;
    }

    /**
     * 查看佳人陌生人问题
     * @param userId
     * @return
     */
    public String strangerQuestions(Long userId) {
        //1. 调用api，通过userId查询佳人的陌生人问题设置
        Question question = questionApi.findByUserId(userId);
        //2. 没有设置陌生人问题，给默认值
        if(null == question){
            return "你喜欢我吗？";
        }
        //3. 返回陌生人问题
        return question.getTxt();
    }

    /**
     * 回复佳人陌生人问题
     * @param paramMap
     */
    public void replyStrangerQuestions(Map<String, Object> paramMap) {
        //1. 取佳人id
        Integer userId = (Integer) paramMap.get("userId");
        //2. 回复的内容
        String reply = (String) paramMap.get("reply");
        //3. 通过登陆用户信息，获取nickname
        UserInfo loginUserInfo = userInfoApi.findById(UserHolder.getUserId());
        String nickname = loginUserInfo.getNickname();
        //4. 通过佳人id查询佳人的陌生人问题
        Question question = questionApi.findByUserId(userId.longValue());
        String strangerQuestion = question==null?"你喜欢我吗？":question.getTxt();
        //5. 构建环信消息内容
        Map<String,Object> msgMap = new HashMap<>();
        msgMap.put("userId", UserHolder.getUserId());// 发送者id
        msgMap.put("nickname",nickname); // 发送者的昵称
        msgMap.put("strangerQuestion", strangerQuestion);
        msgMap.put("reply", reply);
        //6. 调用环信发送消息
        huanXinTemplate.sendMsg(userId.toString(), JSON.toJSONString(msgMap));
    }

    /**
     * 搜附近
     * @param gender
     * @param distance
     * @return
     */
    public List<NearUserVo> searchNearBy(String gender, Long distance) {
        //1. 获取登陆用户id
        Long loginUserId = UserHolder.getUserId();
        //2. 调用api查询登陆用户附近的人
        List<UserLocationVo> userLocationList = userLocationApi.searchNearBy(loginUserId, distance);
        List<NearUserVo> voList = new ArrayList<>();
        //3. 补全附近人的信息 过滤性别
        if(!CollectionUtils.isEmpty(userLocationList)){
            List<Long> userIds = userLocationList.stream().map(UserLocationVo::getUserId).collect(Collectors.toList());
            List<UserInfo> userInfoList = userInfoApi.findByBatchId(userIds);
            // 性别过滤并转成vo, filter(条件满足保留)
            voList = userInfoList.stream().filter(userInfo -> userInfo.getGender().equals(gender)).map(userInfo -> {
                NearUserVo userVo = new NearUserVo();
                BeanUtils.copyProperties(userInfo, userVo);
                userVo.setUserId(userInfo.getId());
                return userVo;
            }).collect(Collectors.toList());
        }
        return voList;
    }
}
