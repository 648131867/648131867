package com.tanhua.server.controller;

import com.tanhua.domain.vo.UserInfoVo;
import com.tanhua.server.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/user")
public class UserController {

    @Autowired
    private UserService userService;

    /**
     * 新用户注册-填写信息
     * @param userInfoVo
     * @return
     */
    @PostMapping("/loginReginfo")
    public ResponseEntity loginReginfo(@RequestBody UserInfoVo userInfoVo)  {
        userService.loginReginfo(userInfoVo);
        return ResponseEntity.ok(null);
    }

    /**
     * 新用户注册-选取头像
     * @param headPhoto
     * @return
     */
    @PostMapping("/loginReginfo/head")
    public ResponseEntity uploadAvatar(MultipartFile headPhoto)  {
        userService.uploadAvatar(headPhoto);
        return ResponseEntity.ok(null);
    }
}
