package com.tanhua.manage.listener;

import com.tanhua.commons.templates.HuaWeiUGCTemplate;
import com.tanhua.domain.mongo.Publish;
import com.tanhua.dubbo.api.mongo.PublishApi;
import lombok.extern.slf4j.Slf4j;
import org.apache.dubbo.config.annotation.Reference;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.util.List;

/**
 * 动态审核
 */
@Slf4j
@Component
@RocketMQMessageListener(topic = "tanhua-publish",consumerGroup = "tanhua_publish_group")
public class PublishMessageListener implements RocketMQListener<String> {

    @Reference
    private PublishApi publishApi;

    @Autowired
    private HuaWeiUGCTemplate huaWeiUGCTemplate;

    @Override
    public void onMessage(String message) {
        //message 就是动态的id
        String publishId = message;
        //1. 查询动态
        Publish publish = publishApi.findById(publishId);
        log.info("动态{}的状态为{}", publishId, publish.getState());
        if(publish.getState() != 0){
            // 审核过了
            return;
        }
        //2. 取出文本来审核
        String content = publish.getTextContent();
        log.info("开始审核动态内容【{}】", publishId);
        if (huaWeiUGCTemplate.textContentCheck(content)) {
            // 文本审核过了，才来审核图片
            //3. 取图片审核
            List<String> medias = publish.getMedias();
            if (!CollectionUtils.isEmpty(medias)) {
                // 有图片的情况才需要来审核
                String[] pics = medias.toArray(new String[]{});
                boolean imageCheckResult = huaWeiUGCTemplate.imageContentCheck(pics);
                log.info("审核动态图片【{}】{}", publishId, imageCheckResult);
                if (imageCheckResult) {
                    // 图片也审核通过
                    //4. 2者审核都通过，则更新动态的状态为1 审核通过
                    publishApi.updateState(publishId, 1);
                    return;
                }
            }
        }
        //5. 只要有一个没通过，则更新动态的状态为2 需要人工复审
        publishApi.updateState(publishId, 2);
    }
}
