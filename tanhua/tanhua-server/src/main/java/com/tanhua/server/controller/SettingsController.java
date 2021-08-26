package com.tanhua.server.controller;

import com.tanhua.domain.vo.PageResult;
import com.tanhua.domain.vo.SettingsVo;
import com.tanhua.domain.vo.UserInfoVoAge;
import com.tanhua.server.service.SettingsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 通用设置
 */
@RestController
@RequestMapping("/users")
public class SettingsController {

    @Autowired
    private SettingsService settingsService;
    
    /**
     * 通用设置查询
     * @return
     */
    @GetMapping("/settings")
    public ResponseEntity querySettings(){
        SettingsVo vo = settingsService.querySettings();
        return ResponseEntity.ok(vo);
    }

    /**
     * 保存通知设置
     * @param vo
     * @return
     */
    @PostMapping("/notifications/setting")
    public ResponseEntity updateNotification(@RequestBody SettingsVo vo){
        settingsService.updateNotification(vo);
        return ResponseEntity.ok(null);
    }

    /**
     * 保存陌生人问题
     * @param paramMap
     * @return
     */
    @PostMapping("/questions")
    public ResponseEntity updateQuestion(@RequestBody Map<String,String> paramMap){
        settingsService.updateQuestion(paramMap);
        return ResponseEntity.ok(null);
    }

    /**
     * 黑名单分页查询
     * @param page
     * @param pageSize
     * @return
     */
    @GetMapping("/blacklist")
    public ResponseEntity blacklist(@RequestParam(value = "page", defaultValue = "1") Long page,
                                    @RequestParam(value = "pagesize",defaultValue = "10") Long pageSize){
        page = page>0?page:1; // 如果页码<=0 查询第1页
        pageSize = pageSize>0?pageSize:10; // 负数处理
        pageSize=pageSize>50?50:pageSize; // pageSize限制大小，防止用户查询过多导致系统问题

        PageResult<UserInfoVoAge> pageResult = settingsService.blackList(page,pageSize);
        return ResponseEntity.ok(pageResult);
    }

    /**
     * 移除黑名单
     * @param blackUserId
     * @return
     */
    @DeleteMapping("/blacklist/{blackUserId}")
    public ResponseEntity removeBlackList(@PathVariable Long blackUserId){
        settingsService.removeBlackList(blackUserId);
        return ResponseEntity.ok(null);
    }
}
