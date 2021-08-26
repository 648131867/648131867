package com.tanhua.dubbo.api.mongo;

import com.mongodb.client.model.Updates;
import com.tanhua.domain.mongo.Comment;
import com.tanhua.domain.mongo.Publish;
import com.tanhua.domain.vo.PageResult;
import org.apache.dubbo.config.annotation.Service;
import org.bson.conversions.Bson;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;

import java.util.ArrayList;
import java.util.List;

@Service
public class CommentApiImpl implements CommentApi {

    @Autowired
    private MongoTemplate mongoTemplate;

    /**
     * 对动态操作
     *
     * @param comment
     * @return
     */
    @Override
    public long save(Comment comment) {
        //补全动态的作者id 5
        Publish publish = mongoTemplate.findById(comment.getTargetId(), Publish.class);
        Long authorId = publish.getUserId(); // 动态的作者id
        //添加评论表记录
        comment.setTargetUserId(authorId);
        comment.setCreated(System.currentTimeMillis());
        mongoTemplate.insert(comment);
        //更新动态的点赞数
        Query publishQuery = new Query(Criteria.where("_id").is(comment.getTargetId()));
        // 更新的操作，自增1
        Update update = new Update();
        String column = comment.getCol();
        update.inc(column, 1);
        mongoTemplate.updateFirst(publishQuery,update, Publish.class);

        if(comment.getCommentType() == 2){
            return 0;// 评论是不需要返回数量
        }
        //查询最新的点赞数
        publish = mongoTemplate.findById(comment.getTargetId(), Publish.class);
        Integer count = publish.getLikeCount(); // 默认返回点赞数
        // commentType=3代表喜欢
        if(3 == comment.getCommentType()){
            count = publish.getLoveCount();
        }
        return count;
    }

    /**
     * 取消点赞
     *
     * @param comment
     * @return
     */
    @Override
    public long remove(Comment comment) {
        //1. 先删除评论表数据
        //1.1 构建删除的条件
        Query removeQuery = new Query(Criteria.where("targetId").is(comment.getTargetId())
                .and("commentType").is(comment.getCommentType())
                .and("userId").is(comment.getUserId()));
        //1.2 删除
        mongoTemplate.remove(removeQuery, Comment.class);
        //2. 更新动态的点赞数-1
        //构建更新的条件，设置自增-1
        Query publishQuery = new Query(Criteria.where("_id").is(comment.getTargetId()));
        Update update = new Update();
        //【注意】 取消点赞，应该是-1
        update.inc(comment.getCol(), -1);
        mongoTemplate.updateFirst(publishQuery,update, Publish.class);
        //3. 查询最新点赞数
        //查询最新的点赞数
        Publish publish = mongoTemplate.findById(comment.getTargetId(), Publish.class);
        Integer count = publish.getLikeCount(); // 默认返回点赞数
        // commentType=3代表喜欢
        if(3 == comment.getCommentType()){
            count = publish.getLoveCount();
        }
        return count;
    }

    /**
     * 通过动态id，分页查询评论列表
     *
     * @param publishId
     * @param page
     * @param pageSize
     * @return
     */
    @Override
    public PageResult findPage(String publishId, Long page, Long pageSize) {
        // 构建查询评论表的条件
        Query query = new Query(Criteria.where("targetId").is(new ObjectId(publishId))
                .and("commentType").is(2));//2 代表评论
        // 统计总数
        long total = mongoTemplate.count(query, Comment.class);
        // 总数>0
        List<Comment> commentList = new ArrayList<Comment>();
        if (total>0) {
            // 按时间降序
            query.with(Sort.by(Sort.Order.desc("created")));
            // 设置分页
            query.skip((page-1)*pageSize).limit(pageSize.intValue());
            // 查询
            commentList = mongoTemplate.find(query, Comment.class);
        }

        // 构建PageResult返回
        return PageResult.pageResult(page, pageSize,commentList,total
        );
    }

    /**
     * 对评论点赞
     *
     * @param comment
     * @return
     */
    @Override
    public long likeComment(Comment comment) {
        //补全评论的作者id 5
        Comment targetComment = mongoTemplate.findById(comment.getTargetId(), Comment.class);
        Long authorId = targetComment.getUserId(); // 原评论的作者id
        //添加评论表记录
        comment.setTargetUserId(authorId);
        comment.setCreated(System.currentTimeMillis());
        mongoTemplate.insert(comment);
        //更新评论的点赞数
        Query commentQuery = new Query(Criteria.where("_id").is(comment.getTargetId()));
        // 更新的操作，自增1
        Update update = new Update();
        update.inc("likeCount", 1);
        mongoTemplate.updateFirst(commentQuery,update, Comment.class);

        // 重新查询评论的点赞数
        targetComment = mongoTemplate.findById(comment.getTargetId(), Comment.class);
        return targetComment.getLikeCount();
    }

    /**
     * 取消评论的点赞
     *
     * @param comment
     * @return
     */
    @Override
    public long dislikeComment(Comment comment) {
        //1. 删除评论表数据
        // 构建删除的条件：3个
        Query commentQuery = new Query(Criteria.where("targetId").is(comment.getTargetId())
                .and("commentType").is(1)//1代表点赞
                .and("userId").is(comment.getUserId()));
        mongoTemplate.remove(commentQuery, Comment.class);
        //2. 更新原评论的点赞数-1
        // 构建原有评论的查询条件
        commentQuery = new Query(Criteria.where("_id").is(comment.getTargetId()));
        // 创建更新自减1
        Update update = new Update();
        update.inc("likeCount", -1);
        // 更新
        mongoTemplate.updateFirst(commentQuery, update, Comment.class);
        //3. 查询最新的点选数
        // 重新查询评论的点赞数
        Comment targetComment = mongoTemplate.findById(comment.getTargetId(), Comment.class);
        return targetComment.getLikeCount();
    }

    /**
     * 通过用户id查询对这个用户的操作(点赞、评论、喜欢)
     *
     * @param loginUserId
     * @param commentType 1: 点赞、2：评论、3：喜欢
     * @param page
     * @param pageSize
     * @return
     */
    @Override
    public PageResult findPageByUserId(Long loginUserId, int commentType, Long page, Long pageSize) {
        //1. 构建查询条件
        Query query = new Query(Criteria.where("targetUserId").is(loginUserId)
                .and("commentType").is(commentType));
        //2. 总数
        long total = mongoTemplate.count(query, Comment.class);
        //3. 总数>0
        List<Comment> commentList = new ArrayList<>();
        if(total > 0) {
            //4. 设置分页
            query.skip((page-1)*pageSize).limit(pageSize.intValue());
            //5. 设置降序
            query.with(Sort.by(Sort.Order.desc("created")));
            //6. 查询结果集
            commentList = mongoTemplate.find(query, Comment.class);
        }
        //7. 构建pageResult返回
        return PageResult.pageResult(page,pageSize,commentList,total);
    }
}
