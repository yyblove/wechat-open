package me.yyb.open.biz;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import me.yyb.open.aes.AesException;
import me.yyb.open.domain.AuthorizationInfo;
import me.yyb.open.domain.Authorizer;
import me.yyb.open.domain.ComponentVerifyTicket;
import me.yyb.open.pojo.ComponentAccessToken;
import me.yyb.open.utils.Constants;
import me.yyb.open.utils.OpenUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.dom4j.DocumentException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.Charset;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author: yyb
 * @date: 17-9-11
 */
@Service
public class OpenService {

    private static Logger logger = LoggerFactory.getLogger(OpenService.class);

    private volatile ComponentAccessToken componentAccessToken;

    private volatile ComponentVerifyTicket componentVerifyTicket;

    private RestTemplate restTemplate = new RestTemplate();

    @Autowired
    MongoTemplate mongoTemplate;

    /**
     * 获取预授权码： pre_auth_code
     * 请求地址：https://api.weixin.qq.com/cgi-bin/component/api_create_preauthcode?component_access_token=xxx
     * 请求参数：{"component_appid": "xxx"}
     * 请求结果：{"pre_auth_code:"xxx", expires_in: 600}
     *
     * @return
     */
    public String getPreAuthCode() {
        String url = "https://api.weixin.qq.com/cgi-bin/component/api_create_preauthcode?component_access_token=" + getComponentAccessToken();
        Map<String, String> postData = Collections.singletonMap("component_appid", Constants.APP_ID);
        String result = restTemplate.postForObject(url, postData, String.class);
        JSONObject object = JSON.parseObject(result);
        return object.getString("pre_auth_code");
    }


    /**
     * 获取第三方平台的令牌
     *
     * @return
     */
    public String getComponentAccessToken() {
        if (componentVerifyTicket == null || StringUtils.isEmpty(componentVerifyTicket.getVerifyTicket())) return null;

        if (componentAccessToken == null
                || System.currentTimeMillis() - componentAccessToken.getCreateTime() >= componentAccessToken.getExpiresIn() - 20 * 60 * 1000) {

            String url = "https://api.weixin.qq.com/cgi-bin/component/api_component_token";

            Map<String, String> postData = new HashMap<>();
            postData.put("component_appid", Constants.APP_ID);
            postData.put("component_appsecret", Constants.APP_SECRET);
            postData.put("component_verify_ticket", this.componentVerifyTicket.getVerifyTicket());

            String result = restTemplate.postForObject(url, postData, String.class);

            JSONObject resObj = JSONObject.parseObject(result);
            String accessToken = resObj.getString("component_access_token");
            if (StringUtils.isNotEmpty(accessToken)) {
                if (componentAccessToken == null)
                    componentAccessToken = new ComponentAccessToken();

                componentAccessToken.setCreateTime(System.currentTimeMillis());
                componentAccessToken.setExpiresIn(resObj.getLongValue("expires_in") * 1000);
                componentAccessToken.setComponentAccessToken(accessToken);
            } else {
                return null;
            }
        }
        return componentAccessToken.getComponentAccessToken();
    }

    /**
     * 保存公众号授权令牌
     *
     * @param authCode
     * @return
     */
    public AuthorizationInfo saveAuth(String authCode) {
        String url = "https://api.weixin.qq.com/cgi-bin/component/api_query_auth?component_access_token=" + getComponentAccessToken();
        Map<String, String> params = new HashMap<>();
        params.put("component_appid", Constants.APP_ID);
        params.put("authorization_code", authCode);

        // 返回公众号的调用凭据
        String result = restTemplate.postForObject(url, params, String.class);
        JSONObject resObj = JSON.parseObject(result);
        if (!resObj.containsKey("errcode")) {
            JSONObject infoObj = resObj.getJSONObject("authorization_info");
            String appId = infoObj.getString("authorizer_appid");

            AuthorizationInfo authorizationInfo = mongoTemplate.findOne(Query.query(Criteria.where("_id").is(appId)), AuthorizationInfo.class);
            authorizationInfo.setAppId(appId);
            authorizationInfo.setAccessToken(infoObj.getString("authorizer_access_token"));
            authorizationInfo.setRefreshToken(infoObj.getString("authorizer_refresh_token"));
            authorizationInfo.setExpiresIn(infoObj.getLongValue("expires_in") * 1000);
            authorizationInfo.setFuncInfo(infoObj.getJSONArray("func_info").toJavaObject(List.class));
            authorizationInfo.setCreateTime(System.currentTimeMillis());
            authorizationInfo.setUpdateTime(System.currentTimeMillis());

            mongoTemplate.save(authorizationInfo);

            return authorizationInfo;
        }
        return null;
    }

