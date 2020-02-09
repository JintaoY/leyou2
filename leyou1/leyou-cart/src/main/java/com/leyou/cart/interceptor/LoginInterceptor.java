package com.leyou.cart.interceptor;

import com.leyou.cart.config.JwtProperties;
import com.leyou.common.pojo.UserInfo;
import com.leyou.common.utils.CookieUtils;
import com.leyou.common.utils.JwtUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * 拦截器   通过cookie  获得token  转为为载荷信息 userInfo
 */
@EnableConfigurationProperties(JwtProperties.class)
@Component
    public class LoginInterceptor extends HandlerInterceptorAdapter {


    @Autowired
    private JwtProperties jwtProperties;

    //线程局部变量，存放userInfo
    private static final ThreadLocal<UserInfo> THREAD_LOCAL = new ThreadLocal<>();

    /**
     * 前置处理
     * @param request
     * @param response
     * @param handler
     * @return
     * @throws Exception
     */
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {

        //从cookie中获得token
        String token = CookieUtils.getCookieValue(request, this.jwtProperties.getCookieName());

        //验证不通过
        if(StringUtils.isBlank(token)){
            //未登录 401
            response.setStatus(HttpStatus.UNAUTHORIZED.value());
            return false;
        }

        //解析token
        UserInfo userInfo = JwtUtils.getInfoFromToken(token, this.jwtProperties.getPublicKey());

        //由于使用局部变量会有线程问题，request可以解决
        //但是有个更好的 ThreadLocal
        //放进线程池中
        THREAD_LOCAL.set(userInfo);
        return true;

    }

    /**
     * 获得用户信息
     * @return
     */
    public static UserInfo getLoginUser(){
        return THREAD_LOCAL.get();
    }

    /**
     * 完成后  移除 
     * @param request
     * @param response
     * @param handler
     * @param ex
     * @throws Exception
     */
    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
        THREAD_LOCAL.remove();
    }
}
