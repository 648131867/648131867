package com.tanhua.domain.mongo;

import lombok.Data;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.core.mapping.Document;

import java.io.Serializable;

@Data
@Document(collection = "quanzi_comment")
public class Comment implements Serializable {

    private ObjectId id;

    private ObjectId targetId;    //目标id 如果：targetType=1 targetId代表动态的id, targetType=2 targetId=视频的id...
                                  //                         targetUserId 动态的作者id       targetUserId视频的发布者
    private Long targetUserId; // 目标作者id
    private Integer commentType;   //评论类型，1-点赞，2-评论，3-喜欢
    private Integer targetType;       //评论内容类型： 1-对动态操作 2-对视频操作 3-对评论操作
    private String content;        //评论内容
    private Long userId;           //评论人
    private Integer likeCount = 0; //点赞数
    private Long created; //发表时间

    //动态选择更新的字段
    public String getCol() {
        return this.commentType == 1 ? "likeCount" : commentType==2? "commentCount"
            : "loveCount";
    }

}