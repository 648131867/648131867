package com.tanhua.domain.vo;

import lombok.Data;
@Data
public class RecommendUserVo {
    private Long id;
    private String avatar;
    private String nickname;
    private String gender; //性别 man woman
    private Integer age;
    private String[] tags;
    private Long fateValue; //缘分值
}