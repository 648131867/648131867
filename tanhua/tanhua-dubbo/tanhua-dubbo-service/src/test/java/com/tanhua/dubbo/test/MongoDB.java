package com.tanhua.dubbo.test;

import com.tanhua.domain.mongo.Publish;
import com.tanhua.domain.mongo.RecommendQuanzi;
import org.apache.commons.lang3.RandomUtils;
import org.bson.types.ObjectId;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.List;

@SpringBootTest
@RunWith(SpringRunner.class)
public class MongoDB {

    @Autowired
    private MongoTemplate mongoTemplate;

    @Test
    public void addRecommentQuanzi(){
        // 先查出有哪些动态
        List<Publish> publishes = mongoTemplate.find(new Query(), Publish.class);
        publishes.forEach(p->{
            // 插入记录
            ObjectId publishId = p.getId();
            RecommendQuanzi quanzi = new RecommendQuanzi();
            quanzi.setCreated(System.currentTimeMillis());
            quanzi.setScore(RandomUtils.nextDouble(80,90));
            quanzi.setUserId(10010l);
            quanzi.setPublishId(publishId);
            mongoTemplate.insert(quanzi);
        });
    }
}
