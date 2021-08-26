package com.tanhua.dubbo.test;

import com.tanhua.domain.mongo.UserLocation;
import com.tanhua.dubbo.api.mongo.UserLocationApi;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.ArrayList;
import java.util.List;

@RunWith(SpringRunner.class)
@SpringBootTest
public class UserLocationTest {

    @Autowired
    private UserLocationApi userLocationApi;

    @Test
    public void addLocation(){
        String[] addresses = {"深圳黑马程序员","红荔村肠粉","深圳南头直升机场","深圳市政府","欢乐谷","世界之窗","东部华侨城","大梅沙海滨公园","深圳宝安国际机场","海雅缤纷城(宝安店)"};
        List<UserLocation> list = new ArrayList<>();
        for (long i = 1; i <= 10; i++) {
            UserLocation userLocation = new UserLocation();
            userLocation.setUserId(i);
            userLocation.setAddress(addresses[(int)i - 1]);
            list.add(userLocation);
        }

        userLocationApi.save(list.get(0),22.582111,113.929778);
        userLocationApi.save(list.get(1),22.587995,113.925528);
        userLocationApi.save(list.get(2),22.562578,113.93814);
        userLocationApi.save(list.get(3),22.549528,114.064478);
        userLocationApi.save(list.get(4),22.547726,113.986074);
        userLocationApi.save(list.get(5),22.540746,113.979399);
        userLocationApi.save(list.get(6),22.632275,114.294924);
        userLocationApi.save(list.get(7),22.598196,114.314011);
        userLocationApi.save(list.get(8),22.638172,113.821705);
        userLocationApi.save(list.get(9),22.566223,113.912386);
    }
}