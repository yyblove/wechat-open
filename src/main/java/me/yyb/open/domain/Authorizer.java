package me.yyb.open.domain;

import org.springframework.data.annotation.Id;

import java.util.Map;

/**
 * @author: yyb
 * @date: 17-9-6
 * 获取授权方公众号帐号基本信息
 */
public class Authorizer {

    /**
     * 授权方appid
     */
    @Id
    private String appId;

    /**
     * 授权方昵称
     */
    private String nickname;

    /**
     * 授权方头像
     */
    private String headImg;

    /**
     * 授权方公众号类型，0代表订阅号，1代表由历史老帐号升级后的订阅号，2代表服务号
     */
    private Integer serviceTypeInfo;

    /**
     * 授权方认证类型，-1代表未认证，0代表微信认证，1代表新浪微博认证，2代表腾讯微博认证，3代表已资质认证通过但还未通过名称认证，4代表已资质认证通过、还未通过名称认证，但通过了新浪微博认证，5代表已资质认证通过、还未通过名称认证，但通过了腾讯微博认证
     */
    private Integer verifyTypeInfo;

    /**
     * 授权方公众号的原始ID
     */
    private String username;

    /**
     * 公众号的主体名称
     */
    private String principalName;

    /**
     * 用以了解以下功能的开通状况（0代表未开通，1代表已开通）：
     * open_store:是否开通微信门店功能
     * open_scan:是否开通微信扫商品功能
     * open_pay:是否开通微信支付功能
     * open_card:是否开通微信卡券功能
     * open_shake:是否开通微信摇一摇功能
     */
    private Map<String, Integer> businessInfo;

    /**
     * 授权方公众号所设置的微信号，可能为空
     */
    private String alias;

    /**
     * 二维码图片的URL，开发者最好自行也进行保存
     */
    private String qrcodeUrl;

    public String getAppId() {
        return appId;
    }

    public void setAppId(String appId) {
        this.appId = appId;
    }

    public String getNickname() {
        return nickname;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

    public String getHeadImg() {
        return headImg;
    }

    public void setHeadImg(String headImg) {
        this.headImg = headImg;
    }

    public Integer getServiceTypeInfo() {
        return serviceTypeInfo;
    }

    public void setServiceTypeInfo(Integer serviceTypeInfo) {
        this.serviceTypeInfo = serviceTypeInfo;
    }

    public Integer getVerifyTypeInfo() {
        return verifyTypeInfo;
    }

    public void setVerifyTypeInfo(Integer verifyTypeInfo) {
        this.verifyTypeInfo = verifyTypeInfo;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPrincipalName() {
        return principalName;
    }

    public void setPrincipalName(String principalName) {
        this.principalName = principalName;
    }

    public Map<String, Integer> getBusinessInfo() {
        return businessInfo;
    }

    public void setBusinessInfo(Map<String, Integer> businessInfo) {
        this.businessInfo = businessInfo;
    }

    public String getAlias() {
        return alias;
    }

    public void setAlias(String alias) {
        this.alias = alias;
    }

    public String getQrcodeUrl() {
        return qrcodeUrl;
    }

    public void setQrcodeUrl(String qrcodeUrl) {
        this.qrcodeUrl = qrcodeUrl;
    }
}