    /**
     * 保存公众号基本信息
     *
     * @param authorizationInfo
     * @return
     */
    public Authorizer getAuthorizer(AuthorizationInfo authorizationInfo) {
        String url = "https://api.weixin.qq.com/cgi-bin/component/api_get_authorizer_info" +
                "?component_access_token=" + getComponentAccessToken();

        Map<String, String> params = new HashMap<>();
        params.put("component_appid", Constants.APP_ID);
        params.put("authorizer_appid", authorizationInfo.getAppId());

        String result = restTemplate.postForObject(url, params, String.class);
        result = new String(result.getBytes(Charset.forName("ISO-8859-1")), Charset.forName("UTF-8"));
        if (result.contains("authorizer_info")) {

            Authorizer authorizer = mongoTemplate.findOne(Query.query(Criteria.where("appId").is(authorizationInfo.getAppId())), Authorizer.class);
            if (authorizer == null) {
                authorizer = new Authorizer();
            }
            authorizer.setAppId(authorizationInfo.getAppId());

            JSONObject object = JSONObject.parseObject(result);

            JSONObject info = object.getJSONObject("authorizer_info");

            authorizer.setNickname(info.getString("nick_name"));
            authorizer.setHeadImg(info.getString("head_img"));

            JSONObject service = info.getJSONObject("service_type_info");
            authorizer.setServiceTypeInfo(service.getInteger("id"));

            JSONObject verify = info.getJSONObject("verify_type_info");
            authorizer.setVerifyTypeInfo(verify.getInteger("id"));

            authorizer.setUsername(info.getString("user_name"));
            authorizer.setPrincipalName(info.getString("principal_name"));
            authorizer.setBusinessInfo(info.getJSONObject("business_info").toJavaObject(Map.class));

            authorizer.setAlias(info.getString(info.getString("alias")));
            authorizer.setQrcodeUrl(info.getString("qrcode_url"));

            mongoTemplate.save(authorizer);
            return authorizer;
        }
        return null;
    }

    /**
     * 发送消息
     *
     * @param response
     * @param content  要发送的消息
     * @throws IOException
     */
    public void output(HttpServletResponse response, String content) throws IOException {
        PrintWriter printWriter = response.getWriter();
        printWriter.print(content);
        printWriter.flush();
        printWriter.close();
    }


    /**
     * 处理授权事件URL
     *
     * @param request
     * @throws IOException
     * @throws AesException
     * @throws DocumentException
     */
    public void doEventAuthorize(HttpServletRequest request) throws IOException, AesException, DocumentException {
        String token = Constants.TOKEN;
        String nonce = request.getParameter("nonce");
        String timestamp = request.getParameter("timestamp");
        String signature = request.getParameter("signature");
        String msgSignature = request.getParameter("msg_signature");

        if (StringUtils.isEmpty(msgSignature)) return;
        // 微信消息签名验证
        boolean isValid = OpenUtils.checkSignature(token, signature, timestamp, nonce);
        if (!isValid) return;
        String xml = IOUtils.toString(request.getReader());
        Map<String, String> map = OpenUtils.decryptMsgToMap(msgSignature, timestamp, nonce, xml);
        logger.info("--->>> doEventAuthorize: {}", map.toString());
        String infoType = map.get("InfoType");
        switch (infoType) {
            case "component_verify_ticket":
                // TODO 推送ticket,这ticket也要保存起来，并且随着微信的推送刷新
                String componentVerifyTicket = map.get("ComponentVerifyTicket");
                this.updateComponentVerifyTicket(componentVerifyTicket);
                break;
            case "unauthorized":
                this.unauthorized(map);
                break;
            case "authorized":
            case "updateauthorized":
                this.createOrUpdateAuthorized(map);
                break;
        }
    }

    /**
     * 更新ticket
     *
     * @param componentVerifyTicket
     */
    private void updateComponentVerifyTicket(String componentVerifyTicket) {
        if (StringUtils.isEmpty(componentVerifyTicket)) return;

        ComponentVerifyTicket ticket = mongoTemplate.findOne(Query.query(Criteria.where("name").is("COMPONENT_VERIFY_TICKET")), ComponentVerifyTicket.class);
        if (ticket == null) {
            ticket = new ComponentVerifyTicket();
            ticket.setName("COMPONENT_VERIFY_TICKET");
        }
        ticket.setUpdateTime(System.currentTimeMillis());
        ticket.setVerifyTicket(componentVerifyTicket);
        mongoTemplate.save(ticket);
        this.componentVerifyTicket = ticket;
    }

    /**
     * 处理取消授权通知
     *
     * @param map：包含 AppId，CreateTime，AuthorizerAppid
     */
    private void unauthorized(Map<String, String> map) {
        String authoirzerAppId = map.get("AuthorizerAppid");
        mongoTemplate.remove(Query.query(Criteria.where("id").is(authoirzerAppId)), AuthorizationInfo.class);
        mongoTemplate.remove(Query.query(Criteria.where("id").is(authoirzerAppId)), Authorizer.class);
    }

    /**
     * 处理授权成功或授权更新通知
     *
     * @param map 包含：AppId，CreateTime，AuthorizerAppid，AuthorizationCode，AuthorizationCodeExpiredTime
     */
    private void createOrUpdateAuthorized(Map<String, String> map) {
        String authorizationCode = map.get("AuthorizationCode");
        AuthorizationInfo authorizationInfo = saveAuth(authorizationCode);
        getAuthorizer(authorizationInfo);
    }

