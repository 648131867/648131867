package com.tanhua.domain.vo;

import lombok.Data;

import java.io.Serializable;

/**
 * 为了解决age是integer问题
 */
@Data
public class UserInfoVoAge implements Serializable {
    private Long id; //用户id
    private String nickname; //昵称
    private String avatar; //用户头像
    private String birthday; //生日
    private String gender; //性别
    private Integer age; //年龄
    private String city; //城市
    private String income; //收入
    private String education; //学历
    private String profession; //行业
    private Integer marriage; //婚姻状态
}