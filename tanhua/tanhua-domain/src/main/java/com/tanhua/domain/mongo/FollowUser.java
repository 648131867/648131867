package com.tanhua.domain.mongo;

import lombok.Data;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.core.mapping.Document;

import java.io.Serializable;
 
@Data
@Document(collection = "follow_user")
public class FollowUser implements Serializable {

    private ObjectId id; //主键id
    private Long userId; //登陆用户id
    private Long followUserId; //登陆用户关注的人的id
    private Long created; //关注时间
}