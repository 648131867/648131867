package com.tanhua.server.service;

import com.tanhua.domain.db.UserInfo;
import com.tanhua.domain.mongo.Comment;
import com.tanhua.domain.vo.CommentVo;
import com.tanhua.domain.vo.PageResult;
import com.tanhua.dubbo.api.UserInfoApi;
import com.tanhua.dubbo.api.mongo.CommentApi;
import com.tanhua.server.interceptor.UserHolder;
import com.tanhua.server.utils.RelativeDateFormat;
import org.apache.dubbo.config.annotation.Reference;
import org.bson.types.ObjectId;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class CommentService {

    @Reference
    private CommentApi commentApi;

    @Reference
    private UserInfoApi userInfoApi;

    @Autowired
    private RedisTemplate redisTemplate;

    /**
     * 动态点赞
     * @param publishId
     * @return
     */
    public long like(String publishId) {
        //1. 获取登陆用户id
        Long loginUserId = UserHolder.getUserId();
        //2. 构建Comment对象
        Comment comment = new Comment();
        comment.setUserId(loginUserId); // 操作者，登陆用户
        comment.setTargetId(new ObjectId(publishId));
        comment.setTargetType(1);//评论内容类型： 1-对动态操作
        comment.setCommentType(1);//评论类型，1-点赞
        //3. 调用api添加动态点赞
        long likeCount = commentApi.save(comment);
        // 标记当前登陆用户对这个动态点赞了
        String key = "publish_like_" + loginUserId + "_" + publishId;
        redisTemplate.opsForValue().set(key, 1);
        //4. 返回点赞数
        return likeCount;
    }

    /**
     * 动态取消点赞
     * @param publishId
     * @return
     */
    public long dislike(String publishId) {
        //1. 获取登陆用户id
        Long loginUserId = UserHolder.getUserId();
        //2. 构建Comment对象
        Comment comment = new Comment();
        comment.setUserId(loginUserId); // 操作者，登陆用户
        comment.setTargetId(new ObjectId(publishId));
        comment.setCommentType(1);//评论类型，1-点赞
        //3. 调用api删除评论
        long likeCount = commentApi.remove(comment);
        // 删除点赞的标记
        String key = "publish_like_" + loginUserId + "_" + publishId;
        redisTemplate.delete(key);
        //4. 返回点赞数
        return likeCount;
    }

    /**
     * 动态的喜欢
     * @param publishId
     * @return
     */
    public long love(String publishId) {
        //1. 获取登陆用户id
        Long loginUserId = UserHolder.getUserId();
        //2. 构建Comment对象
        Comment comment = new Comment();
        comment.setUserId(loginUserId); // 操作者，登陆用户
        comment.setTargetId(new ObjectId(publishId));
        comment.setTargetType(1);//评论内容类型： 1-对动态操作
        comment.setCommentType(3);//评论类型，3-喜欢
        //3. 调用api添加动态喜欢
        long loveCount = commentApi.save(comment);
        // 标记当前登陆用户对这个动态喜欢了
        String key = "publish_love_" + loginUserId + "_" + publishId;
        redisTemplate.opsForValue().set(key, 1);
        //4. 返回喜欢数
        return loveCount;
    }

    /**
     * 动态取消喜欢
     * @param publishId
     * @return
     */
    public long unlove(String publishId) {
        //1. 获取登陆用户id
        Long loginUserId = UserHolder.getUserId();
        //2. 构建Comment对象
        Comment comment = new Comment();
        comment.setUserId(loginUserId); // 操作者，登陆用户
        comment.setTargetId(new ObjectId(publishId));
        comment.setCommentType(3);//评论类型，3-喜欢
        //3. 调用api删除评论
        long likeCount = commentApi.remove(comment);
        // 删除喜欢的标记
        String key = "publish_love_" + loginUserId + "_" + publishId;
        redisTemplate.delete(key);
        //4. 返回喜欢数
        return likeCount;
    }

    /**
     * 分页查询某个动态的评论列表
     * @param publishId
     * @param page
     * @param pageSize
     * @return
     */
    public PageResult<CommentVo> findPage(String publishId, Long page, Long pageSize) {
        //1. 调用api通过动态id分页查询评论列表
        PageResult pageResult =commentApi.findPage(publishId,page,pageSize);
        //2. 获取分页的结果集
        List<Comment> commentList = pageResult.getItems();
        List<CommentVo> voList = new ArrayList<>();
        if(!CollectionUtils.isEmpty(commentList)) {
            //3. 获取所有评论者的id
            List<Long> commentUserIds = commentList.stream().map(Comment::getUserId).collect(Collectors.toList());
            //4. 批量查询评论者详情
            List<UserInfo> userInfList = userInfoApi.findByBatchId(commentUserIds);
            //5. 评论者详情转成map
            Map<Long, UserInfo> userInfoMap = userInfList.stream().collect(Collectors.toMap(UserInfo::getId, u -> u));
            //6. 构建vo，转成vo
            voList = commentList.stream().map(comment -> {
                CommentVo vo = new CommentVo();
                // 复制评论内容
                BeanUtils.copyProperties(comment, vo);
                vo.setId(comment.getId().toHexString());
                vo.setCreateDate(RelativeDateFormat.format(new Date(comment.getCreated())));
                // 复制用户详情
                UserInfo userInfo = userInfoMap.get(comment.getUserId());
                BeanUtils.copyProperties(userInfo, vo);

                String key = "comment_like_" + UserHolder.getUserId() + "_" + vo.getId();
                if(redisTemplate.hasKey(key)){
                    // 点赞过了
                    vo.setHasLiked(1);
                }
                return vo;
            }).collect(Collectors.toList());
        }
        pageResult.setItems(voList);
        //7. 返回
        return pageResult;
    }

    /**
     * 发表评论
     * @param paramMap
     */
    public void add(Map<String, String> paramMap) {
        //1. 动态id
        String publishId = paramMap.get("movementId");
        //2. 评论内容
        String content = paramMap.get("comment");
        //3. 构建评论对象
        Comment comment = new Comment();
        comment.setTargetId(new ObjectId(publishId));
        comment.setTargetType(1);//1代表操作的对象类型为动态
        comment.setCommentType(2);// 评论
        comment.setContent(content);
        comment.setUserId(UserHolder.getUserId());
        //4. 调用api添加
        commentApi.save(comment);
    }

    /**
     * 对评论 点赞
     * @param commentId
     * @return
     */
    public long likeComment(String commentId) {
        //1. 构建评论对象
        Comment comment = new Comment();
        comment.setTargetId(new ObjectId(commentId));
        comment.setTargetType(3);//3-对评论操作
        comment.setCommentType(1);//评论类型，1-点赞
        comment.setUserId(UserHolder.getUserId());
        //2. 调用api添加
        long count = commentApi.likeComment(comment);
        //3. 用redis标记 当前用户对这个评论点赞了
        String key = "comment_like_" + UserHolder.getUserId() + "_" + commentId;
        redisTemplate.opsForValue().set(key,1);
        //4. 返回最新的点赞数
        return count;
    }

    /**
     * 对评论取消点赞
     * @param commentId
     * @return
     */
    public long dislikeComment(String commentId) {
        //1 构建评论对象
        Comment comment = new Comment();
        comment.setTargetId(new ObjectId(commentId));
        comment.setCommentType(1);// 1点赞
        comment.setUserId(UserHolder.getUserId());
        //2. 调用api取消评论的点赞
        long count = commentApi.dislikeComment(comment);
        //3. 删除redis的标记
        String key = "comment_like_" + UserHolder.getUserId() + "_" + commentId;
        redisTemplate.delete(key);
        //4. 返回最新数量
        return count;
    }
}
