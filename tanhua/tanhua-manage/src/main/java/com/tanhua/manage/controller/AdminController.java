package com.tanhua.manage.controller;

import cn.hutool.captcha.CaptchaUtil;
import cn.hutool.captcha.LineCaptcha;
import com.tanhua.manage.domain.Admin;
import com.tanhua.manage.interceptor.AdminHolder;
import com.tanhua.manage.service.AdminService;
import com.tanhua.manage.vo.AdminVo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Map;

@RestController
@RequestMapping("/system/users")
@Slf4j
public class AdminController {

    @Autowired
    private AdminService adminService;

    /**
     * 后台登陆时 图片验证码 生成
     */
    @GetMapping("/verification")
    public void showValidateCodePic(String uuid,HttpServletRequest req, HttpServletResponse res){
        res.setDateHeader("Expires",0);
        res.setHeader("Cache-Control", "no-store, no-cache, must-revalidate");
        res.addHeader("Cache-Control", "post-check=0, pre-check=0");
        // Set standard HTTP/1.0 no-cache header.
        res.setHeader("Pragma", "no-cache");
        res.setContentType("image/jpeg");
        LineCaptcha lineCaptcha = CaptchaUtil.createLineCaptcha(299, 97);
        String code = lineCaptcha.getCode();
        log.debug("uuid={},code={}",uuid,code);
        adminService.saveCode(uuid,code);
        try {
            lineCaptcha.write(res.getOutputStream());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 登陆
     * @param paramMap
     * @return
     */
    @PostMapping("/login")
    public ResponseEntity login(@RequestBody Map<String,String> paramMap){
        Map<String,String> resultMap = adminService.login(paramMap);
        return ResponseEntity.ok(resultMap);
    }

    /**
     * 获取登陆用户信息
     * @return
     */
    @PostMapping("/profile")
    public ResponseEntity profile(){
        Admin admin = AdminHolder.getAdmin();
        AdminVo adminVo = new AdminVo();
        BeanUtils.copyProperties(admin,adminVo);
        return ResponseEntity.ok(adminVo);
    }

    /**
     * 退出登陆
     * @param token
     * @return
     */
    @PostMapping("/logout")
    public ResponseEntity logout(@RequestHeader("Authorization") String token){
        adminService.logout(token);
        return ResponseEntity.ok(null);
    }

}