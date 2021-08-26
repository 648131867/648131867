package com.tanhua.domain.vo;

import lombok.Data;

@Data
public class SettingsVo {
    private Long id;
    private String strangerQuestion;// 陌生人问题
    private String phone;// 登陆用户的手机号码
    private boolean likeNotification=true; // 喜欢的通知, 默认是打开
    private boolean pinglunNotification=true; // 评论时的通知, 默认是打开
    private boolean gonggaoNotification=true;// 发布公告时的通知, 默认是打开
}