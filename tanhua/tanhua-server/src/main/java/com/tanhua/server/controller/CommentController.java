package com.tanhua.server.controller;

import com.tanhua.domain.vo.CommentVo;
import com.tanhua.domain.vo.PageResult;
import com.tanhua.server.service.CommentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/comments")
public class CommentController {
    
    @Autowired
    private CommentService commentService;

    /**
     * 分页查询某个动态的评论列表
     * @param page
     * @param pageSize
     * @param movementId
     * @return
     */
    @GetMapping
    public ResponseEntity findPage(@RequestParam(value = "page",defaultValue = "1") Long page,
                                   @RequestParam (value = "pagesize",defaultValue = "1") Long pageSize,
                                   String movementId){
        PageResult<CommentVo> pageResult = commentService.findPage(movementId, page, pageSize);
        return ResponseEntity.ok(pageResult);
    }

    /**
     * 发表评论
     * @param paramMap
     * @return
     */
    @PostMapping
    public ResponseEntity add(@RequestBody Map<String,String> paramMap){
        commentService.add(paramMap);
        return ResponseEntity.ok(null);
    }

    /**
     * 对评论 点赞
     * @param commentId
     * @return
     */
    @GetMapping("/{commentId}/like")
    public ResponseEntity likeComment(@PathVariable String commentId){
        long count = commentService.likeComment(commentId);
        return ResponseEntity.ok(count);
    }

    /**
     * 对评论 点赞
     * @param commentId
     * @return
     */
    @GetMapping("/{commentId}/dislike")
    public ResponseEntity dislikeComment(@PathVariable String commentId){
        long count = commentService.dislikeComment(commentId);
        return ResponseEntity.ok(count);
    }
    
}
