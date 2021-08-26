package com.tanhua.server.service;

import com.alibaba.fastjson.JSON;
import com.tanhua.commons.exception.TanHuaException;
import com.tanhua.commons.templates.FaceTemplate;
import com.tanhua.commons.templates.HuanXinTemplate;
import com.tanhua.commons.templates.OssTemplate;
import com.tanhua.commons.templates.SmsTemplate;
import com.tanhua.domain.db.User;
import com.tanhua.domain.db.UserInfo;
import com.tanhua.domain.mongo.RecommendUser;
import com.tanhua.domain.vo.*;
import com.tanhua.dubbo.api.UserApi;
import com.tanhua.dubbo.api.UserInfoApi;
import com.tanhua.dubbo.api.mongo.FriendApi;
import com.tanhua.dubbo.api.mongo.UserLikeApi;
import com.tanhua.dubbo.api.mongo.VisitorApi;
import com.tanhua.server.interceptor.UserHolder;
import com.tanhua.server.utils.GetAgeUtil;
import com.tanhua.server.utils.JwtUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.DateFormatUtils;
import org.apache.dubbo.config.annotation.Reference;
import org.apache.rocketmq.spring.core.RocketMQTemplate;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Service
@Slf4j
public class UserService {

    @Reference
    private UserApi userApi;

    @Reference
    private UserInfoApi userInfoApi;

    @Reference
    private FriendApi friendApi;

    @Reference
    private UserLikeApi userLikeApi;

    @Reference
    private VisitorApi visitorApi;

    @Autowired
    private RedisTemplate redisTemplate;

    @Autowired
    private SmsTemplate smsTemplate;

    @Autowired
    private JwtUtils jwtUtils;

    @Autowired
    private FaceTemplate faceTemplate;

    @Autowired
    private OssTemplate ossTemplate;

    @Autowired
    private HuanXinTemplate huanXinTemplate;

    @Autowired
    private RocketMQTemplate rocketMQTemplate;

    /**
     * 手机验证码key的前缀
     */
    @Value("${tanhua.redisValidateCodeKeyPrefix}")
    private String redisValidateCodeKeyPrefix;

    /**
     * 通过手机号码查询
     * @param phone
     * @return
     */
    public User findByMobile(String phone) {
        // 调用api查询
        User user = userApi.findByMobile(phone);
        return user;
    }

    /**
     * 创建用户
     * @param user
     */
    public void saveUser(User user) {
        userApi.save(user);
    }

    /**
     * 注册登陆 - 发送验证码
     * @param phone
     */
    public void sendValidateCode(String phone) {
        // 1. 通过手机号码来获取redis中的验证码
        String key = redisValidateCodeKeyPrefix + phone;
        String codeInRedis = (String) redisTemplate.opsForValue().get(key);
        log.info("codeInRedis: {},{}", codeInRedis, phone);
        // 2. redis中的验证码有值，则报错，验证码未失效
        if(null != codeInRedis){
            //验证码未失效
            throw new TanHuaException(ErrorResult.duplicate());
        }
        // 3. 没值，
        // 4. 生成验证码
        String validateCode = "123456";//RandomStringUtils.randomNumeric(6);

        // 5. 调用smsTemplate发送验证码
        //Map<String, String> sendResult = smsTemplate.sendValidateCode(phone, validateCode);
        //if(null != sendResult){
        //    // 发送验证返回不为空，发送失败
        //    throw new TanHuaException(ErrorResult.fail());
        //}
        // 6. 存入redis，设置有效期10分钟
        // p1: key, p2: value, p3:数值, p4: 计量单位
        log.info("发送验证码成功.{},{}",validateCode,phone);
        // 有效期10分钟
        redisTemplate.opsForValue().set(key, validateCode, 10, TimeUnit.MINUTES);
    }

