package com.tanhua.dubbo.api.mongo;

import com.tanhua.domain.mongo.Comment;
import com.tanhua.domain.vo.PageResult;

public interface CommentApi {
    /**
     * 对动态操作
     * @param comment
     * @return
     */
    long save(Comment comment);

    /**
     * 取消点赞
     * @param comment
     * @return
     */
    long remove(Comment comment);

    /**
     * 通过动态id，分页查询评论列表
     * @param publsihId
     * @param page
     * @param pageSize
     * @return
     */
    PageResult findPage(String publsihId, Long page, Long pageSize);

    /**
     * 对评论点赞
     * @param comment
     * @return
     */
    long likeComment(Comment comment);

    /**
     * 取消评论的点赞
     * @param comment
     * @return
     */
    long dislikeComment(Comment comment);

    /**
     * 通过用户id查询对这个用户的操作(点赞、评论、喜欢)
     * @param userId
     * @param commentType 1: 点赞、2：评论、3：喜欢
     * @param page
     * @param pageSize
     * @return
     */
    PageResult findPageByUserId(Long userId, int commentType, Long page, Long pageSize);
}
