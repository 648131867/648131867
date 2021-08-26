package com.tanhua.manage.service;

import cn.hutool.core.date.DateUtil;
import com.tanhua.manage.domain.AnalysisByDay;
import com.tanhua.manage.mapper.AnalysisByDayMapper;
import com.tanhua.manage.mapper.LogMapper;
import com.tanhua.manage.utils.ComputeUtil;
import com.tanhua.manage.vo.AnalysisSummaryVo;
import org.checkerframework.checker.units.qual.A;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;

@Service
public class AnalysisService {

    @Autowired
    private AnalysisByDayMapper analysisByDayMapper;

    @Autowired
    private LogMapper logMapper;

    /**
     * 首页的概要统计
     * @return
     */
    public AnalysisSummaryVo summary() {
        AnalysisSummaryVo vo = new AnalysisSummaryVo();
        Date todayDate = new Date();
        //1. 查询累计用户数
        Long cumulativeUsers = analysisByDayMapper.totalUserCount();
        vo.setCumulativeUsers(cumulativeUsers);
        //2. 过去7天 日期、30天日期
        String last7Date = DateUtil.offsetDay(todayDate, -7).toDateStr();
        String last30Date = DateUtil.offsetDay(todayDate, -30).toDateStr();
        //2.1 活跃用户数
        //过去7天活跃用户
        Long activePassWeek = logMapper.countActiveUserAfterDate(last7Date);
        vo.setActivePassWeek(activePassWeek);
        //过去30天活跃用户数
        Long activePassMonth = logMapper.countActiveUserAfterDate(last30Date);
        vo.setActivePassMonth(activePassMonth);
        //3. 获取今天日期
        String today = DateUtil.today();
        //3.1 获取昨天日期
        String yesterday = DateUtil.yesterday().toDateStr();
        //3.2 今天数据
        AnalysisByDay todayData = analysisByDayMapper.findByDate(today);
        vo.setNewUsersToday(todayData.getNumRegistered());
        vo.setLoginTimesToday(todayData.getNumLogin());
        vo.setActiveUsersToday(todayData.getNumActive());
        //3.3 昨天数据
        AnalysisByDay yesterdayData = analysisByDayMapper.findByDate(yesterday);
        //3.4 计算3个环比
        vo.setNewUsersTodayRate(ComputeUtil.computeRate(todayData.getNumRegistered(),yesterdayData.getNumRegistered()));
        vo.setLoginTimesTodayRate(ComputeUtil.computeRate(todayData.getNumLogin(), yesterdayData.getNumLogin()));
        vo.setActiveUsersTodayRate(ComputeUtil.computeRate(todayData.getNumActive(), yesterdayData.getNumActive()));
        //4. 构建vo，设置值
        //5. 返回
        return vo;
    }

    /**
     * 后台数据统计的方法
     */
    public void analysisData() {
        String today = DateUtil.today();
        String yesterday = DateUtil.yesterday().toDateStr();
        //1. 统计当天注册用户数
        Long numRegistered = logMapper.countRegisterByDate(today);
        //2. 当天活跃用户数
        Long numActive = logMapper.countActiveByDate(today);
        //3. 当天登陆用户数
        Long numLogin = logMapper.countLoginByDate(today);
        //4. 次日存留数
        Long numRetention1d = logMapper.countNumretention1d(today, yesterday);
        AnalysisByDay analysisByDay = new AnalysisByDay();
        analysisByDay.setNumActive(numActive);
        analysisByDay.setNumRegistered(numRegistered);
        analysisByDay.setNumLogin(numLogin);
        analysisByDay.setNumRetention1d(numRetention1d);
        analysisByDay.setRecordDate(new Date());
        analysisByDay.setUpdated(new Date());
        //5. 通过日期判断是否存在当天的统计记录
        AnalysisByDay analysisByDayInDB = analysisByDayMapper.findByDate(today);
        //6. 存在则更新数据
        if(null != analysisByDay){
            analysisByDay.setId(analysisByDayInDB.getId());
            analysisByDayMapper.updateById(analysisByDay);
        }else {
            //7. 不存在则添加记录
            analysisByDay.setCreated(new Date());
            analysisByDayMapper.insert(analysisByDay);
        }
    }
}
