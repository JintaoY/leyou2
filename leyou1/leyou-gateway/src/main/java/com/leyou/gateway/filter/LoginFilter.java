package com.leyou.gateway.filter;

import com.leyou.common.utils.CookieUtils;
import com.leyou.common.utils.JwtUtils;
import com.leyou.gateway.config.FilterProperties;
import com.leyou.gateway.config.JwtProperties;
import com.netflix.zuul.ZuulFilter;
import com.netflix.zuul.context.RequestContext;
import com.netflix.zuul.exception.ZuulException;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

@Component
@EnableConfigurationProperties({JwtProperties.class, FilterProperties.class})
public class LoginFilter extends ZuulFilter {

    @Autowired
    private JwtProperties jwtProperties;

    @Autowired
    private FilterProperties filterProperties;

    @Override
    public String filterType() {
        return "pre";
    }

    @Override
    public int filterOrder() {
        return 10;
    }

    @Override
    public boolean shouldFilter() {

        //获得放行的的路径
        List<String> paths = filterProperties.getAllowPaths();
        //获得上下文
        RequestContext context = RequestContext.getCurrentContext();

        //获得request
        HttpServletRequest request = context.getRequest();

        //获得请求路径  uri  的前缀
        String requestURI = request.getRequestURI();

        //循环判断白名单  uri前缀的   放行 false
        for (String path : paths) {
            //放行 false
            if(requestURI.startsWith(path)){
                return false;
            }
        }

        return true;
    }

    @Override
    public Object run() throws ZuulException {

        //因为需要request   所以从Zuul的应用上下文中获得
        RequestContext context = RequestContext.getCurrentContext();
        //获得请求
        HttpServletRequest request = context.getRequest();

        //获得token
        String token = CookieUtils.getCookieValue(request, jwtProperties.getCookieName());

        if(StringUtils.isBlank(token)){
            //设置Zuul响应
            context.setSendZuulResponse(false);
            //响应状态码   或者401  103
            context.setResponseStatusCode(HttpStatus.UNAUTHORIZED.value());
        }

        //解析失败 则响应失败
        try {
            JwtUtils.getInfoFromToken(token, this.jwtProperties.getPublicKey());

            return null;

        } catch (Exception e) {
            e.printStackTrace();

            context.setSendZuulResponse(false);
            context.setResponseStatusCode(HttpStatus.FORBIDDEN.value());
        }


        return null;
    }
}
