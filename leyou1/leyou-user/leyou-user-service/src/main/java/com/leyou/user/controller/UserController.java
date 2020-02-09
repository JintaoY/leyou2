package com.leyou.user.controller;

import com.leyou.user.pojo.User;
import com.leyou.user.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import javax.validation.Valid;

@Controller
public class UserController {

    @Autowired
    private UserService userService;

    /**
     * 数据验证
     * @param data
     * @param type
     * @return
     */
    @GetMapping("/check/{data}/{type}")
    public ResponseEntity<Boolean> checkUserData(@PathVariable("data") String data,
           @PathVariable("type") Integer type){

        Boolean bool = this.userService.checkUserData(data,type);

        if(bool == null){
            //错误的请求参数
            return  ResponseEntity.badRequest().build();
        }

        return ResponseEntity.ok(bool);
    }

    /**
     * 生成短信验证码
     * @param phone
     * @return
     */
    @PostMapping("code")
    public ResponseEntity<Void> sendVerifyCode(@RequestParam(value = "phone") String phone){

        this.userService.sendVerifyCode(phone);

        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    /**
     * 用户注册
     * @param user
     * @param code
     * @return
     */
    @PostMapping("register")
    public ResponseEntity<Void> register(@Valid User user, @RequestParam("code")String code){

        //验证 用户名 和  手机号码
        Boolean boolUsername = this.userService.checkUserData(user.getUsername(), 1);
        Boolean boolPhone = this.userService.checkUserData(user.getPhone(), 2);

        if(boolUsername == null || !boolUsername || boolPhone == null || !boolPhone){
            return ResponseEntity.badRequest().build();
        }


        Boolean bool = this.userService.register(user,code);

        if(bool == null || !bool){
            return ResponseEntity.badRequest().build();
        }

        return ResponseEntity.status(HttpStatus.CREATED).build();
    }


    /**
     * 登录
     * @param username
     * @param password
     * @return
     */
    @GetMapping("query")
    public ResponseEntity<User> queryUser(@RequestParam("username") String username,
                                           @RequestParam("password") String password){

        User user = this.userService.queryUser(username,password);

        if(user == null){
            return ResponseEntity.badRequest().build();
        }

        return ResponseEntity.ok(user);
    }

}
