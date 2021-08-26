package com.tanhua.server.controller;

import com.tanhua.domain.vo.ContactVo;
import com.tanhua.domain.vo.MessageVo;
import com.tanhua.domain.vo.PageResult;
import com.tanhua.server.service.IMService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/messages")
public class IMController {

    @Autowired
    private IMService imService;

    /**
     * 聊一聊后的交友
     * @param paramMap
     * @return
     */
    @PostMapping("/contacts")
    public ResponseEntity makeFriends(@RequestBody Map<String, Long> paramMap){
        imService.makeFriends(paramMap.get("userId"));
        return ResponseEntity.ok(null);
    }

    /**
     * 联系人列表
     * @return
     */
    @GetMapping("/contacts")
    public ResponseEntity queryContactsList(@RequestParam (value = "page",defaultValue = "1") Long page,
                                   @RequestParam (value = "pagesize",defaultValue = "10") Long pageSize,
                                   String keyword){
        PageResult<ContactVo> pageResult = imService.queryContactsList(page,pageSize,keyword);
        return ResponseEntity.ok(pageResult);
    }

    /**
     * 谁点赞了我 列表
     * @param page
     * @param pageSize
     * @return
     */
    @GetMapping("/likes")
    public ResponseEntity likes(@RequestParam (value = "page",defaultValue = "1") Long page,
                                @RequestParam (value = "pagesize",defaultValue = "10") Long pageSize){
        // 点赞的commentType=1
        int commentType = 1;
        PageResult<MessageVo> pageResult = imService.messageCommentList(commentType,page, pageSize);
        return ResponseEntity.ok(pageResult);
    }

    /**
     * 谁喜欢了我 列表
     * @param page
     * @param pageSize
     * @return
     */
    @GetMapping("/loves")
    public ResponseEntity loves(@RequestParam (value = "page",defaultValue = "1") Long page,
                                @RequestParam (value = "pagesize",defaultValue = "10") Long pageSize){
        // 喜欢的commentType=3
        int commentType = 3;
        PageResult<MessageVo> pageResult = imService.messageCommentList(commentType,page, pageSize);
        return ResponseEntity.ok(pageResult);
    }

    /**
     * 谁评论了我 列表
     * @param page
     * @param pageSize
     * @return
     */
    @GetMapping("/comments")
    public ResponseEntity comments(@RequestParam (value = "page",defaultValue = "1") Long page,
                                @RequestParam (value = "pagesize",defaultValue = "10") Long pageSize){
        // 评论的commentType=2
        int commentType = 2;
        PageResult<MessageVo> pageResult = imService.messageCommentList(commentType,page, pageSize);
        return ResponseEntity.ok(pageResult);
    }
}
