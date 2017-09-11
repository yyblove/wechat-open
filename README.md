# wechat-open 微信第三方平台开发

----------
> - [微信第三方平台 - 官网](https://open.weixin.qq.com/cgi-bin/showdocument?action=dir_list&t=resource/res_list&verify=1&lang=zh_CN)
> - [参考文章](http://blog.csdn.net/zhangdaiscott/article/details/48269837)

----------

## 填写开发资料
> [微信第三方平台 - 申请资料说明](https://open.weixin.qq.com/cgi-bin/showdocument?action=dir_list&t=resource/res_list&verify=1&id=open1419318462&token=&lang=zh_CN)

![授权流程相关](./image/01.png)

![授权后代替公众号实现业务](./image/02.png)

![白名单IP地址](./image/03.png)

----------

## 授权流程介绍

 1. 第三方平台先提供一个`HMTL`页面，介绍该平台的功能，并提供授权页面
 ![第三方平台页面](./image/04.png)
 2. 用户点击授权地址，获得一个`二维码`页面
 ![公众平台授权页面](./image/05.png)
 3. 公众号`管理者`或`运营者`，扫描这个`二维码`，同意授权
 4. 第三方平台服务获取相关信息，并进行处理
 5. 至此公众号授权给第三方平台完成

----------
## 接口说明
----------
> [微信 - 授权流程技术说明](https://open.weixin.qq.com/cgi-bin/showdocument?action=dir_list&t=resource/res_list&verify=1&id=open1453779503&token=&lang=zh_CN)

----------
### 授权事件接收URL介绍
 - URL：`http://open.baidu.com/open/event/authorize` 
 - 该URL处理4件事情，分别是
    - 处理微信每10分钟像这个接口推送的`component_verify_ticket` 
    - 处理取消授权通知
    - 处理授权成功通知
    - 处理授权更新通知
 - 接收到消息后接收到后必须直接返回字符串`success`
 - `component_verify_ticket`： 验证平台方的重要凭据，服务方在获取component_access_token时需要提供最新推送的ticket以供验证身份合法性。此ticket作为验证服务方的重要凭据，请妥善保存。

----------
### 公众号消息与事件接收URL
- URL： `http://open.baidu.com/open/$APPID$/callback`
- 该URL处理，粉丝对公众号的发送消息、菜单、扫码等事件。
- 并且要处理微信全网发布检测， [微信 - 全网发布接入检测说明](https://open.weixin.qq.com/cgi-bin/showdocument?action=dir_list&t=resource/res_list&verify=1&id=open1419318611&token=&lang=zh_CN)
 

----------
### 授权页面
- URL：`http://open.baidu.com/open`
- 该页面需要提供一个授权链接：
`https://mp.weixin.qq.com/cgi-bin/componentloginpage?component_appid=xxxx&pre_auth_code=xxxxx&redirect_uri=xxxx`
 - `component_appid` : 第三方平台的APPID
 - `pre_auth_code` : 预授权码
 - `redirect_uri` : 回调URI，不需要URLEncode
 - 提供授权页面

----------
### 授权成功回调页面
- URL: `http://open.baidu.com/open/auth`
- 返回授权成功页面