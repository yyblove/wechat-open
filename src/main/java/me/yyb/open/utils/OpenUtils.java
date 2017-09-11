package me.yyb.open.utils;

import me.yyb.open.aes.AesException;
import me.yyb.open.aes.WXBizMsgCrypt;
import org.apache.commons.lang.RandomStringUtils;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;

import java.io.ByteArrayInputStream;
import java.nio.charset.Charset;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.*;

/**
 * @author: yyb
 * @date: 17-9-11
 */
public class OpenUtils {

    /**
     * 校验微信消息签名是否有效
     *
     * @param token
     * @param signature
     * @param timestamp
     * @param nonce
     * @return
     */
    public static boolean checkSignature(String token, String signature, String timestamp, String nonce) {
        String[] arr = new String[]{token, timestamp, nonce};
        Arrays.sort(arr);

        StringBuilder content = new StringBuilder();
        for (String s : arr) {
            content.append(s);
        }

        MessageDigest md = null;
        String tmpStr = null;

        try {
            md = MessageDigest.getInstance("SHA-1");
            byte[] digest = md.digest(content.toString().getBytes());
            tmpStr = byteToStr(digest);
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }

        return tmpStr != null && tmpStr.equalsIgnoreCase(signature);
    }

    private static String byteToStr(byte[] byteArray) {
        StringBuilder strDigest = new StringBuilder();
        for (byte item : byteArray) {
            strDigest.append(byteToHexStr(item));
        }
        return strDigest.toString();
    }

    private static String byteToHexStr(byte mByte) {
        char[] Digit = {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F'};
        char[] tempArr = new char[2];
        tempArr[0] = Digit[(mByte >>> 4) & 0X0F];
        tempArr[1] = Digit[mByte & 0X0F];
        return new String(tempArr);
    }

    /**
     * 解密微信消息，返回map
     *
     * @param msgSignature
     * @param timestamp
     * @param nonce
     * @param encryptData
     * @return
     * @throws AesException
     * @throws DocumentException
     */
    public static Map<String, String> decryptMsgToMap(String msgSignature, String timestamp, String nonce, String encryptData) throws AesException, DocumentException {
        WXBizMsgCrypt wxBizMsgCrypt = new WXBizMsgCrypt(Constants.TOKEN, Constants.ENCODING_AES_KEY, Constants.APP_ID);
        String xml = wxBizMsgCrypt.decryptMsg(msgSignature, timestamp, nonce, encryptData);
        return xmlToMap(xml);
    }

    /**
     * 将xml转换成map
     *
     * @param xml
     * @return
     * @throws DocumentException
     */
    public static Map<String, String> xmlToMap(String xml) throws DocumentException {
        Map<String, String> map = new HashMap<>();
        SAXReader reader = new SAXReader();
        Document document = reader.read(new ByteArrayInputStream(xml.getBytes(Charset.forName("utf-8"))));
        Element root = document.getRootElement();
        List<Element> list = root.elements();
        for (Element element : list) {
            map.put(element.getName(), element.getText());
        }
        return map;
    }

    /**
     * 加密消息
     *
     * @param xml
     * @return
     * @throws AesException
     */
    public static String encryptMsg(String xml) throws AesException {
        WXBizMsgCrypt wxBizMsgCrypt = new WXBizMsgCrypt(Constants.TOKEN, Constants.ENCODING_AES_KEY, Constants.APP_ID);
        String timestamp = System.currentTimeMillis() + "";
        String nonceStr = RandomStringUtils.randomAlphanumeric(32);
        return wxBizMsgCrypt.encryptMsg(xml, timestamp, nonceStr);
    }

    /**
     * 生成 文本消息的xml格式
     *
     * @param toUserName
     * @param fromUserName
     * @param content
     * @return
     */
    public static String generateTextXML(String toUserName, String fromUserName, String content) {
        StringBuilder sb = new StringBuilder();
        sb.append("<xml>");
        sb.append("<ToUserName><![CDATA[").append(toUserName).append("]]></ToUserName>");
        sb.append("<FromUserName><![CDATA[").append(fromUserName).append("]]></FromUserName>");
        sb.append("<CreateTime>").append(System.currentTimeMillis()).append("</CreateTime>");
        sb.append("<MsgType><![CDATA[text]]></MsgType>");
        sb.append("<Content><![CDATA[").append(content).append("]]></Content>)");
        sb.append("</xml>");
        return sb.toString();
    }
}