    /**
     * 注册登陆-验证码校验
     * @param verificationCode
     * @param phone
     * @return
     */
    public Map<String, Object> loginVerification(String verificationCode, String phone) {
        //1. 验证码校验
        //1.1 拼接验证码的key
        String key = redisValidateCodeKeyPrefix + phone;
        //1.2 从redis中取验证码
        String codeInRedis = (String) redisTemplate.opsForValue().get(key);
        log.info("codeInRedis,verificationCode: {},{}",codeInRedis, verificationCode);
        //1.3 redis的验证码过期了，报错
        if(null == codeInRedis){
            throw new TanHuaException(ErrorResult.loginError());
        }
        //1.4 校验前端的与redis的验证码是否一致    【注意】取反 !
        if(!StringUtils.equals(codeInRedis, verificationCode)) {
            //1.5 不一致，报错
            throw new TanHuaException(ErrorResult.validateCodeError());
        }
        //1.6 正确，删除redis中的key, 防止重复提交
        redisTemplate.delete(key);
        //2. 判断用户是否已经注册过
        //2.1 通过手机号码查询, 调用userApi
        User user = userApi.findByMobile(phone);
        //2.2 不存在用户，则注册为新用户, userApi
        boolean isNew = false; // 默认是已注册
        String type = "0102";// 默认为登陆
        if(null == user){
            user = new User();
            user.setMobile(phone);
            // 使用手机号码后六位
            user.setPassword(DigestUtils.md2Hex(phone.substring(5)));
            Long userId = userApi.save(user);
            // 签发的token, 存入redis中的用户信息都有id
            user.setId(userId);
            // 新注册
            isNew = true;
            // 注册环信上的账号
            huanXinTemplate.register(userId);
            type = "0101";
        }
        //3. 签发token, 会用户id,手机号码
        String token = jwtUtils.createJWT(phone, user.getId());
        //3.1 以token为key, 以用户信息为value存入redis, 并且设置有效为7天
        String userJsonString = JSON.toJSONString(user);
        String tokenKey = "TOKEN_" + token;
        redisTemplate.opsForValue().set(tokenKey, userJsonString, 7, TimeUnit.DAYS);
        //4. 构建返回的数据{token:, isNew}
        Map<String,Object> result = new HashMap<String,Object>();
        result.put("token", token);
        result.put("isNew", isNew);
    //5. 发送mq消息
    // 构建消息的map
    Map<String,Object> logMap = new HashMap<>();
    logMap.put("userId", user.getId());
    logMap.put("type", type);
    logMap.put("log_time", DateFormatUtils.format(new Date(),"yyyy-MM-dd"));
    logMap.put("address","深圳黑马");
    logMap.put("equipment","iphone 13");
    // 转成json字符串
    String jsonString = JSON.toJSONString(logMap);
    log.info("log:" + jsonString);
    // 发送mq
    rocketMQTemplate.convertAndSend("tanhua_log", jsonString);
        return result;
    }

    /**
     * 新用户注册-填写信息
     * @param userInfoVo
     */
    public void loginReginfo(UserInfoVo userInfoVo) {
        // 1. 获取用户的id
        // 1.1 通过token从redis中获取登陆用户的信息
        //String tokenKey = "TOKEN_" + token;
        // 1.2 拼接登陆用户key, 取出redis中的值 是一个json字符串
        // 用户太长没使用了，有效7天，用户根据没登陆（用postman）
        // 1.3 解析为json字符串为用户信息
        User loginUser = UserHolder.getUser();
        // 2. vo转成userInfo对象
        UserInfo userInfo = new UserInfo();
        // 复制2个对象的属性值。要求：属性名必须一致，属性的类型也必须一致。属性名与类型都一致才会复制，否则不复制
        // p1: 从 复制源 复制给 P2: 目标对象
        BeanUtils.copyProperties(userInfoVo, userInfo);
        // 算出年龄
        userInfo.setAge(GetAgeUtil.getAge(userInfoVo.getBirthday()));
        // 设置用户信息的id，登陆用户的id
        userInfo.setId(loginUser.getId());
        // 3. 调用userInfoApi添加数据
        userInfoApi.add(userInfo);
    }

    /**
     * 新用户注册-选取头像
     * @param headPhoto
     */
    public void uploadAvatar(MultipartFile headPhoto) {
        //1. 获取登陆用户信息，校验用户是否有效
        User loginUser = UserHolder.getUser();
        //2. 调用百度人脸检测
        try {
            if (!faceTemplate.detect(headPhoto.getBytes())) {
                //3. 没通过则报没人脸
                throw new TanHuaException(ErrorResult.faceError());
            }
            //4. 通过，上传到阿里oss, 返回头像的url
            String filename = headPhoto.getOriginalFilename();
            String avatarUrl = ossTemplate.upload(filename, headPhoto.getInputStream());
            //5. 调用api更新用户头像
            UserInfo userInfo = new UserInfo();
            // 更新时where条件，更新哪个用户的
            userInfo.setId(loginUser.getId());
            // 更新上传用户的头像
            userInfo.setAvatar(avatarUrl);
            userInfoApi.update(userInfo);
        } catch (IOException e) {
            log.error("上传头像失败", e);
            throw new TanHuaException("上传头像失败");
        }
    }

