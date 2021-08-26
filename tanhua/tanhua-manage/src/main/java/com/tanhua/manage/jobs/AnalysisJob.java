package com.tanhua.manage.jobs;

import com.tanhua.manage.service.AnalysisService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class AnalysisJob {

    @Autowired
    private AnalysisService analysisService;
    //private SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    // TODO 上线时改为每10分钟执行一次
    @Scheduled(cron = "0 0/10 * * * ?")
    public void doJob(){
        //System.out.println(sdf.format(new Date()));
        log.info("开始执行后台数据统计");
        analysisService.analysisData();
        log.info("完成后台数据统计");
    }
}
