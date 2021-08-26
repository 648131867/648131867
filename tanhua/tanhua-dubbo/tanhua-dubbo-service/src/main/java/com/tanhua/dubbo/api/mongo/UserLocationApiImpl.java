package com.tanhua.dubbo.api.mongo;

import com.tanhua.domain.db.User;
import com.tanhua.domain.mongo.UserLocation;
import com.tanhua.domain.vo.UserLocationVo;
import org.apache.dubbo.config.annotation.Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.geo.Circle;
import org.springframework.data.geo.Distance;
import org.springframework.data.geo.Metrics;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.geo.GeoJsonPoint;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;

import java.util.List;


@Service
public class UserLocationApiImpl implements UserLocationApi {

    @Autowired
    private MongoTemplate mongoTemplate;

    /**
     * 保存用户的地理位置
     *
     * @param location
     * @param latitude y
     * @param longitude x
     */
    @Override
    public void save(UserLocation location, Double latitude, Double longitude) {
        //1. 构建查询的条件
        Query query = new Query(Criteria.where("userId").is(location.getUserId()));
        // 构建坐标 x=longitude, y=latitude
        GeoJsonPoint point = new GeoJsonPoint(longitude, latitude);
        long timeMillis = System.currentTimeMillis();
        //2. 判断是否存在用户的记录
        if (mongoTemplate.exists(query, UserLocation.class)) {
            //3. 存在则更新坐标
            Update update = new Update();
            update.set("location", point);
            update.set("address", location.getAddress());
            update.set("updated", timeMillis);
            mongoTemplate.updateFirst(query, update, UserLocation.class);
        }else {
            //4. 不存在则添加记录
            location.setLocation(point);
            location.setCreated(timeMillis);
            location.setUpdated(timeMillis);
            mongoTemplate.insert(location);
        }
    }

    /**
     * 搜附近
     *
     * @param loginUserId
     * @param distance
     * @return
     */
    @Override
    public List<UserLocationVo> searchNearBy(Long loginUserId, Long distance) {
        //1. 取出登陆用户的坐标
        //1.1 构建查询条件
        Query query = new Query(Criteria.where("userId").is(loginUserId));
        //1.2 查询出登陆用户信息
        UserLocation loginUserLocation = mongoTemplate.findOne(query, UserLocation.class);
        //2. 构建 半径>圆 distance单位是米,  KILOMETERS 1000米
        Distance radius = new Distance(distance/1000, Metrics.KILOMETERS);
        // 圆：p1:圆心坐标， p2: 半径
        Circle circle = new Circle(loginUserLocation.getLocation(), radius);
        //3. 查询
        // 条件, 排除登陆用户自己
        Query searchQuery = new Query(Criteria.where("userId").ne(loginUserId));
        // 以circle范围搜索
        searchQuery.addCriteria(Criteria.where("location").withinSphere(circle));
        List<UserLocation> userLocations = mongoTemplate.find(searchQuery, UserLocation.class);
        //4. 把查询到结果集转成vo(反序列失败问题GeoJsonPoint, x,y)
        return UserLocationVo.formatToList(userLocations);
    }
}
