package com.tanhua.dubbo.api.mongo;

import com.tanhua.domain.mongo.UserLocation;
import com.tanhua.domain.vo.UserLocationVo;

import java.util.List;

public interface UserLocationApi {
    /**
     * 保存用户的地理位置
     * @param location
     * @param latitude
     * @param longitude
     */
    void save(UserLocation location, Double latitude, Double longitude);

    /**
     * 搜附近
     * @param loginUserId
     * @param distance
     * @return
     */
    List<UserLocationVo> searchNearBy(Long loginUserId, Long distance);
}