    /**
     * 处理公众号的消息和事件
     *
     * @param request
     * @param response
     * @throws IOException
     * @throws AesException
     * @throws DocumentException
     */
    public void processMessageAndEvent(HttpServletRequest request, HttpServletResponse response) throws IOException, AesException, DocumentException {
        String token = Constants.TOKEN;
        String nonce = request.getParameter("nonce");
        String timestamp = request.getParameter("timestamp");
        String signature = request.getParameter("signature");
        String msgSignature = request.getParameter("msg_signature");
        if (StringUtils.isBlank(msgSignature)) {
            // 微信推送给第三方开放平台的消息一定是加过密的，无消息加密无法解密消息
            return;
        }
        String xml = IOUtils.toString(request.getReader());
        boolean isValid = OpenUtils.checkSignature(token, signature, timestamp, nonce);
        if (isValid) {
            Map<String, String> map = OpenUtils.decryptMsgToMap(msgSignature, timestamp, nonce, xml);
            logger.info("--->>> processMessageAndEvent: {}", map.toString());

            String msgType = map.get("MsgType");
            String toUserName = map.get("ToUserName");
            String fromUserName = map.get("FromUserName");

            // 微信全网测试: gh_3c884a361561 微信专用测试公众号；  gh_8dad206e9538 微信专用测试小程序
            if (StringUtils.equals(toUserName, Constants.MP_USERNAME) || StringUtils.equals(toUserName, Constants.MINI_USERNAME)) {

                if ("event".equalsIgnoreCase(msgType)) {

                    // 全网检测的模拟粉丝点击事件，只要返回文本消息：事件名称+"from_callback"
                    String event = map.get("Event");
                    replyTextMessage(response, OpenUtils.generateTextXML(fromUserName, toUserName, event + "from_callback"));

                } else if ("text".equalsIgnoreCase(msgType)) {
                    String content = map.get("Content");

                    if ("TESTCOMPONENT_MSG_TYPE_TEXT".equalsIgnoreCase(content)) {

                        //全网检测的模拟模板消息，只要返回文本消息： 内容 + "_callback"
                        replyTextMessage(response, OpenUtils.generateTextXML(fromUserName, toUserName, content + "_callback"));
                    } else if (StringUtils.startsWithIgnoreCase(content, "QUERY_AUTH_CODE")) {

                        // 模拟粉丝发送文本消息给专用测试公众号，第三方平台方需在5秒内返回空串表明暂时不回复，然后再立即使用客服消息接口发送消息回复粉丝
                        this.output(response, "");
                        String msg = content.split(":")[1];
                        // 调用客服接口回复消息
                        this.replyApiTextMessage(msg, fromUserName);
                    }
                }
            } else {
                // TODO 其他绑定的公众号处理
                this.output(response, "");
            }
        }
    }

    /**
     * 发挥消息文本
     *
     * @param response
     * @param message
     * @throws IOException
     */
    private void replyTextMessage(HttpServletResponse response, String message) throws IOException, AesException {
        // 加密要发送的消息
        String encryptXml = OpenUtils.encryptMsg(message);
        this.output(response, encryptXml);
    }

    /**
     * 第三方平台方拿到$query_auth_code$的值后，
     * 通过接口文档页中的“使用授权码换取公众号的授权信息”API，
     * 将$query_auth_code$的值赋值给API所需的参数authorization_code。
     * 然后，调用发送客服消息api回复文本消息给粉丝，
     * 其中文本消息的content字段设为：$query_auth_code$_from_api（其中$query_auth_code$需要替换成推送过来的query_auth_code）
     *
     * @param authCode
     * @param toUserName
     */
    private void replyApiTextMessage(String authCode, String toUserName) {
        // 先通过 authCode 获取到公众号的 accessToken
        String accessTokenUrl = "https://api.weixin.qq.com/cgi-bin/component/api_query_auth?component_access_token=" + this.getComponentAccessToken();
        Map<String, String> params = new HashMap<>();
        params.put("component_appid", Constants.APP_ID);
        params.put("authorization_code", authCode);
        String accessTokenResult = restTemplate.postForObject(accessTokenUrl, params, String.class);
        JSONObject object = JSONObject.parseObject(accessTokenResult);

        // 通过公众号的accessToken调用客服接口发送消息
        Map<String, Object> obj = new HashMap<String, Object>();
        Map<String, Object> msgMap = new HashMap<String, Object>();
        String msg = authCode + "_from_api";
        msgMap.put("content", msg);
        obj.put("touser", toUserName);
        obj.put("msgtype", "text");
        obj.put("text", msgMap);
        String sendUrl = "https://api.weixin.qq.com/cgi-bin/message/custom/send?access_token=" + object.getString("authorizer_access_token");
        String sendResult = restTemplate.postForObject(sendUrl, obj, String.class);
    }
}
