package com.tanhua.domain.db;
import lombok.Data;

/**
 * 陌生人问题
 */
@Data
public class Question extends BasePojo {
    private Long id;
    private Long userId;
    //问题内容
    private String txt;
}