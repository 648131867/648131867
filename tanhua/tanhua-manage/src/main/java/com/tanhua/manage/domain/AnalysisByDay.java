package com.tanhua.manage.domain;

import com.tanhua.domain.db.BasePojo;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AnalysisByDay extends BasePojo {

    private Long id;
    /**
     * 日期
     */
    private Date recordDate;
    /**
     * 新注册用户数
     */
    private Long numRegistered = 0l;
    /**
     * 活跃用户数
     */
    private Long numActive = 0l;
    /**
     * 登陆次数
     */
    private Long numLogin = 0l;
    /**
     * 次日留存用户数
     */
    private Long numRetention1d = 0l;
}