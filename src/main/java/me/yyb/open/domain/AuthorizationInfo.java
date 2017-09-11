package me.yyb.open.domain;

import org.springframework.data.annotation.Id;

import java.util.List;
import java.util.Map;

/**
 * @author: yyb
 * @date: 17-9-11
 */

public class AuthorizationInfo {

    @Id
    private String appId;

    private String accessToken;

    /**
     * 接口调用凭据刷新令牌（在授权的公众号具备API权限时，才有此返回值） <br/>
     * 刷新令牌主要用于第三方平台获取和刷新已授权用户的access_token <br/>
     * 只会在授权时刻提供，请妥善保存。 <br/>
     * 一旦丢失，只能让用户重新授权，才能再次拿到新的刷新令牌 <br/>
     */
    private String refreshToken;

    private Long expiresIn;

    private List<Map<String, Object>> funcInfo;

    private Long createTime;

    private Long updateTime;

    public String getAppId() {
        return appId;
    }

    public void setAppId(String appId) {
        this.appId = appId;
    }

    public String getAccessToken() {
        return accessToken;
    }

    public void setAccessToken(String accessToken) {
        this.accessToken = accessToken;
    }

    public String getRefreshToken() {
        return refreshToken;
    }

    public void setRefreshToken(String refreshToken) {
        this.refreshToken = refreshToken;
    }

    public Long getExpiresIn() {
        return expiresIn;
    }

    public void setExpiresIn(Long expiresIn) {
        this.expiresIn = expiresIn;
    }

    public List<Map<String, Object>> getFuncInfo() {
        return funcInfo;
    }

    public void setFuncInfo(List<Map<String, Object>> funcInfo) {
        this.funcInfo = funcInfo;
    }

    public Long getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Long createTime) {
        this.createTime = createTime;
    }

    public Long getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(Long updateTime) {
        this.updateTime = updateTime;
    }
}
