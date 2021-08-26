package com.tanhua.server.controller;

import com.tanhua.domain.vo.CountsVo;
import com.tanhua.domain.vo.FriendVo;
import com.tanhua.domain.vo.PageResult;
import com.tanhua.domain.vo.UserInfoVo;
import com.tanhua.server.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/users")
public class UserInfoController {

    @Autowired
    private UserService userService;

    /**
     * 登陆后，获取个人信息
     * @param userID
     * @param huanxinID
     * @return
     */
    @GetMapping
    public ResponseEntity getUserInfo(String userID, String huanxinID){
        // userID与huanxinID 暂时不处理先，等即时通讯时再来处理
        UserInfoVo vo = userService.getLoginUserInfo();
        return ResponseEntity.ok(vo);
    }

    /**
     * 更新用户信息
     * @param vo
     * @return
     */
    @PutMapping
    public ResponseEntity updateUserInfo(@RequestBody UserInfoVo vo){
        userService.updateUserInfo(vo);
        return ResponseEntity.ok(null);
    }

    /**
     * 我喜欢 统计
     * @return
     */
    @GetMapping("/counts")
    public ResponseEntity counts(){
        CountsVo vo = userService.counts();
        return ResponseEntity.ok(vo);
    }

    /**
     * 互相喜欢、喜欢、粉丝、谁看过我 - 翻页列表
     * @param type
     * 1 互相关注
     * 2 我关注
     * 3 粉丝
     * 4 谁看过我
     * @return
     */
    @GetMapping("/friends/{type}")
    public ResponseEntity queryUserLikeList(@PathVariable int type,
                                            @RequestParam (value = "page",defaultValue = "1") Long page,
                                            @RequestParam (value = "pagesize",defaultValue = "10") Long pageSize){
        PageResult<FriendVo> pageResult = userService.queryUserLikeList(type, page,pageSize);
        return ResponseEntity.ok(pageResult);
    }

    /**
     * 粉丝 - 喜欢
     * @return
     */
    @PostMapping("/fans/{fensId}")
    public ResponseEntity fansLike(@PathVariable Long fensId){
        userService.fansLike(fensId);
        return ResponseEntity.ok(null);
    }

}
