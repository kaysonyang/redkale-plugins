/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.redkalex.pay;

import java.lang.reflect.Type;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.security.*;
import java.security.spec.*;
import java.util.*;
import java.util.logging.*;
import java.util.stream.Collectors;
import javax.annotation.Resource;
import org.redkale.convert.json.*;
import org.redkale.util.*;
import static org.redkalex.pay.Pays.*;

/**
 *
 * @author zhangjx
 */
public class AliPayService extends AbstractPayService {

    //private static final Charset UTF8 = Charset.forName("UTF-8");
    private static final String format = "%1$tY-%1$tm-%1$td %1$tH:%1$tM:%1$tS"; //yyyy-MM-dd HH:mm:ss

    private static final Type TYPE_REPONSE_MAP = new TypeToken<HashMap<String, HashMap<String, String>>>() {
    }.getType();

    public static final int PAY_WX_ERROR = 4012101;//微信支付失败

    public static final int PAY_FALSIFY_ORDER = 4012017;//交易签名被篡改

    public static final int PAY_STATUS_ERROR = 4012018;//订单或者支付状态不正确

    protected final Logger logger = Logger.getLogger(this.getClass().getSimpleName());

    protected final boolean fine = logger.isLoggable(Level.FINE);

    protected final boolean finer = logger.isLoggable(Level.FINER);

    protected final boolean finest = logger.isLoggable(Level.FINEST);

    @Resource(name = "property.pay.alipay.merchno") //商户ID
    protected String merchno = "xxxxxxxxxxx";

    @Resource(name = "property.pay.alipay.charset") //字符集 用UTF-8没调试通过
    protected String charset = "GBK";

    @Resource(name = "property.pay.alipay.appid") //公众账号ID
    protected String appid = "2015051100069126";

    @Resource(name = "property.pay.alipay.notifyurl") //回调url
    protected String notifyurl = "http: //xxxxxx";

    @Resource(name = "property.pay.alipay.signkey") //签名算法需要用到的秘钥
    protected String signkey = "MIICdgIBADANBgkqhkiG9w0BAQEFAASCAmAwggJcAgEAAoGBAMvPGb+aJQX0RPjs"
        + "x6iZUbcujk9GZhVT1Z7N5hky6rZWkOmO2VLwGaY1zyMwHnPkb3fYcv8lhB/+9LsG"
        + "sPTdSl1qYOyApI1KLyXZTK/qmHHT9uiX1oz02uwNFuSZb9i7FbYth1vEuuM3qnZE"
        + "7WmgNQkmcted9JF0f/0jK9IOqqNBAgMBAAECgYAKQmOWbIj+krxCF5E5YHZnlTVe"
        + "sjmDS1QOiWjSzehYw2TKDQHNlf6EimLh75Mo3E/sJX4sb9QF1Ey3eW/A877BfBDg"
        + "UBDuXJUQXzUL3MJFD0w6+Zsx/xPqQmCl9gYwiR7DMeUKtgUrMmYFQFCELzwlM3st"
        + "uVPcjLXMaXT8M0EeDQJBAOcDRH02CpfwvodqSzJxy0TFZtaTbKX39TwxJFcSFfV3"
        + "fNYDnSdYTYswQmUWpOLP0hoxrcMGlPbhGzPjSACULE8CQQDh2o/OkVHE1aXr4u5l"
        + "Q8OxUIaATGQvstOQL9UA8DmNab4QLrn0Ol8T0p2J4IHeWXa1UFtm34/2amp9Vjkt"
        + "kYNvAkARv3uEjyFTOQi6SJ1MW9e9CdlzxNHFEn7ByBi9o8MSH8L0gkSRoEQc3HFN"
        + "aOb0EflXT9fEsv3A1dyMKPsAKGIbAkEAv9meysOah+9MMCHmi9KSSu6yMg2yFOp8"
        + "2EApWdC1srAeKTTn9NQYq4f/Fn3FE5E/SyllWu+RJKqkpq81hsXStQJADwlOtdyi"
        + "k8N5DvlCSt2rsBsskz4Uiv3KUCwCqq+Lt6g/uFkrTcoBR7GHKOHyyk+l+aJjtxnD"
        + "ONuh2psnu0N1vg==";

