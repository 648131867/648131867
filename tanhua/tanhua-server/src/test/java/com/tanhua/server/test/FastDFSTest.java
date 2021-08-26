package com.tanhua.server.test;

import com.github.tobato.fastdfs.domain.conn.FdfsWebServer;
import com.github.tobato.fastdfs.domain.fdfs.StorePath;
import com.github.tobato.fastdfs.service.FastFileStorageClient;
import com.tanhua.TanhuaServerApplication;
import org.apache.commons.io.FileUtils;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.File;
import java.io.IOException;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = TanhuaServerApplication.class)
public class FastDFSTest {

    @Autowired
    private FastFileStorageClient client;

    @Autowired
    private FdfsWebServer fdfsWebServer;

    @Test
    public void testUploadFile() throws IOException {
        File file = new File("D:\\work\\sz114\\02_上课内容\\day66_探花交友_day07\\资料\\01-视频图片\\超人跳.mp4");
        StorePath storePath = client.uploadFile(FileUtils.openInputStream(file), file.length(), "mp4", null);
        //group1/M00/00/00/wKjToGECC_eAIh5eAAnHyeWJ2cs024.mp4
        System.out.println(storePath.getFullPath());
        //M00/00/00/wKjToGECC_eAIh5eAAnHyeWJ2cs024.mp4
        System.out.println(storePath.getPath());

        //获取文件请求地址
        //http://192.168.211.160:8888/group1/M00/00/00/wKjToGECC_eAIh5eAAnHyeWJ2cs024.mp4
        String url = fdfsWebServer.getWebServerUrl()+storePath.getFullPath();
        System.out.println(url);
    }
}