package com.leyou.auth.controller;

import com.leyou.auth.config.JwtProperties;
import com.leyou.auth.service.AuthService;
import com.leyou.common.pojo.UserInfo;
import com.leyou.common.utils.CookieUtils;
import com.leyou.common.utils.JwtUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@Controller
@EnableConfigurationProperties(JwtProperties.class)
public class AuthController {

    @Autowired
    private AuthService authService;

    @Autowired
    private JwtProperties jwtProperties;

    /**
     * 登录
     * @param username
     * @param password
     * @param request
     * @param response
     * @return
     */
    @PostMapping("accredit")
    private ResponseEntity<Void> accredit(
            @RequestParam("username") String username,
            @RequestParam("password") String password,
            HttpServletRequest request,HttpServletResponse response
    ){

        //登录验证
        String token = this.authService.accredit(username,password);

        if(StringUtils.isBlank(token)){
            //401 权限不足
            //return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        //将token写入cookie中
        CookieUtils.setCookie(request, response, this.jwtProperties.getCookieName(),
                token, this.jwtProperties.getCookieMaxAge()*60);


        return ResponseEntity.ok().build();
    }

    /**
     * 用户的显示
     * 验证用户信息
     * 另外一种方式获得cookie
     * 并且每次都进行刷新 token 和 cookie
     * @return
     */
    @GetMapping("verify")
    public ResponseEntity<UserInfo> verify(@CookieValue("LY_TOKEN")String token,
                                           HttpServletRequest request,
                                           HttpServletResponse response){

        UserInfo userInfo = null;
        try {
            //从token中获得载荷信息
            userInfo = JwtUtils.getInfoFromToken(token, this.jwtProperties.getPublicKey());

            if(userInfo == null){
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
            }

            //刷新jwt的认证时间
            token = JwtUtils.generateToken(userInfo, this.jwtProperties.getPrivateKey(), this.jwtProperties.getExpire()*60);

            //刷新cookie
            CookieUtils.setCookie(request, response, this.jwtProperties.getCookieName(),
                    token, this.jwtProperties.getCookieMaxAge()*60);


            return ResponseEntity.ok(userInfo);


        } catch (Exception e) {
            e.printStackTrace();

        }
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
    }


}
