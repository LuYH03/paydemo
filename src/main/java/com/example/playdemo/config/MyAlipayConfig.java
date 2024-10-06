package com.example.playdemo.config;

import com.alipay.easysdk.kernel.Config;
import lombok.Data;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @Author: YHan
 * @Date: 2024/10/6 02:46
 * @Description:
 **/
@Configuration
@Data
public class MyAlipayConfig {
    @Bean
    public Config config(AliPayProperties payProperties){
        Config config = new Config();
        config.protocol = payProperties.getProtocol();;
        config.gatewayHost = payProperties.getGatewayHost();
        config.signType = payProperties.getSignType();
        config.appId = payProperties.getAppId();
        //为避免私钥随源码泄露，推荐从文件中读取私钥字符串而不是写入源码中
        config.merchantCertPath = payProperties.getMerchantPrivateKey();
        config.alipayPublicKey = payProperties.getAlipayPublicKey();
        //可设置异步通知接收服务地址（可选）
        config.notifyUrl = "";
        //可设置AES密钥，调用AES加解密相关接口时需要
        config.encryptKey = "";
        return config;
    }
}
