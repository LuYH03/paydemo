package com.example.playdemo.controller;

import cn.hutool.extra.qrcode.QrCodeUtil;
import cn.hutool.extra.qrcode.QrConfig;
import com.alipay.api.AlipayApiException;
import com.alipay.api.internal.util.file.IOUtils;
import com.example.playdemo.config.WXPayConfigCustom;
import com.github.wxpay.sdk.WXPay;
import com.github.wxpay.sdk.WXPayUtil;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

/**
 * @Author: YHan
 * @Date: 2024/10/6 04:12
 * @Description:
 **/
@RestController
@RequestMapping("wxpay")
public class WxPayController {
    /**
     *  当面付 统一创建交易生成二维码接口
     * @param orderNo 订单ID
     * @return
     */
    @RequestMapping("precreate")
    public String precreate(String orderNo) throws Exception {
        WXPay wxPay = new WXPay(new WXPayConfigCustom());

        Map<String, String> paramMap = new HashMap<>();
        paramMap.put("body","神岭物流运费");
        paramMap.put("out_trade_no", orderNo);
        paramMap.put("total_fee", "1"); //金额 单位：分
        paramMap.put("spbill_create_ip", "123.12.12.123");
        paramMap.put("notify_url", "外网地址/wxpay/notify"); //可使用内网穿透
        paramMap.put("trade_type","NATIVE ");

        Map<String, String> result = wxPay.unifiedOrder(paramMap);
        String codeUrl = result.get("code_url");
        return createQrCode(codeUrl);
    }

    /**
     * 查询交易
     * @param orderNo
     * @return
     */
    @RequestMapping("query")
    public String query(String orderNo) throws Exception {
        WXPay wxPay = new WXPay(new WXPayConfigCustom());

        Map<String, String> paramMap = new HashMap<>();
        paramMap.put("out_trade_no", orderNo);
        Map<String, String> result = wxPay.orderQuery(paramMap);
        return result.get("trade_state");
    }
    /**
     * 交易退款
     * @param orderNo 订单号
     * @param refundNo 退款ID
     * @return code 10000表示退款成功
     */
    public Map<String, String> refund(String orderNo, String refundNo) throws Exception {
        WXPay wxPay = new WXPay(new WXPayConfigCustom());

        Map<String, String> paramMap = new HashMap<>();
        paramMap.put("out_trade_no", orderNo);
        paramMap.put("out_refund_no", refundNo);
        paramMap.put("total_fee", "1");
        paramMap.put("refund_fee", "1");

        Map<String, String> result = wxPay.refund(paramMap);
        return result;
    }

    /**
     * 交易退款查询
     * @param orderNo 订单ID
     * @param refundNo 退款ID
     * @return
     * @throws AlipayApiException
     */
    public Map<String, String> refundQuery(String orderNo, String refundNo) throws Exception {
        WXPay wxPay = new WXPay(new WXPayConfigCustom());

        Map<String, String> paramMap = new HashMap<>();
        paramMap.put("out_trade_no", orderNo);
        paramMap.put("out_refund_no", refundNo);

        Map<String, String> result = wxPay.refundQuery(paramMap);
        return result;
    }

    /**
     * 支付回调通知
     * @param request
     * @return
     */
    @RequestMapping("notify")
    public String payNotify(HttpServletRequest request) {
        try {
            Map<String, String> resultMap = new HashMap<>();
            String xmlResult = IOUtils.toString(request.getInputStream(), request.getCharacterEncoding());
            Map<String, String> map = WXPayUtil.xmlToMap(xmlResult);
            //1. 验证签名
            WXPay wxPay = new WXPay(new WXPayConfigCustom());
            boolean signatureValid = wxPay.isResponseSignatureValid(map);
            if (!signatureValid) {
                System.out.println("验证签名失败");
                resultMap.put("return_code", "FAIL");
                resultMap.put("return_msg", "验证签名失败");
                return WXPayUtil.mapToXml(resultMap);
            }
            //2. 根据交易单id 查询数据库中的交易单
            System.out.println("根据交易单id 查询数据库中的交易单 " + map.get("out_trade_no"));
            //3. 对比金额是否一致
            //4.  加分布式锁
            //5. 判断交易单的状态
            //6.根据支付的结果通知  修改交易当状态
            //7.解锁
            //返回微信支付结果
            System.out.println("验证签名成功");
            resultMap.put("return_code", "SUCCESS");
            resultMap.put("return_msg", "验证签名成功");
            return WXPayUtil.mapToXml(resultMap);
        } catch (Exception e){
            throw new RuntimeException();
        }
    }

    public String createQrCode(String qrCode) {
        return QrCodeUtil.generateAsBase64(qrCode, new QrConfig(500, 500), "jpg");
    }
}
