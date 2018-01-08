package me.yyb.open.vo;

/**
 * @author yyb
 * @since 2018-01-08
 */
public abstract class AbstractMessage {

    private String fromUserName;

    private String toUserName;

    public String getFromUserName() {
        return fromUserName;
    }

    public void setFromUserName(String fromUserName) {
        this.fromUserName = fromUserName;
    }

    public String getToUserName() {
        return toUserName;
    }

    public void setToUserName(String toUserName) {
        this.toUserName = toUserName;
    }

    // 回复的消息类型
    abstract String getMsgType();

    // 生成中间的信息
    abstract String generateCenterXML();

    public String parseToXML() {
        StringBuilder xmlMessage = new StringBuilder();
        xmlMessage.append("<xml>");
        xmlMessage.append("<ToUserName><![CDATA[").append(getToUserName()).append("]]></ToUserName>");
        xmlMessage.append("<FromUserName><![CDATA[").append(getFromUserName()).append("]]></FromUserName>");
        xmlMessage.append("<CreateTime>").append(System.currentTimeMillis()).append("</CreateTime>");
        xmlMessage.append("<MsgType><![CDATA[").append(getMsgType()).append("]]></MsgType>");
        xmlMessage.append(generateCenterXML());
        xmlMessage.append("</xml>");
        return xmlMessage.toString();
    }


}
