package com.example.playdemo.config;

import lombok.Data;

/**
 * @Author: YHan
 * @Date: 2024/10/5 00:45
 * @Description:
 **/

@Data
public class AlipayConfig {
    //网关地址
    private String serverUrl;
    //应用ID
    private String appId;
    //应用私钥
    private String privateKey;
    //请求格式
    private String format;
    //字符集
    private String charset;
    //签名类型
    private String signType;
    //设置支付宝公钥
    private String alipayPublicKey;

}
