package com.tanhua.domain.mongo;

import lombok.Data;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.core.mapping.Document;

import java.io.Serializable;

/**
 * <p>
 * 时间线表，每一个用户一张表进行存储
 * 没有固定表名
 * </p>
 */
@Data
public class TimeLine implements Serializable {

    private ObjectId id;

    private Long userId; // 作者id
    private ObjectId publishId; //动态id

    private Long created; //发布的时间
}