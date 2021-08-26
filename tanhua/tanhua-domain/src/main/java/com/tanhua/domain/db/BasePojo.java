package com.tanhua.domain.db;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.TableField;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

@Data
public abstract class BasePojo implements Serializable {
    // fill 填充，填充的时机, FieldFill.INSERT 插入时触发
    @TableField(fill = FieldFill.INSERT)
    private Date created;
    //FieldFill.INSERT_UPDATE 插入或更新时触发
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private Date updated;
}