    @Resource(name = "property.pay.alipay.publickey") //公钥
    protected String publickey = "MIGfMA0GCSqGSIb3DQEBAQUAA4GNADCBiQKBgQDDI6d306Q8fIfCOaTXyiUeJHkrIvYISRcc73s3vF1ZT7XN8RNPwJxo8pWaJMmvyTn9N4HQ632qJBVHf8sxHi/fEsraprwCtzvzQETrNRwVxLO5jVmRGi60j8Ue1efIlzPXV9je9mkjzOmdssymZkh2QhUrCmZYI/FCEa3/cNMW0QIDAQAB";

    @Resource
    protected JsonConvert convert;

    @Override
    public PayResponse create(PayCreatRequest request) {
        request.checkVaild();
        final PayResponse result = new PayResponse();
        try {
            final TreeMap<String, String> map = new TreeMap<>();
            map.put("app_id", this.appid);
            map.put("sign_type", "RSA");
            map.put("charset", this.charset);
            map.put("format", "json");
            map.put("version", "1.0");
            if (this.notifyurl != null && !this.notifyurl.isEmpty()) map.put("notify_url", this.notifyurl);
            map.put("timestamp", String.format(format, System.currentTimeMillis()));
            map.put("method", "alipay.trade.create");

            final TreeMap<String, String> biz_content = new TreeMap<>();
            if (request.getMap() != null) biz_content.putAll(request.getMap());
            biz_content.put("out_trade_no", request.getTradeno());
            biz_content.put("total_amount", "" + (request.getTrademoney() / 100.0));
            biz_content.put("subject", "" + request.getTradetitle());
            biz_content.put("body", request.getTradebody());
            map.put("biz_content", convert.convertTo(biz_content));

            map.put("sign", createSign(map));

            final String responseText = Utility.postHttpContent("https://openapi.alipay.com/gateway.do", Charset.forName(this.charset), joinMap(map));
            //{"alipay_trade_create_response":{"code":"40002","msg":"Invalid Arguments","sub_code":"isv.invalid-signature","sub_msg":"无效签名"},"sign":"xxxxxxxxxxxx"}
            result.setResponseText(responseText);
            final InnerCreateResponse resp = convert.convertFrom(InnerCreateResponse.class, responseText);
            resp.responseText = responseText; //原始的返回内容            
            if (!checkSign(resp)) return result.retcode(RETPAY_FALSIFY_ERROR);
            final Map<String, String> resultmap = resp.alipay_trade_create_response;
            result.setResult(resultmap);
            if (!"SUCCESS".equalsIgnoreCase(resultmap.get("msg"))) {
                return result.retcode(RETPAY_ALIPAY_ERROR).retinfo(resultmap.get("sub_msg"));
            }
        } catch (Exception e) {
            result.setRetcode(RETPAY_ALIPAY_ERROR);
            logger.log(Level.WARNING, "create_pay_error", e);
        }
        return result;
    }

    public static void main(String[] args) throws Throwable {
        AliPayService service = new AliPayService();
        service.convert = JsonConvert.root();
        PayCreatRequest request = new PayCreatRequest();
        request.setPaytype(PAYTYPE_ALIPAY);
        request.setTradetitle("外卖");
        request.setTradebody("白菜");
        request.setTrademoney(1);
        request.setTradeno("200000001");
        request.setClientAddr("192.168.0.1");
        System.out.println(service.create(request));

    }

