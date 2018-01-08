package me.yyb.open.vo;

/**
 * @author yyb
 * @since  2017-8-30
 * <p>
 * 微信自动回复， 类型文本
 */
public class TextMessage extends AbstractMessage {

    private String content;

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    @Override
    String getMsgType() {
        return "text";
    }

    @Override
    String generateCenterXML() {
        return String.format("<Content><![CDATA[%s]]></Content>", content);
    }
}