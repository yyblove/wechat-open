package me.yyb.open.controller;

import me.yyb.open.aes.AesException;
import me.yyb.open.biz.OpenService;
import me.yyb.open.domain.AuthorizationInfo;
import me.yyb.open.utils.Constants;
import org.dom4j.DocumentException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * @author: yyb
 * @date: 17-9-11
 */
@RestController
public class OpenController {

    private static Logger logger = LoggerFactory.getLogger(OpenController.class);

    @Autowired
    OpenService openService;

    /**
     * 授权页面
     *
     * @return
     */
    @GetMapping("/open")
    public ModelAndView open() {
        ModelAndView view = new ModelAndView("open");
        String location = "https://mp.weixin.qq.com/cgi-bin/componentloginpage?"
                + "component_appid=" + Constants.APP_ID
                + "&pre_auth_code=" + openService.getPreAuthCode()
                + "&redirect_uri=http://" + Constants.HOST + "/open/auth";
        view.addObject("location", location);
        return view;
    }

    /**
     * 授权回调页面
     *
     * @param authCode
     * @param expiresIn
     * @return
     */
    @GetMapping("/open/auth")
    public ModelAndView openAuth(@RequestParam(name = "auth_code") String authCode,
                                 @RequestParam(name = "expires_in") Integer expiresIn) {
        ModelAndView view = new ModelAndView("success");
        AuthorizationInfo authorizationInfo = openService.saveAuth(authCode);
        openService.getAuthorizer(authorizationInfo);
        return view;
    }

    @GetMapping("/open/event/authorize")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void doEventAuthorize(HttpServletRequest request, HttpServletResponse response) throws IOException {
        try {
            openService.doEventAuthorize(request);
        } catch (Exception e) {
            logger.error("-->> doEventAuthorize error", e);
        } finally {
            openService.output(response, "success");
        }
    }

    @GetMapping("/open/{appId}/callback")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void callback(HttpServletRequest request, HttpServletResponse response) throws DocumentException, AesException, IOException {
        openService.processMessageAndEvent(request, response);
    }

}
