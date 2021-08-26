package com.tanhua.manage.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.tanhua.manage.domain.AnalysisByDay;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface AnalysisByDayMapper extends BaseMapper<AnalysisByDay> {
    /**
     * 所有用户数
     * @return
     */
    @Select("select sum(num_registered) from tb_analysis_by_day")
    Long totalUserCount();

    @Select("select * from tb_analysis_by_day where record_date = #{dateStr}")
    AnalysisByDay findByDate(String dateStr);
}