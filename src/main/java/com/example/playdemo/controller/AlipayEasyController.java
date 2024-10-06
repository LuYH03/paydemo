package com.example.playdemo.controller;

import cn.hutool.extra.qrcode.QrCodeUtil;
import cn.hutool.extra.qrcode.QrConfig;
import com.alipay.api.AlipayApiException;
import com.alipay.easysdk.factory.Factory;
import com.alipay.easysdk.kernel.Config;
import com.alipay.easysdk.payment.common.models.AlipayTradeFastpayRefundQueryResponse;
import com.alipay.easysdk.payment.common.models.AlipayTradeRefundResponse;
import com.example.playdemo.config.AlipayConfig;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @Author: YHan
 * @Date: 2024/10/6 02:28
 * @Description:
 **/
@RestController
@RequestMapping("alipay_easy")
public class AlipayEasyController {
    @Autowired
    private Config config;

    /**
     *  当面付 统一创建交易生成二维码接口
     * @param orderNo 订单ID
     * @return
     */
    @RequestMapping("precreate")
    public String precreate(String orderNo) throws Exception {
        // 返回二维码链接
        Factory.setOptions(config);
        String qrCode = Factory.Payment.FaceToFace()
                .asyncNotify("暴露的公网地址/alipay_easy/notify")
                .preCreate("神岭物流运费", orderNo, "0.01").getQrCode();
        return createQrCode(qrCode);
    }

    /**
     * 查询交易
     * @param orderNo
     * @return
     */
    @RequestMapping("query")
    public String query(String orderNo) throws Exception {
        Factory.setOptions(config);
        String tradeStatus = Factory.Payment.Common().query(orderNo).getTradeStatus();
        return tradeStatus;
    }

    /**
     * 交易退款
     * @param orderNo 订单号
     * @param refundNo 退款ID
     * @return code 10000表示退款成功
     */
    public AlipayTradeRefundResponse refund(String orderNo, String refundNo) throws Exception {
        Factory.setOptions(config);
        AlipayTradeRefundResponse response = Factory.Payment.Common().refund(orderNo, "0.01");
        return response;
    }

    /**
     * 交易退款查询
     * @param orderNo 订单ID
     * @param refundNo 退款ID
     * @return
     * @throws AlipayApiException
     */
    public AlipayTradeFastpayRefundQueryResponse refundQuery(String orderNo, String refundNo) throws Exception {
        Factory.setOptions(config);
        // 如果发起退款时未传 refundNo(退款ID) 则查询退款时queryRefund(param1, param2) param2值为订单ID
        AlipayTradeFastpayRefundQueryResponse response = Factory.Payment.Common().queryRefund(orderNo, orderNo);
        return response;
    }

    /**
     * 支付成功后异步通知
     * @param request
     * @return
     */
    @RequestMapping("notify")
    public String payNotify(HttpServletRequest request) throws Exception {
        Map<String, String[]> parameterMap = request.getParameterMap();
        Map<String, String> paramMap = new HashMap<>();
        parameterMap.forEach((k, v) -> {
            paramMap.put(k, Arrays.stream(v).collect(Collectors.joining()));
        });
        //1. 验签
        Factory.setOptions(config);
        Boolean aBoolean = Factory.Payment.Common().verifyNotify(paramMap);
        if (!aBoolean) {
            System.out.println("验证签名失败");
        }
        //2. 根据交易单id 查询数据库中的交易单
        //3. 对比金额是否一致
        //4.  加分布式锁
        //5. 判断交易单的状态
        //6.根据支付的结果通知  修改交易当状态
        //7.解锁
        //返回支付宝支付结果

        System.out.println(paramMap);
        return "succes";
    }


    /**
     * Hutool生成二维码base64
     * @param qrCode
     * @return
     */
    public String createQrCode(String qrCode) {
        return QrCodeUtil.generateAsBase64(qrCode, new QrConfig(500, 500), "jpg");
    }



}