    /**
     * 获取登陆用户信息
     * @return
     */
    public UserInfoVo getLoginUserInfo() {
        // 1. 通过token获取登陆用户id
        Long loginUserId = UserHolder.getUserId();
        // 2. 调用userInfoApi查询详情
        UserInfo userInfo = userInfoApi.findById(loginUserId);
        // 3. UserInfo 转成 vo
        UserInfoVo vo = new UserInfoVo();
        // 复制属性值
        BeanUtils.copyProperties(userInfo, vo);
        // 设置age属性，前端要求的是字符串
        vo.setAge(userInfo.getAge().toString());
        // 4. 返回vo
        return vo;
    }

    /**
     * 更新用户信息
     * @param vo
     */
    public void updateUserInfo(UserInfoVo vo) {
        //1. 通过token获取登陆用户id
        Long loginUserId = UserHolder.getUserId();
        //2. 把vo转成UserInfo
        UserInfo userInfo = new UserInfo();
        BeanUtils.copyProperties(vo, userInfo);
        //3. 计算年龄
        userInfo.setAge(GetAgeUtil.getAge(vo.getBirthday()));
        //4. 设置更新的用户id=登陆用户
        userInfo.setId(loginUserId);
        //5. 调用api更新
        userInfoApi.update(userInfo);
    }

    /**
     * 我喜欢 统计
     * @return
     */
    public CountsVo counts() {
        Long loginUserId = UserHolder.getUserId();
        //1. 查询好友数量
        Long eachLoveCount = friendApi.countByUserId(loginUserId);
        //2. 查询我喜欢的数量
        Long loveCount = userLikeApi.loveCount(loginUserId);
        //3. 查询喜欢我(粉丝)的数量
        Long fanCount = userLikeApi.fanCount(loginUserId);
        //4. 构建vo
        CountsVo vo = new CountsVo();
        vo.setFanCount(fanCount);
        vo.setEachLoveCount(eachLoveCount);
        vo.setLoveCount(loveCount);
        //5. 返回
        return vo;
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
    public PageResult<FriendVo> queryUserLikeList(int type, Long page, Long pageSize) {
        //1. 根据type调用api查询 返回统计的数据类型
        // 统一返回的类型RecommendUser
        PageResult pageResult = null;
        boolean alreadyLove = false;
        switch (type){
            case 1: pageResult = friendApi.findPageWithScore(UserHolder.getUserId(), page, pageSize);alreadyLove=true;break;
            case 2: pageResult = userLikeApi.findPageOneSideLike(UserHolder.getUserId(), page, pageSize);alreadyLove=true;break;
            case 3: pageResult = userLikeApi.findPageFens(UserHolder.getUserId(), page, pageSize);break;
            case 4: pageResult = visitorApi.findPageByUserId(UserHolder.getUserId(), page, pageSize);break;
            default: throw new TanHuaException("参数不正确!");
        }
        //2. 获取用户id集合
        List<RecommendUser> userList =  pageResult.getItems();
        if(!CollectionUtils.isEmpty(userList)) {
            List<Long> userIds = userList.stream().map(RecommendUser::getUserId).collect(Collectors.toList());
            //3. 批量查询用户信息
            List<UserInfo> userInfoList = userInfoApi.findByBatchId(userIds);
            //4. 转成map
            Map<Long, UserInfo> userInfoMap = userInfoList.stream().collect(Collectors.toMap(UserInfo::getId, u -> u));
            //5. 转成成vo
            final boolean finalAlreadyLove = alreadyLove;
            List<FriendVo> voList = userList.stream().map(recommendUser -> {
                FriendVo vo = new FriendVo();
                // 取出用户的信息
                UserInfo userInfo = userInfoMap.get(recommendUser.getUserId());
                // 属性复制
                BeanUtils.copyProperties(userInfo, vo);
                // 缘分值
                vo.setMatchRate(recommendUser.getScore().intValue());
                vo.setAlreadyLove(finalAlreadyLove);
                return vo;
            }).collect(Collectors.toList());
            pageResult.setItems(voList);
        }
        //6. 返回
        return pageResult;
    }

    /**
     * 粉丝 - 喜欢
     * @param fensId
     */
    public void fansLike(Long fensId) {
        //1. 获取登陆用户id
        Long loginUserId = UserHolder.getUserId();
        //2. 调用api，实现添加为好友的动态
        boolean flag = userLikeApi.fansLike(loginUserId, fensId);
        //3. 如果添加好友成功，在环信是也注册为好友
        if(flag){
            huanXinTemplate.makeFriends(loginUserId, fensId);
        }
    }
}
