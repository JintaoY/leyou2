package com.leyou.gateway.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

@Configuration
public class LeyouCorsConfig {

    @Bean
    public CorsFilter corsFilter(){

        //初始化cors配置对象
        CorsConfiguration config = new CorsConfiguration();
        //允许跨域的域名，如果要携带cookie，不能为*  *代表所有的域名都可与跨域
        config.addAllowedOrigin("http://manage.leyou.com");
        //加上www.leyou.com
        config.addAllowedOrigin("http://www.leyou.com");


        config.setAllowCredentials(true); //允许携带从cookie

        config.addAllowedMethod("*"); //代表了所有的请求方法

        config.addAllowedHeader("*"); //允许携带任何头信息


        //CorsFilter 的参数的实现类
        //添加映射地址，进行拦截
        UrlBasedCorsConfigurationSource configSource = new UrlBasedCorsConfigurationSource();
        configSource.registerCorsConfiguration("/**",config);

        //返回新的CorsFilter  参数为接口，找他的实现类
        return new CorsFilter(configSource);
    }
}
