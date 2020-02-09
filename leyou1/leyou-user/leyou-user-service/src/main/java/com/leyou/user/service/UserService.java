package com.leyou.user.service;

import com.leyou.common.utils.NumberUtils;
import com.leyou.user.mapper.UserMapper;
import com.leyou.user.pojo.User;
import com.leyou.user.utils.CodecUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Service
public class UserService {

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private AmqpTemplate amqpTemplate;

    @Autowired
    private StringRedisTemplate redisTemplate;

    //规范 redis key的存储格式
    private static final String KEY_PREFIX = "user:verfity:";

    /**
     * 检查数据是否可用
     * @param data
     * @param type
     * @return
     */
    public Boolean checkUserData(String data, Integer type) {

        User record = new User();
        switch (type){
            case 1:
                record.setUsername(data);
                break;
            case 2:
                record.setPhone(data);
                break;
            default:
                //返回null  则为请求参数错误  type 默认为null
                return null;
        }

       return this.userMapper.selectCount(record) == 0;
    }

    /**
     *生成短信验证码
     * @param phone
     */
    public void sendVerifyCode(String phone) {

        //判断
        if(StringUtils.isBlank(phone)){
            return;
        }

        //生成验证码
        String code = NumberUtils.generateCode(6);


        //发送消息到rabbitMQ消息队列中
        Map<String,String> msg = new HashMap<>();
            msg.put("phone", phone);
        msg.put("code", code);

        this.amqpTemplate.convertAndSend("LEYOU.SMS.EXCHANGE", "sms.verify.code", msg);

        //把验证码存储到redis中
        this.redisTemplate.opsForValue().set(KEY_PREFIX+phone, code,5, TimeUnit.MINUTES);

    }

    /**
     * 用户注册
     * @param user
     * @param code
     * @return
     */
    public Boolean register(User user, String code) {

        //验证短信验证码
        String redisCode = this.redisTemplate.opsForValue().get(KEY_PREFIX + user.getPhone());

        if(!StringUtils.equals(code, redisCode)){
            return false;
        }

        //生成盐
        String salt = CodecUtils.generateSalt();
        user.setSalt(salt);

        //对密码加墨存储
        user.setPassword(CodecUtils.md5Hex(user.getPassword(), salt));
        user.setId(null);
        user.setCreated(new Date());

        //添加到数据库
        Boolean bool = this.userMapper.insertSelective(user) == 1;

        if(bool){
            this.redisTemplate.delete(KEY_PREFIX + user.getPhone());
        }
        return bool;
    }


    /**
     * 查询用户
     * @param username
     * @param password
     * @return
     */
    public User queryUser(String username, String password) {

        User record = new User();
        record.setUsername(username);
        //根据用户名查询用户
        User user = this.userMapper.selectOne(record);

        if(user == null){
            return null;
        }

        //验证密码
            //把密码和salt进行md5加密后结果

        String md5Password = CodecUtils.md5Hex(password, user.getSalt());
        if(StringUtils.equals(md5Password, user.getPassword())){
            return user;
        }

        return null;
    }
}