    @Override
    public PayQueryResponse query(PayRequest request) {
        request.checkVaild();
        final PayQueryResponse result = new PayQueryResponse();
        try {
            final TreeMap<String, String> map = new TreeMap<>();
            map.put("app_id", this.appid);
            map.put("sign_type", "RSA");
            map.put("charset", this.charset);
            map.put("format", "json");
            map.put("version", "1.0");
            //if (this.notifyurl != null && !this.notifyurl.isEmpty()) map.put("notify_url", this.notifyurl); // 查询不需要
            map.put("timestamp", String.format(format, System.currentTimeMillis()));
            map.put("method", "alipay.trade.query");

            final TreeMap<String, String> biz_content = new TreeMap<>();
            biz_content.put("out_trade_no", request.getTradeno());
            map.put("biz_content", convert.convertTo(biz_content));

            map.put("sign", createSign(map));

            final String responseText = Utility.postHttpContent("https://openapi.alipay.com/gateway.do", Charset.forName(this.charset), joinMap(map));

            result.setResponseText(responseText);
            final InnerQueryResponse resp = convert.convertFrom(InnerQueryResponse.class, responseText);
            resp.responseText = responseText; //原始的返回内容            
            if (!checkSign(resp)) return result.retcode(RETPAY_FALSIFY_ERROR);
            final Map<String, String> resultmap = resp.alipay_trade_query_response;
            result.setResult(resultmap);
            if (!"SUCCESS".equalsIgnoreCase(resultmap.get("msg"))) {
                return result.retcode(RETPAY_ALIPAY_ERROR).retinfo(resultmap.get("sub_msg"));
            }
            //trade_status 交易状态：WAIT_BUYER_PAY（交易创建，等待买家付款）、TRADE_CLOSED（未付款交易超时关闭，或支付完成后全额退款）、TRADE_SUCCESS（交易支付成功）、TRADE_FINISHED（交易结束，不可退款）
            short paystatus = PAYSTATUS_PAYNO;
            switch (resultmap.get("trade_status")) {
                case "TRADE_SUCCESS": paystatus = PAYSTATUS_PAYOK;
                    break;
                case "WAIT_BUYER_PAY": paystatus = PAYSTATUS_UNPAY;
                    break;
                case "TRADE_CLOSED": paystatus = PAYSTATUS_CLOSED;
                    break;
                case "TRADE_FINISHED": paystatus = PAYSTATUS_PAYOK;
                    break;
            }
            result.setPaystatus(paystatus);
            result.setPayedmoney((long) (Double.parseDouble(resultmap.get("receipt_amount")) * 100));
        } catch (Exception e) {
            result.setRetcode(RETPAY_ALIPAY_ERROR);
            logger.log(Level.WARNING, "query_pay_error", e);
        }
        return result;
    }

    @Override
    public PayResponse close(PayRequest request) {
        request.checkVaild();
        final PayResponse result = new PayResponse();
        try {
            final TreeMap<String, String> map = new TreeMap<>();
            map.put("app_id", this.appid);
            map.put("sign_type", "RSA");
            map.put("charset", this.charset);
            map.put("format", "json");
            map.put("version", "1.0");
            if (this.notifyurl != null && !this.notifyurl.isEmpty()) map.put("notify_url", this.notifyurl);
            map.put("timestamp", String.format(format, System.currentTimeMillis()));
            map.put("method", "alipay.trade.close");

            final TreeMap<String, String> biz_content = new TreeMap<>();
            biz_content.put("out_trade_no", request.getTradeno());
            map.put("biz_content", convert.convertTo(biz_content));

            map.put("sign", createSign(map));

            final String responseText = Utility.postHttpContent("https://openapi.alipay.com/gateway.do", Charset.forName(this.charset), joinMap(map));

            result.setResponseText(responseText);
            final InnerCloseResponse resp = convert.convertFrom(InnerCloseResponse.class, responseText);
            resp.responseText = responseText; //原始的返回内容            
            if (!checkSign(resp)) return result.retcode(RETPAY_FALSIFY_ERROR);
            final Map<String, String> resultmap = resp.alipay_trade_close_response;
            result.setResult(resultmap);
            if (!"SUCCESS".equalsIgnoreCase(resultmap.get("msg"))) {
                return result.retcode(RETPAY_ALIPAY_ERROR).retinfo(resultmap.get("sub_msg"));
            }
        } catch (Exception e) {
            result.setRetcode(RETPAY_ALIPAY_ERROR);
            logger.log(Level.WARNING, "close_pay_error", e);
        }
        return result;
    }

