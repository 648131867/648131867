package com.tanhua.manage.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.tanhua.manage.domain.Log;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface LogMapper extends BaseMapper<Log> {

    @Select("select count(distinct user_id) from tb_log where log_time > #{dateStr}")
    Long countActiveUserAfterDate(String dateStr);

    /**
     * 统计当天注册用户数
     * @param today
     * @return
     */
    @Select("select count(1) from tb_log where type='0101' and log_time=#{today}")
    Long countRegisterByDate(String today);

    /**
     * 当天活跃用户数
     * @param today
     * @return
     */
    @Select("select count(distinct user_id) from tb_log where log_time=#{today}")
    Long countActiveByDate(String today);

    /**
     * 当天登陆用户数
     * @param today
     * @return
     */
    @Select("select count(distinct user_id) from tb_log where type in ('0101','0102') and log_time=#{today}")
    Long countLoginByDate(String today);

    /**
     * 次日存留数
     * @param today
     * @param yesterday
     * @return
     */
    @Select("select count(distinct user_id) from tb_log where log_time=#{today} " +
            "and user_id in (" +
            " select user_id from tb_log where type='0101' and log_time=#{yesterday}" +
            ")")
    Long countNumretention1d(@Param("today") String today, @Param("yesterday") String yesterday);
}
