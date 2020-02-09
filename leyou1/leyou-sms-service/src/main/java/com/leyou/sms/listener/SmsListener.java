package com.leyou.sms.listener;

import com.aliyuncs.exceptions.ClientException;
import com.leyou.sms.config.SmsProperties;
import com.leyou.sms.utils.SmsUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.amqp.core.ExchangeTypes;
import org.springframework.amqp.rabbit.annotation.Exchange;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.QueueBinding;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
@EnableConfigurationProperties(SmsProperties.class)
public class SmsListener {

    @Autowired
    private SmsProperties smsProperties;

    @Autowired
    private SmsUtils smsUtils;



    public void listenSmsALI(Map<String,String> msg) throws ClientException {

        if(msg == null || msg.size() < 0){
            //放弃处理
            return;
        }
        //手机号码
        String phone = msg.get("phone");
        //验证码
        String code = msg.get("code");

        if(StringUtils.isNoneBlank(phone) && StringUtils.isNoneBlank(code)){
            //因为可能是不同的短信；比如购物
            this.smsUtils.sendSms(phone, code, smsProperties.getSignName(), smsProperties.getVerifyCodeTemplate());
        }

        return;
    }


    @RabbitListener(bindings = @QueueBinding(
            value = @Queue(value = "LEYOU.SMS.QUEUE",durable = "true"),
            exchange = @Exchange(value = "LEYOU.SMS.EXCHANGE",ignoreDeclarationExceptions = "true",type = ExchangeTypes.TOPIC),
            key = {"sms.verify.code"}
    ))
    public void listenSms(Map<String,String> msg){
        if(msg == null || msg.size() < 0){
            //放弃处理
            return;
        }
        //手机号码
        String phone = msg.get("phone");
        //验证码
        String code = msg.get("code");

        if(StringUtils.isNoneBlank(phone) && StringUtils.isNoneBlank(code)) {
            //因为可能是不同的短信；比如购物
            //this.smsUtils.sendSms(phone, code, smsProperties.getSignName(), smsProperties.getVerifyCodeTemplate());
            System.out.println("向手机号码为 " + phone + " 发送验证码 " + code);
        }

        return;
    }

}
