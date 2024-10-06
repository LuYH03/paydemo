package com.example.playdemo.controller;

import cn.hutool.core.map.MapUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.extra.qrcode.QrCodeUtil;
import cn.hutool.extra.qrcode.QrConfig;
import cn.hutool.json.JSONUtil;
import com.alipay.api.AlipayApiException;
import com.alipay.api.AlipayClient;
import com.alipay.api.DefaultAlipayClient;
import com.alipay.api.request.*;
import com.alipay.api.response.AlipayTradeFastpayRefundQueryResponse;
import com.alipay.api.response.AlipayTradePrecreateResponse;
import com.alipay.api.response.AlipayTradeQueryResponse;
import com.alipay.api.response.AlipayTradeRefundResponse;
import com.example.playdemo.config.AlipayConfig;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * @Author: YHan
 * @Date: 2024/10/5 00:36
 * @Description:
 **/
@RestController
@RequestMapping("alipay")
public class AlipayController {

    /**
     * 创建交易
     * @param orderNo 订单ID
     * @return
     */
    @RequestMapping("precreate")
    public String precreate(String orderNo) {
        // 返回二维码链接
        AlipayTradePrecreateRequest request = new AlipayTradePrecreateRequest();

        Map<Object, Object> build = MapUtil.builder()
                .put("out_trade_no", orderNo)
                .put("total_amount", "1000")
                .put("subject", "测试")
                .build();
        request.setBizContent(JSONUtil.toJsonStr(build));
        AlipayClient client = client();
        try {
            AlipayTradePrecreateResponse response = client.execute(request);
            String qrCode = response.getQrCode();

            return createQrCode(qrCode);
        }catch (AlipayApiException e){
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    /**
     * 查询交易
     * @param orderNo
     * @return
     */
    @RequestMapping("query")
    public String query(String orderNo) throws AlipayApiException {
        AlipayTradeQueryRequest request = new AlipayTradeQueryRequest();
        Map<Object, Object> build = MapUtil.builder().put("out_trade_no", orderNo).build();
        request.setBizContent(JSONUtil.toJsonStr(build));

        AlipayClient client = client();
        AlipayTradeQueryResponse response = client.execute(request);
        System.out.println(response);
        String tradeStatus = response.getTradeStatus();
        boolean flag = ObjectUtil.equal(tradeStatus, "TRADE_SUCCESS");
        if (flag) {
            return "交易成功";
        }else {
            return "交易失败";
        }
    }

    /**
     * 交易退款
     * @param orderNo 订单号
     * @param refundNo 退款ID
     * @return code 10000表示退款成功
     */
    public String refund(String orderNo, String refundNo) throws AlipayApiException {
        //返回退款请求 是否发起成功
        AlipayTradeRefundRequest request = new AlipayTradeRefundRequest();
        Map<Object, Object> build = MapUtil.builder()
                .put("out_trade_no", orderNo)
                .put("refund_amount", "3000.00")
                .put("out_request_no", refundNo) //退款ID 非必传 如果不是一次性退款 是分笔数退款是必传参数
                .build();
        request.setBizContent(JSONUtil.toJsonStr(build));
        AlipayClient client = client();
        AlipayTradeRefundResponse response = client.execute(request);
        String code = response.getCode();
        return code;
    }

    /**
     * 交易退款查询
     * @param orderNo 订单ID
     * @param refundNo 退款ID
     * @return
     * @throws AlipayApiException
     */
    public String refundQuery(String orderNo, String refundNo) throws AlipayApiException {
        AlipayTradeFastpayRefundQueryRequest request = new AlipayTradeFastpayRefundQueryRequest();
        Map<Object, Object> build = MapUtil.builder()
                .put("out_trade_no", orderNo)
                .put("out_request_no", refundNo)
                .build();
        request.setBizContent(JSONUtil.toJsonStr(build));
        AlipayClient client = client();
        AlipayTradeFastpayRefundQueryResponse response = client.execute(request);
        String refundStatus = response.getRefundStatus();
        if (ObjectUtil.equal(refundNo, "REFUND_SUCCESS")){
            return "退款已到账";
        }else{
            return "退款未到账";
        }
    }

    /**
     * Hutool生成二维码base64
     * @param qrCode
     * @return
     */
    public String createQrCode(String qrCode) {
        return QrCodeUtil.generateAsBase64(qrCode, new QrConfig(500, 500), "jpg");
    }

    /**
     * 发送阿里客户端
     * @return
     */
    public AlipayClient client(){
        AlipayConfig alipayConfig = new AlipayConfig();
        alipayConfig.setServerUrl("https://openapi-sandbox.dl.alipaydev.com/gateway.do");
        alipayConfig.setAppId("9021000141609685");
        alipayConfig.setPrivateKey("MIIEvAIBADANBgkqhkiG9w0BAQEFAASCBKYwggSiAgEAAoIBAQCFGEb3xT+VrsTXjm/2CmJVaEDY9jO//U0lBD99Wa1ObpS1vVuPK9o6mISGy2dsapgCvmAmSXEelR6IeYzjP8DYimpmUXP+mOqTa9Vl/PdLYrom/Ca+ZkQJfro47MXWPtWgiIthXU36W5oqzBimDgigkIa5GZ20Swmj3Gyauc9mTZbS3Fw/p8R2M4xB4bZqLLDKaBXk5Iuffyv571+M3CesO+ficH5WwicbGepPXNT7VzxZ/eoBSLdZlp39edS+6/oYwLoarjW25k2hB/SlrnySpfN420XTXiL6K6pAmvgupN2ZiQ0nCl8f4VQczvId4/tIBTGZwHkle+88EvqXJPpxAgMBAAECggEAEdSqzU7zsc30ORywCzpg6ihJZ8e0Lyh0SV1EVDrLaRKPgbmYppmtZuQ//P8LIA83xkZTA04bhYlcydPe+CL2ZwxELOkErTs3UgDEWiGFz+euIMLvPdrdipgbwryV1JoebJ9ebPDfHYdgJBOrybNfnINASVQROLJE3FsPgbsa+g2BeAPJXJ7v61K91x0MU6xLxraRyaaM0KE3XnestHboENh/nv+qPXFLF5yvxSh1UDJq37UMv6sXR7s9ogb0HhypxuuroW3kmy5PNXDxW+93pD6S2mPjqR5HJkqsAnDC2vYvBJmIwCwOkojDp0igI/HNzM2xT/n7zBET57c88STtQQKBgQDDuxe0i+OLNRP8HRVIaDCV8Da0jH4y5pQnybsYP7VHEi6o1S3rb7uNtf/PUQ7lVwwO+QKcyE5uidbyBWPHL9FXYfCXqVInMOj/vd3gixcdOotoyQbCXgZ044Loip4fVv6pT4sx2koXdmyKmnaDQIkCwX/fn/zuSeDRKmwbcAyKzQKBgQCuE8Ql/8y4x8B6C/0yY7OT8RmwpXtEkcnG96rMIy1GxpZjVoYQ1EMdKLHbflso4yoU8QUzqPImrHQOe74rRKwTKARtK3RfbrICUblVY+05Nwh9mKheOXSYnLqmgQQKTRBlMSI0lm/XvU1rY25CaZpdruOcc+UFfc+Z586fdlk2NQKBgBx9J+ckkVsav6zrgsSGPcQlMaE37cp0202rRCs0YqtUjywN02A4ZCEtQ22mi7d/4wZu4h113kyijYGunSZJ5HuGiuFYSNfhe5h1x0y+kf+0HMSEthImY2QyFUTJoFhJbkmwhT8dWbluiD/oWaWjCA45oeYnwEXixPFM7m1T8s9lAoGAIjuQ30gmok/l8hpZeIdS0U0gmDRywVFKNO6PkAV66egzuuVRCA42BI30LA31L1088VFUjIw00pYnXJHJHAktLbc4558cC4hhSUN5BunXxOClss1Kjv0NiNWJfyTv/naZOs0mxOCwreR0TSIIlNbfnHzAFgxUhXfwU3vajlFfyeECgYALJC/nKL9MwbIY9fbX0Qeqf48BzEYvJnHQ6gyXuU1SZrE87SfuPk1KNVfKrf5wEW2MC3YP4p5b2+glcMlY5rqQXC4JbHeSKcXdtcYzTXF88YhGFdc1I2J19Jf7JF/OLnpSfrgY4Da2oUvSnZv0ouDrEv9577gjgteACroorrqQmQ==");
        alipayConfig.setFormat("json");
        alipayConfig.setCharset("GBK");
        alipayConfig.setSignType("RSA2");
        alipayConfig.setAlipayPublicKey("MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAspGMJqQX+MjGiAjGbitWnhfNa8FALcCeCKdtsCPxbMm+8pp0ebVoFwN7trfN1GAzq76ReH3hQhwGwU15tnbRfH5jOgJ62bspt9zsJDlyO+DuDRgS9P74jZB7doA5F54cz2ic43ytPGJdvgfE8GEHpQujDf7P3qOHig7j9OSsED8LSiFKiPYnBCRECURv/+OsY4Oy9n3vRe4xG4phrirWEmWPuxSSoXSvOVSezJ84D9vj8+hYX3GZSgA46HwMVPRazyA1XFs4i2BC4IqvCjR4czXz7uvizmmPdy5C+Z/22w4j+PXp0LBuFBVcj3jJ034cdU/dR1ReMiqY0bBpBiuvYwIDAQAB");

       try {
           return new DefaultAlipayClient(alipayConfig.getServerUrl(),
                   alipayConfig.getAppId(),
                   alipayConfig.getPrivateKey(),
                   alipayConfig.getFormat(),
                   alipayConfig.getCharset(),
                   alipayConfig.getAlipayPublicKey(),
                   alipayConfig.getSignType()
           );
       }catch (Exception e){
           e.printStackTrace();
           System.out.println("初始化支付宝配置错误");
           throw new RuntimeException("初始化支付宝配置错误");
       }
    }

}
