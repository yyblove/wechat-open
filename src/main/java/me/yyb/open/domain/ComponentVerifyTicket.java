package me.yyb.open.domain;

import org.springframework.data.annotation.Id;

/**
 * @author: yyb
 * @date: 17-9-6
 */
public class ComponentVerifyTicket {

    @Id
    private String id;

    private String name;

    private String verifyTicket;

    private Long updateTime;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getVerifyTicket() {
        return verifyTicket;
    }

    public void setVerifyTicket(String verifyTicket) {
        this.verifyTicket = verifyTicket;
    }

    public Long getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(Long updateTime) {
        this.updateTime = updateTime;
    }
}
