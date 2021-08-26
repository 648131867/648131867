package com.tanhua.server.service;

import com.tanhua.domain.db.*;
import com.tanhua.domain.vo.PageResult;
import com.tanhua.domain.vo.SettingsVo;
import com.tanhua.domain.vo.UserInfoVoAge;
import com.tanhua.dubbo.api.BlackListApi;
import com.tanhua.dubbo.api.QuestionApi;
import com.tanhua.dubbo.api.SettingsApi;
import com.tanhua.dubbo.api.UserInfoApi;
import com.tanhua.server.interceptor.UserHolder;
import org.apache.dubbo.config.annotation.Reference;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class SettingsService {

    @Reference
    private QuestionApi questionApi;

    @Reference
    private SettingsApi settingsApi;

    @Reference
    private BlackListApi blackListApi;

    @Reference
    private UserInfoApi userInfoApi;

    /**
     * 通用设置查询
     * @return
     */
    public SettingsVo querySettings() {
        // 1. 获取登陆用户信息
        User loginUser = UserHolder.getUser();
        // 2. 查询登陆用户的陌生人问题
        String strangerQuestion = "你喜欢我吗?"; // 默认的陌生人问题
        Question question = questionApi.findByUserId(loginUser.getId());
        if(null != question){
            // 用户有设置，则使用用户设置的
            strangerQuestion = question.getTxt();
        }
        // 3. 查询登陆用户的通知设置
        Settings settings = settingsApi.findByUserId(loginUser.getId());
        // 4. 构建vo
        SettingsVo vo = new SettingsVo();
        vo.setStrangerQuestion(strangerQuestion);
        // 有通知设置时才需要来设置，否则采用默认值true
        if(null != settings){
            BeanUtils.copyProperties(settings, vo);
        }
        // 5. 设置用户id, 手机号码
        vo.setId(loginUser.getId());
        vo.setPhone(loginUser.getMobile());
        // 6. 返回vo
        return vo;
    }

    /**
     * 保存通知设置
     * @param vo
     */
    public void updateNotification(SettingsVo vo) {
        //1. 把vo转成 pojo
        Settings settings = new Settings();
        BeanUtils.copyProperties(vo, settings);
        //2. 设置操作的用户id为登陆用户id
        settings.setUserId(UserHolder.getUserId());
        //3. 调用api保存
        settingsApi.save(settings);
    }

    /**
     * 保存陌生人问题
     * @param paramMap
     */
    public void updateQuestion(Map<String, String> paramMap) {
        //1. 构建pojo
        Question question = new Question();
        question.setTxt(paramMap.get("content"));
        question.setUserId(UserHolder.getUserId());
        //2. 调用api保存
        questionApi.save(question);
    }

    /**
     * 黑名单分页查询
     * @param page
     * @param pageSize
     * @return
     */
    public PageResult<UserInfoVoAge> blackList_2(Long page, Long pageSize) {
        // 1. 调用api通过登陆用户id，分页查询登陆用户的黑名单列表
        PageResult pageResult = blackListApi.findPageByUserId(UserHolder.getUserId(), page, pageSize);
        // 2. 获取分页的结果集，里有黑名单人员的用户id
        List<BlackList> blackLists = pageResult.getItems();
        // 3. 补全黑名单人员的用户详情
        // isEmpty为空
        if(!CollectionUtils.isEmpty(blackLists)){
            // 有黑名单
            // 批量查询黑名单人员的详情
            List<UserInfoVoAge> voList = new ArrayList<>(blackLists.size());
            for (BlackList blackList : blackLists) {
                // 遍历黑名单人员, 取出黑名单人员的id
                Long blackUserId = blackList.getBlackUserId();
                // 查询这个黑名单人员的信息
                UserInfo blackUserInfo = userInfoApi.findById(blackUserId);
                // 转成vo
                UserInfoVoAge vo = new UserInfoVoAge();
                BeanUtils.copyProperties(blackUserInfo, vo);
                voList.add(vo);
            }
            // 重置设置查询的结果
            pageResult.setItems(voList);
        }

        // 5. 返回
        return pageResult;
    }

    /**
     * 黑名单分页查询
     * @param page
     * @param pageSize
     * @return
     */
    public PageResult<UserInfoVoAge> blackList_3(Long page, Long pageSize) {
        // 1. 调用api通过登陆用户id，分页查询登陆用户的黑名单列表
        PageResult pageResult = blackListApi.findPageByUserId(UserHolder.getUserId(), page, pageSize);
        // 2. 获取分页的结果集，里有黑名单人员的用户id
        List<BlackList> blackLists = pageResult.getItems();
        // 3. 补全黑名单人员的用户详情
        // isEmpty为空
        if(!CollectionUtils.isEmpty(blackLists)){
            // 有黑名单
            // 批量查询黑名单人员的详情
            List<UserInfoVoAge> voList = new ArrayList<>(blackLists.size());
            // 获取所有黑名单人员的id
            List<Long> blackUserIds = blackLists.stream().map(BlackList::getBlackUserId).collect(Collectors.toList());
            // 批量查询用户信息
            // 用户详情的集合
            List<UserInfo> userInfoList = userInfoApi.findByBatchId(blackUserIds);
            for (BlackList blackList : blackLists) {
                // 遍历黑名单人员, 取出黑名单人员的id
                Long blackUserId = blackList.getBlackUserId();
                // 查询这个黑名单人员的信息
                UserInfo blackUserInfo = getUserInfo(blackUserId,userInfoList);
                // 转成vo
                UserInfoVoAge vo = new UserInfoVoAge();
                BeanUtils.copyProperties(blackUserInfo, vo);
                voList.add(vo);
            }
            // 重置设置查询的结果
            pageResult.setItems(voList);
        }

        // 5. 返回
        return pageResult;
    }

    /**
     * 黑名单分页查询
     * @param page
     * @param pageSize
     * @return
     */
    public PageResult<UserInfoVoAge> blackList(Long page, Long pageSize) {
        // 1. 调用api通过登陆用户id，分页查询登陆用户的黑名单列表
        PageResult pageResult = blackListApi.findPageByUserId(UserHolder.getUserId(), page, pageSize);
        // 2. 获取分页的结果集，里有黑名单人员的用户id
        List<BlackList> blackLists = pageResult.getItems();
        // 3. 补全黑名单人员的用户详情
        // isEmpty为空
        if(!CollectionUtils.isEmpty(blackLists)){
            // 有黑名单
            // 批量查询黑名单人员的详情
            List<UserInfoVoAge> voList = new ArrayList<>(blackLists.size());
            // 获取所有黑名单人员的id
            List<Long> blackUserIds = blackLists.stream().map(BlackList::getBlackUserId).collect(Collectors.toList());
            // 批量查询用户信息
            // 用户详情的集合
            List<UserInfo> userInfoList = userInfoApi.findByBatchId(blackUserIds);
            // 转成map, key=用户id, value=用户详情, u代表每个userInfo
            Map<Long, UserInfo> userInfoMap = userInfoList.stream().collect(Collectors.toMap(UserInfo::getId, u->u));
            //Map<Long, UserInfo> userInfoMap = new HashMap<>();
            //for (UserInfo userInfo : userInfoList) {
            //    userInfoMap.put(userInfo.getId(), userInfo);
            //}

           /* for (BlackList blackList : blackLists) {
                // 遍历黑名单人员, 取出黑名单人员的id
                Long blackUserId = blackList.getBlackUserId();
                // 查询这个黑名单人员的信息
                UserInfo blackUserInfo = userInfoMap.get(blackUserId);
                // 转成vo
                UserInfoVoAge vo = new UserInfoVoAge();
                BeanUtils.copyProperties(blackUserInfo, vo);
                voList.add(vo);
            }*/
            voList = blackLists.stream().map(blackList -> {
                UserInfoVoAge vo = new UserInfoVoAge();
                BeanUtils.copyProperties(userInfoMap.get(blackList.getBlackUserId()), vo);
                return vo;
            }).collect(Collectors.toList());

            // 重置设置查询的结果
            pageResult.setItems(voList);
        }

        // 5. 返回
        return pageResult;
    }

    /**
     * 通过用户id从集合中找出用户信息
     * @param userId
     * @param userInfoMap  key=用户id, value=UserInfo用户详情
     * @return
     */
    private UserInfo getUserInfo(Long userId, Map<Long, UserInfo> userInfoMap){
        return userInfoMap.get(userId);
    }

    /**
     * 通过用户id从集合中找出用户信息
     * @param userId
     * @param userInfoList
     * @return
     */
    private UserInfo getUserInfo(Long userId, List<UserInfo> userInfoList){
        for (UserInfo userInfo : userInfoList) {
            if (userInfo.getId().equals(userId)) {
                return userInfo;
            }
        }
        return null;
    }

    /**
     * 移除黑名单
     * @param blackUserId
     */
    public void removeBlackList(Long blackUserId) {
        //1. 获取登陆用户id
        Long loginUserId = UserHolder.getUserId();
        //2. 调用api删除
        blackListApi.delete(loginUserId, blackUserId);
    }
}
