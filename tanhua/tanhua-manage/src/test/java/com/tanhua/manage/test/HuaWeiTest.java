package com.tanhua.manage.test;

import com.tanhua.commons.templates.HuaWeiUGCTemplate;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest
public class HuaWeiTest {

    @Autowired
    private HuaWeiUGCTemplate template;

    @Test
    public void testToken() {
        System.out.println(template.getToken());
    }

    @Test
    public void testText() {
        boolean check = template.textContentCheck("好好先生,他妈的，什么鬼!");
        System.out.println(check);
    }

    @Test
    public void testImages() {
        String[] urls = new String[]{
                "https://sz-114.oss-cn-shenzhen.aliyuncs.com/xdd.png"
        };
        boolean check = template.imageContentCheck(urls);
        System.out.println(check);
    }
}