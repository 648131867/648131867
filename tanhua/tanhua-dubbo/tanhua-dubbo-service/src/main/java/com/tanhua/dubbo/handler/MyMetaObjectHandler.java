package com.tanhua.dubbo.handler;

import com.baomidou.mybatisplus.core.handlers.MetaObjectHandler;
import org.apache.ibatis.reflection.MetaObject;
import org.springframework.stereotype.Component;
import java.util.Date;

/**
 * 公共填充类
 */
@Component
public class MyMetaObjectHandler implements MetaObjectHandler {
    /**
     * 插入时自动填充 created updated
     * 添加新数据，
     * @param metaObject
     */
    @Override
    public void insertFill(MetaObject metaObject) {
        // 通过字段名设置它的值
        // p1: 字段名称
        // p2: 值
        //
        setFieldValByName("created", new Date(), metaObject);
        setFieldValByName("updated", new Date(), metaObject);
    }

    /**
     * 更新时自动填充 updated
     *
     * @param metaObject
     */
    @Override
    public void updateFill(MetaObject metaObject) {
        // 更新时触发，且给updated字段赋值
        setFieldValByName("updated", new Date(), metaObject);
    }
}