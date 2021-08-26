package com.tanhua.manage.listener;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.tanhua.manage.domain.Log;
import com.tanhua.manage.service.LogService;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Date;

@Slf4j
@Component
@RocketMQMessageListener(topic = "tanhua_log", consumerGroup = "tanhua_log_consumer")
public class LogMessageListener implements RocketMQListener<String> {

    @Autowired
    private LogService logService;

    @Override
    public void onMessage(String message) {
        log.info("消费了: tanhua_log: {}", message);
        //message tanhua-server中UserService中的jsonString logMap
        //1. 转成map JSONObject implements Map<String, Object>
        JSONObject jsonObject = JSON.parseObject(message);
        //2. 把map转成pojo
        Log logPojo = new Log();
        logPojo.setLogTime(jsonObject.getString("log_time"));
        logPojo.setPlace(jsonObject.getString("address"));
        logPojo.setEquipment(jsonObject.getString("equipment"));
        logPojo.setType(jsonObject.getString("type"));
        logPojo.setUserId(jsonObject.getLong("userId"));
        logPojo.setCreated(new Date());
        //3. 调用service保存日志
        logService.add(logPojo);
        log.info("保存log记录表数据库");
    }
}
