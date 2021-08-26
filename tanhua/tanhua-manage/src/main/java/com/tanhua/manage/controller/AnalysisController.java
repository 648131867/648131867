package com.tanhua.manage.controller;

import com.tanhua.manage.service.AnalysisService;
import com.tanhua.manage.vo.AnalysisSummaryVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/dashboard")
public class AnalysisController {

    @Autowired
    private AnalysisService analysisService;
    
    /**
     * 首页的概要统计
     * @return
     */
    @GetMapping("/summary")
    public ResponseEntity summary(){
        AnalysisSummaryVo vo = analysisService.summary();
        return ResponseEntity.ok(vo);
    }
}
