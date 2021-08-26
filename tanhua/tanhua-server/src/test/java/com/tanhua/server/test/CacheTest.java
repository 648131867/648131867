package com.tanhua.server.test;

import com.tanhua.domain.db.UserInfo;
import com.tanhua.TanhuaServerApplication;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.List;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = TanhuaServerApplication.class)
public class CacheTest {

    @Autowired
    private TestUserInfoService userInfoService;

    @Test
    public void testFindAll() {
        List<UserInfo> list = userInfoService.findAll();
        for (UserInfo info : list) {
            System.out.println(info);
        }
    }

    @Test
    public  void testSave() {
        UserInfo userInfo = new UserInfo();
        userInfoService.save(userInfo);
    }

    @Test
    public  void testFind() {
        UserInfo info = userInfoService.findById(2l);
        System.out.println(info);
    }

    @Test
    public  void testUpdate() {
        UserInfo info = new UserInfo();
        info.setId(1l);
        info.setNickname("def");
        userInfoService.update(info);
    }
}