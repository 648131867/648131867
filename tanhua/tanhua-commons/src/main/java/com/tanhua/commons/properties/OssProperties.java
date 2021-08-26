package com.tanhua.commons.properties;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties(prefix = "tanhua.oss")
public class OssProperties {
    private String endpoint;
    private String accessKeyId;
    private String accessKeySecret;
    private String bucketName; // sz-114
    private String url;//https://sz-114.oss-cn-shenzhen.aliyuncs.com
}