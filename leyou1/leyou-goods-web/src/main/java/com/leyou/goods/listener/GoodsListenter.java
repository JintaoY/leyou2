package com.leyou.goods.listener;

import com.leyou.goods.service.GoodsHtmlService;
import org.springframework.amqp.core.ExchangeTypes;
import org.springframework.amqp.rabbit.annotation.Exchange;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.QueueBinding;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class GoodsListenter {

    @Autowired
    private GoodsHtmlService goodsHtmlService;

    /**
     * 监听保存和修改的提交消息
     * @param spuId
     */
    @RabbitListener(bindings = @QueueBinding(
            value = @Queue(value = "LEYOU.CREATE.WEB.QUEUE", durable = "true"),
            exchange = @Exchange(
                    value = "LEYOU.ITEM.EXCHANGE",
                    ignoreDeclarationExceptions = "true",
                    type = ExchangeTypes.TOPIC),
            key = {"item.insert", "item.update"}))
    public void save(Long spuId) throws Exception {
        System.out.println("spuId"+spuId);
        if(spuId == null){

            return;
        }

        //调用goodsHtmlService 进行创建静态页面；修改则是创建覆盖
        //this.goodsHtmlService.createHtml(spuId);
        //可以有异步创建静态页面
        this.goodsHtmlService.createHtml(spuId);
    }


    /**
     * 监听删除的信息
     * @param spuId
     */
    @RabbitListener(bindings = @QueueBinding(
            value = @Queue(value = "LEYOU.DELETE.WEB.QUEUE", durable = "true"),
            exchange = @Exchange(
                    value = "LEYOU.ITEM.EXCHANGE",
                    ignoreDeclarationExceptions = "true",
                    type = ExchangeTypes.TOPIC),
            key = "item.delete"))
    public void delete(Long spuId){

        if(spuId == null){
            return;
        }
        //调用goodsHtmlService 进行创建静态页面；修改则是创建覆盖
        this.goodsHtmlService.deleteHtml(spuId);
    }



}
