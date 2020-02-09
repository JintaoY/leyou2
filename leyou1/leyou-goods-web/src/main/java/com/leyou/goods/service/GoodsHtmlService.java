package com.leyou.goods.service;

import com.leyou.common.utils.ThreadUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.Map;

@Service
public class GoodsHtmlService {

    @Autowired
    private TemplateEngine engine;

    @Autowired
    private GoodsService goodsService;

    /**
     * 创建静态页面
     */
    public void createHtml(Long spuId) throws FileNotFoundException {
        PrintWriter write = null;

            //获得详情页的数据
            Map<String, Object> data = this.goodsService.loadData(spuId);

            //初始化thymeleaf上下文
            Context context = new Context();
            //把一个map数据放入上下文中
            context.setVariables(data);

            //创建数据流
            File file = new File("D:\\application\\nignx\\nginx-1.14.0\\html\\item\\"+spuId+".html");
            write = new PrintWriter(file);

            this.engine.process("item",context,write);

            //如果输出流不为null则关掉
            if(write != null){
                write.close();
            }

    }


    /**
     * 新建一个线程处理页面静态化   提高效率
     * 因为创建一个 静态页面太久，新建一个线程 可以变得更快
     * @param spuId
     */
    public void asyncExcute(Long spuId){

        //更简洁的写法
        ThreadUtils.execute(()-> {
            try {
                createHtml(spuId);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
        });

        /*ThreadUtils.execute(new Runnable() {
            @Override
            public void run() {
                createHtml(spuId);
            }
        });*/

    }

    /**
     * 删除静态页面
     * @param spuId
     */
    public void deleteHtml(Long spuId) {
        //指定路径删除
        File file = new File("D:\\application\\nignx\\nginx-1.14.0\\html\\item\\"+spuId+".html");

        file.deleteOnExit();

    }
}
