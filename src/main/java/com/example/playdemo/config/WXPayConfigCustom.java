package com.example.playdemo.config;

import com.github.wxpay.sdk.IWXPayDomain;
import com.github.wxpay.sdk.WXPayConfig;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;

/**
 * @Author: YHan
 * @Date: 2024/10/6 03:59
 * @Description:
 **/
public class WXPayConfigCustom extends WXPayConfig {

    /**
     * 开发者ID(AppID)
     * @return
     */
    @Override
    protected String getAppID() {
        return "wx0ca99a203b1e9943";
    }

    /**
     * 商户号
     * @return
     */
    @Override
    protected String getMchID() {
        return "1561414331";
    }

    /**
     * API密钥
     * @return
     */
    @Override
    protected String getKey() {
        return "CZBK51236435wxpay435434323FFDuis";
    }

    //退款：必须强制使用API证书
    @Override
    protected InputStream getCertStream() {
        try{
            String path = ClassLoader.getSystemResource("").getPath();
            return new FileInputStream(new File(path + "apiclient_cert.p12")); //apiclient_cert.p12证书暂时没有
        } catch (FileNotFoundException e){
            e.printStackTrace();
        }
        return null;
    }

    @Override
    protected IWXPayDomain getWXPayDomain() {
        return null;
    }
}
