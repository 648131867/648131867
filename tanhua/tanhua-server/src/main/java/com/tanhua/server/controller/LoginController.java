package com.tanhua.server.controller;

import com.tanhua.domain.db.User;
import com.tanhua.server.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/user")
public class LoginController {

    @Autowired
    private UserService userService;

    //ResponseEntity: 响应内容. spring封装的响应http请求的对象
    // 与前端约定好了。不能改。只能用ResponseEntity
    // 规范
    @GetMapping("/findByMobile")
    public ResponseEntity findUser(String phone){
        // 调用业务层
        User user = userService.findByMobile(phone);
        return ResponseEntity.ok(user);
    }

    @PostMapping("/add")
    public ResponseEntity saveUser(@RequestBody User user){
        userService.saveUser(user);
        // ok(null)没有要响应的数据， http响应体内容为null
        return ResponseEntity.ok(null);
    }

    /**
     * 注册登陆 - 发送验证码
     * @param paramMap
     * @return
     */
    @PostMapping("/login")
    public ResponseEntity sendValidateCode(@RequestBody Map<String,String> paramMap){
        // 获取 手机号码
        String phone = paramMap.get("phone");
        // 调用业务发送验证码
        userService.sendValidateCode(phone);
        // 响应结果
        return ResponseEntity.ok(null);
    }

    /**
     * 注册登陆-验证码校验
     */
    @PostMapping("/loginVerification")
    public ResponseEntity loginVerification(@RequestBody Map<String,String> paramMap){
        // 验证码
        String verificationCode = paramMap.get("verificationCode");
        // 手机号码
        String phone = paramMap.get("phone");
        // 登陆认证
        Map<String,Object> result = userService.loginVerification(verificationCode,phone);
        return ResponseEntity.ok(result);
    }
}
