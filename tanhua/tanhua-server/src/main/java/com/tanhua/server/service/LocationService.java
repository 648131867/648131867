package com.tanhua.server.service;

import com.tanhua.domain.mongo.UserLocation;
import com.tanhua.dubbo.api.mongo.UserLocationApi;
import com.tanhua.server.interceptor.UserHolder;
import org.apache.dubbo.config.annotation.Reference;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public class LocationService {

    @Reference
    private UserLocationApi userLocationApi;

    /**
     * 上报地理位置
     * @param paramMap
     */
    public void reportLocation(Map<String, Object> paramMap) {
        //1. 取出经纬度
        Double latitude = (Double) paramMap.get("latitude");//纬度 y
        Double longitude = (Double) paramMap.get("longitude");//经度 x
        //2. 取所在地理位置的描述
        String addrStr = (String) paramMap.get("addrStr");
        //3. 构建userlocation对象
        UserLocation location = new UserLocation();
        location.setAddress(addrStr);
        location.setUserId(UserHolder.getUserId());
        //4. 调用api保存登陆用户的地理位置
        userLocationApi.save(location, latitude, longitude);
    }
}
