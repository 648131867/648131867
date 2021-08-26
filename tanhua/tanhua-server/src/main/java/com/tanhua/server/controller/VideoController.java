package com.tanhua.server.controller;

import com.tanhua.domain.vo.PageResult;
import com.tanhua.domain.vo.VideoVo;
import com.tanhua.server.service.VideoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/smallVideos")
public class VideoController {

    @Autowired
    private VideoService videoService;

    /**
     * 发布小视频
     * @param videoThumbnail
     * @param videoFile
     * @return
     */
    @PostMapping
    public ResponseEntity post(MultipartFile videoThumbnail, MultipartFile videoFile){
        videoService.post(videoThumbnail, videoFile);
        return ResponseEntity.ok(null);
    }

    /**
     * 小视频分页查询
     * @param page
     * @param pageSize
     * @return
     */
    @GetMapping
    public ResponseEntity findPage(@RequestParam(value = "page",defaultValue = "1") Long page,
                                   @RequestParam (value = "pagesize",defaultValue = "10") Long pageSize){
        PageResult<VideoVo> pageResult = videoService.findPage(page,pageSize);
        //PageResult pageResult = videoService.getVideoList(page, pageSize);
        return ResponseEntity.ok(pageResult);
    }

    /**
     * 关注视频的作者
     * @param followUserId
     * @return
     */
    @PostMapping("/{followUserId}/userFocus")
    public ResponseEntity focusUser(@PathVariable Long followUserId){
        videoService.focusUser(followUserId);
        return ResponseEntity.ok(null);
    }

    /**
     * 取消关注视频的作者
     * @param followUserId
     * @return
     */
    @PostMapping("/{followUserId}/userUnFocus")
    public ResponseEntity userUnFocus(@PathVariable Long followUserId){
        videoService.userUnFocus(followUserId);
        return ResponseEntity.ok(null);
    }
}
