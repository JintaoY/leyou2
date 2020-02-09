package com.leyou.upload.controller;


import com.leyou.upload.service.UploadService;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

@Controller
@RequestMapping("upload")
public class UploadController {

    @Autowired
    private UploadService uploadService;

    @PostMapping("image")
    public ResponseEntity<String> uploadImage(@RequestParam("file") MultipartFile file){
        //返回该图片的url
        String url = this.uploadService.upload(file);

        if(StringUtils.isBlank(url)){
            //当放回为空时"" 或者null 时  说明请求错误，也是请求参数错误
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }

        //请求回返成功  为201  已在服务器上成功创建了一个或多个新资源
        return ResponseEntity.status(HttpStatus.CREATED).body(url);
    }
}