    @Override
    public PayRefundResponse refund(PayRefundRequest request) {
        request.checkVaild();
        final PayRefundResponse result = new PayRefundResponse();
        try {
            final TreeMap<String, String> map = new TreeMap<>();
            map.put("app_id", this.appid);
            map.put("sign_type", "RSA");
            map.put("charset", this.charset);
            map.put("format", "json");
            map.put("version", "1.0");
            map.put("timestamp", String.format(format, System.currentTimeMillis()));
            map.put("method", "alipay.trade.refund");

            final TreeMap<String, String> biz_content = new TreeMap<>();
            biz_content.put("out_trade_no", request.getTradeno());
            biz_content.put("refund_amount", "" + (request.getRefundmoney() / 100.0));
            map.put("biz_content", convert.convertTo(biz_content));

            map.put("sign", createSign(map));

            final String responseText = Utility.postHttpContent("https://openapi.alipay.com/gateway.do", Charset.forName(this.charset), joinMap(map));

            result.setResponseText(responseText);
            final InnerCloseResponse resp = convert.convertFrom(InnerCloseResponse.class, responseText);
            resp.responseText = responseText; //原始的返回内容            
            if (!checkSign(resp)) return result.retcode(RETPAY_FALSIFY_ERROR);
            final Map<String, String> resultmap = resp.alipay_trade_close_response;
            result.setResult(resultmap);
            if (!"SUCCESS".equalsIgnoreCase(resultmap.get("msg"))) {
                return result.retcode(RETPAY_ALIPAY_ERROR).retinfo(resultmap.get("sub_msg"));
            }
            result.setRefundedmoney((long) (Double.parseDouble(resultmap.get("refund_fee")) * 100));
        } catch (Exception e) {
            result.setRetcode(RETPAY_ALIPAY_ERROR);
            logger.log(Level.WARNING, "close_pay_error", e);
        }
        return result;
    }

    @Override
    public PayRefundResponse queryRefund(PayRequest request) {
        PayQueryResponse queryResponse = query(request);
        final PayRefundResponse response = new PayRefundResponse();
        response.setRetcode(queryResponse.getRetcode());
        response.setRetinfo(queryResponse.getRetinfo());
        response.setResponseText(queryResponse.getResponseText());
        response.setResult(queryResponse.getResult());
        if (queryResponse.isSuccess()) {
            response.setRefundedmoney((long) (Double.parseDouble(response.getResult().get("receipt_amount")) * 100));
        }
        return response;
    }

    protected boolean checkSign(InnerResponse response) throws Exception {
        String text = response.responseText;
        text = text.substring(text.indexOf(':') + 1, text.indexOf(",\"sign\""));

        final KeyFactory factory = KeyFactory.getInstance("RSA");
        PublicKey pubKey = factory.generatePublic(new X509EncodedKeySpec(Base64.getDecoder().decode(this.publickey)));
        Signature signature = Signature.getInstance("SHA1WithRSA");
        signature.initVerify(pubKey);
        signature.update(text.getBytes(this.charset));
        return signature.verify(Base64.getDecoder().decode(response.sign.getBytes()));
    }

    protected String createSign(Map<String, String> map) throws Exception {

        PKCS8EncodedKeySpec priPKCS8 = new PKCS8EncodedKeySpec(Base64.getDecoder().decode(this.signkey));
        final KeyFactory factory = KeyFactory.getInstance("RSA");
        PrivateKey priKey = factory.generatePrivate(priPKCS8);

        Signature signature = Signature.getInstance("SHA1WithRSA");
        signature.initSign(priKey);
        signature.update(joinMap(map).getBytes(this.charset));
        return URLEncoder.encode(Base64.getEncoder().encodeToString(signature.sign()), "UTF-8");
    }

    protected String joinMap(Map<String, String> map) {
        return map.entrySet().stream().map((e -> e.getKey() + "=" + e.getValue())).collect(Collectors.joining("&"));
    }

    public static class InnerCloseResponse extends InnerResponse {

        public Map<String, String> alipay_trade_close_response;

    }

    public static class InnerQueryResponse extends InnerResponse {

        public Map<String, String> alipay_trade_query_response;

    }

    public static class InnerCreateResponse extends InnerResponse {

        public Map<String, String> alipay_trade_create_response;

    }

    public static class InnerResponse {

        public String responseText;

        public String sign;

        @Override
        public String toString() {
            return JsonFactory.root().getConvert().convertTo(this);
        }
    }
}