package me.yyb.open.pojo;

/**
 * @author: yyb
 * @date: 17-9-11
 * 第三方平台的调用凭据
 */
public class ComponentAccessToken {

    /**
     * 调用凭据， 也称为令牌
     */
    private String componentAccessToken;

    /**
     * 有效时间
     */
    private Long expiresIn;

    /**
     * 创建时间
     */
    private Long createTime;


    public String getComponentAccessToken() {
        return componentAccessToken;
    }

    public void setComponentAccessToken(String componentAccessToken) {
        this.componentAccessToken = componentAccessToken;
    }

    public Long getExpiresIn() {
        return expiresIn;
    }

    public void setExpiresIn(Long expiresIn) {
        this.expiresIn = expiresIn;
    }

    public Long getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Long createTime) {
        this.createTime = createTime;
    }
}
