package com.tanhua.server.service;

import com.tanhua.commons.templates.HuanXinTemplate;
import com.tanhua.domain.db.UserInfo;
import com.tanhua.domain.mongo.Comment;
import com.tanhua.domain.mongo.Friend;
import com.tanhua.domain.vo.ContactVo;
import com.tanhua.domain.vo.MessageVo;
import com.tanhua.domain.vo.PageResult;
import com.tanhua.dubbo.api.UserInfoApi;
import com.tanhua.dubbo.api.mongo.CommentApi;
import com.tanhua.dubbo.api.mongo.FriendApi;
import com.tanhua.server.interceptor.UserHolder;
import com.tanhua.server.utils.RelativeDateFormat;
import org.apache.dubbo.config.annotation.Reference;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class IMService {

    @Reference
    private FriendApi friendApi;

    @Reference
    private UserInfoApi userInfoApi;

    @Reference
    private CommentApi commentApi;

    @Autowired
    private HuanXinTemplate huanXinTemplate;

    /**
     * 聊一聊后的交友
     * @param userId
     */
    public void makeFriends(Long userId) {
        //1. 调用api添加登陆用户与用户id为好友
        friendApi.makeFriends(UserHolder.getUserId(), userId);
        //2. 在环信上添加他为好友, 【注意】：这个ID必须在环信上的用户中存在,否则404
        huanXinTemplate.makeFriends(UserHolder.getUserId(), userId);
    }

    /**
     * 联系人列表
     * @param page
     * @param pageSize
     * @param keyword 暂时不理它
     * @return
     */
    public PageResult<ContactVo> queryContactsList(Long page, Long pageSize, String keyword) {
        // 1. 调用api,通过登陆用户id查询他的好友列表
        PageResult pageResult = friendApi.findPage(UserHolder.getUserId(), page,pageSize);
        // 2. 获取分页的结果集
        List<Friend> friendList = pageResult.getItems();
        // 3. 判断是否有结果
        if(!CollectionUtils.isEmpty(friendList)) {
            // 4. 取出所有好友的id
            List<Long> friendIds = friendList.stream().map(Friend::getFriendId).collect(Collectors.toList());
            // 5. 批量查询好友信息
            List<UserInfo> userInfoList = userInfoApi.findByBatchId(friendIds);
            // 6. 转成vo
            List<ContactVo> voList = userInfoList.stream().map(userInfo -> {
                ContactVo vo = new ContactVo();
                // 复制 属性
                BeanUtils.copyProperties(userInfo, vo);
                vo.setUserId(userInfo.getId().toString());
                return vo;
            }).collect(Collectors.toList());
            // 7. 设置回pageResult
            pageResult.setItems(voList);
        }
        // 8. 返回pageResult
        return pageResult;
    }

    /**
     * 点赞、评论、喜欢列表
     * @param commentType
     * @param page
     * @param pageSize
     * @return
     */
    public PageResult<MessageVo> messageCommentList(int commentType, Long page, Long pageSize) {
        //1. 调用api 按commentType分页查询评论表
        PageResult pageResult = commentApi.findPageByUserId(UserHolder.getUserId(), commentType, page, pageSize);
        //2. 分页结果集
        List<Comment> commentList = pageResult.getItems();
        if(!CollectionUtils.isEmpty(commentList)) {
            //3. 取出操作者的id
            List<Long> userIds = commentList.stream().map(Comment::getUserId).collect(Collectors.toList());
            //4. 批量查询操作者的信息
            List<UserInfo> userInfoList = userInfoApi.findByBatchId(userIds);
            //5. 把操作者信息转成map key=用户id value=用户信息
            Map<Long, UserInfo> userInfoMap = userInfoList.stream().collect(Collectors.toMap(UserInfo::getId, u -> u));
            //6. 转vo
            List<MessageVo> voList = commentList.stream().map(comment -> {
                MessageVo vo = new MessageVo();
                //7.  复制属性， 日期处理
                BeanUtils.copyProperties(userInfoMap.get(comment.getUserId()), vo);
                vo.setId(comment.getId().toHexString());
                vo.setCreateDate(RelativeDateFormat.format(new Date(comment.getCreated())));
                return vo;
            }).collect(Collectors.toList());
            //8.  设置回pageResult
            pageResult.setItems(voList);
        }
        //9. 返回结果
        return pageResult;
    }
}
