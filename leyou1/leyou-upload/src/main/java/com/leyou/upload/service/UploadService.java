package com.leyou.upload.service;

import com.github.tobato.fastdfs.domain.StorePath;
import com.github.tobato.fastdfs.service.FastFileStorageClient;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

@Service
public class UploadService {

    //初始化常量list 使用Arrays.asList
    private static final List<String> CONTENT_TYPES = Arrays.asList("image/jpeg",
            "image/gif","image/png");

    private static final Logger LOGGER = LoggerFactory.getLogger(UploadService.class);

    //存储客户端 工具类而已
    @Autowired
    private FastFileStorageClient storageClient;

    public String upload(MultipartFile file) {

        String originalFilename = file.getOriginalFilename();

        //检查文件的类型
        //可以检查文件的后缀
        //StringUtils.substringAfterLast(file.getOriginalFilename(),".");
        //但是这里用其他的方式，判断文件类型
        String contentType = file.getContentType();

        if(!CONTENT_TYPES.contains(contentType)){
            //当文件类型不存在时 返回null

            //输出日志  {} 占位符
            LOGGER.info("文件类型不合法 {}",originalFilename);

            return null;
        }

        //检查文件的内容  使用ImageIO  我们的验证码也是基于它
        try {
            BufferedImage bufferedImage = ImageIO.read(file.getInputStream());

            if(bufferedImage == null){
                //当bufferedImage 为null 返回null

                LOGGER.info("文件内容不合法 {}",originalFilename);

                return null;
            }


            //保存到服务器
            //file.transferTo(new File("D:\\javaeeeclipse\\heima\\image\\" + originalFilename));

            String ext = StringUtils.substringAfterLast(originalFilename, ".");//获得文件名后缀
            // 上传并保存图片，参数：1-上传的文件流 2-文件的大小 3-文件的后缀 4-可以不管他
            StorePath storePath = this.storageClient.uploadFile(file.getInputStream(), file.getSize(), ext, null);

            //生成url地址，返回
            //return "http://image.leyou.com/"+originalFilename;
            return "http://image.leyou.com/"+storePath.getFullPath();

        } catch (IOException e) {

            LOGGER.info("服务器内部错误 {}",originalFilename);

            e.printStackTrace();
        }

        return null;
    }
}
