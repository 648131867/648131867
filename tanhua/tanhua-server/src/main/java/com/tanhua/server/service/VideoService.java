package com.tanhua.server.service;

import com.github.tobato.fastdfs.domain.conn.FdfsWebServer;
import com.github.tobato.fastdfs.domain.fdfs.StorePath;
import com.github.tobato.fastdfs.service.FastFileStorageClient;
import com.tanhua.commons.exception.TanHuaException;
import com.tanhua.commons.templates.OssTemplate;
import com.tanhua.domain.db.UserInfo;
import com.tanhua.domain.mongo.FollowUser;
import com.tanhua.domain.mongo.Video;
import com.tanhua.domain.vo.PageResult;
import com.tanhua.domain.vo.VideoVo;
import com.tanhua.dubbo.api.UserInfoApi;
import com.tanhua.dubbo.api.mongo.VideoApi;
import com.tanhua.server.interceptor.UserHolder;
import lombok.extern.slf4j.Slf4j;
import org.apache.dubbo.config.annotation.Reference;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Slf4j
public class VideoService {

    @Reference
    private VideoApi videoApi;

    @Reference
    private UserInfoApi userInfoApi;

    @Autowired
    private OssTemplate ossTemplate;

    @Autowired
    private FastFileStorageClient client;

    @Autowired
    private FdfsWebServer fdfsWebServer;

    @Autowired
    private RedisTemplate redisTemplate;

    /**
     * 发布小视频
     * @param videoThumbnail 封面图片
     * @param videoFile
     */
    @CacheEvict(value = "videoList",allEntries = true)
    public void post(MultipartFile videoThumbnail, MultipartFile videoFile) {
        //1. 封面图片 上传到阿里oss上。要接收图片地址
        try {
            String picUrl = ossTemplate.upload(videoThumbnail.getOriginalFilename(), videoThumbnail.getInputStream());
            //2. 视频上传到 fastDFS，接收视频地址
            String videoFilename = videoFile.getOriginalFilename();
            String videoExtension = videoFilename.substring(videoFilename.lastIndexOf(".")+1);
            // p1: 文件的输入流，p2:文件大小, p3:文件的后缀名(不包含.)。p4:null
            StorePath storePath = client.uploadFile(videoFile.getInputStream(), videoFile.getSize(), videoExtension, null);
            String videoUrl = fdfsWebServer.getWebServerUrl()+storePath.getFullPath();
            //3. 构建video对象
            Video video = new Video();
            video.setVideoUrl(videoUrl);
            video.setPicUrl(picUrl);
            video.setUserId(UserHolder.getUserId());
            video.setText("黑马出品"); // 水印
            //4. 调用api保存视频数据到mongodb
            videoApi.add(video);
        } catch (IOException e) {
            log.error("上传视频封面失败",e);
            throw new TanHuaException("上传视频封面失败");
        }
    }


   //public PageResult getVideoList(Long page, Long pageSize){
   //    return videoApi.findPage(page,pageSize);
   //}

    /**
     * 小视频分页查询
     * @param page
     * @param pageSize
     * @return
     */
    @Cacheable(value = "videoList", key = "#page + '_' + #pageSize")
    public PageResult<VideoVo> findPage(Long page, Long pageSize) {
        // 1. 调用api分页查询
        PageResult pageResult = videoApi.findPage(page,pageSize);
        // 2. 获取结果集
        List<Video> videoList = pageResult.getItems();
        if(!CollectionUtils.isEmpty(videoList)) {
            // 3. 获取作者ids
            List<Long> userIds = videoList.stream().map(Video::getUserId).collect(Collectors.toList());
            // 4. 批量查询作者信息list
            List<UserInfo> userInfoList = userInfoApi.findByBatchId(userIds);
            // 5. 把作者信息转成map
            Map<Long, UserInfo> userInfoMap = userInfoList.stream().collect(Collectors.toMap(UserInfo::getId, u -> u));
            // 6. 小视频转成vo
            List<VideoVo> voList = videoList.stream().map(video -> {
                VideoVo vo = new VideoVo();
                // 7.  复制视频信息
                BeanUtils.copyProperties(video, vo);
                vo.setId(video.getId().toHexString());
                vo.setSignature(video.getText());
                vo.setCover(video.getPicUrl());
                //复制作者信息
                UserInfo userInfo = userInfoMap.get(video.getUserId());
                BeanUtils.copyProperties(userInfo, vo);
                // 登陆用户是否关注了这个作者
                String key = "follow_user_" + UserHolder.getUserId() + "_" + video.getUserId();
                if(redisTemplate.hasKey(key)){
                    vo.setHasFocus(1);//1: 关注了
                }
                return vo;
            }).collect(Collectors.toList());
            // 8. 设置回pageResult
            pageResult.setItems(voList);
        }
        // 9. 返回pageResult
        return pageResult;
    }

    /**
     * 关注视频的作者
     * @param followUserId
     */
    public void focusUser(Long followUserId) {
        //1. 构建followUser对象
        FollowUser followUser = new FollowUser();
        followUser.setUserId(UserHolder.getUserId());
        followUser.setFollowUserId(followUserId);
        //2. 调用api保存到关注表
        videoApi.followUser(followUser);
        //3. redis中标记登陆用户关注了视频的作者
        String key = "follow_user_" + UserHolder.getUserId() + "_" + followUserId;
        redisTemplate.opsForValue().set(key,1);
    }

    /**
     * 取消关注
     * @param followUserId
     */
    public void userUnFocus(Long followUserId) {
        //1. 构建关注的对象(删除条件)
        FollowUser followUser = new FollowUser();
        followUser.setUserId(UserHolder.getUserId());
        followUser.setFollowUserId(followUserId);
        //2. 调用api取消关系
        videoApi.unfollowUser(followUser);
        //3. 删除redis标记
        String key = "follow_user_" + UserHolder.getUserId() + "_" + followUserId;
        redisTemplate.delete(key);
    }
}
