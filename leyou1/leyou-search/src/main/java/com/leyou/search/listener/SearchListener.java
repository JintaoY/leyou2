package com.leyou.search.listener;

import com.leyou.search.service.SearchService;
import org.springframework.amqp.core.ExchangeTypes;
import org.springframework.amqp.rabbit.annotation.Exchange;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.QueueBinding;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class SearchListener {

    @Autowired
    private SearchService searchService;


    /**
     * 异常往上抛 默认配置ACK  Spring 的特性AOP
     * @param spuId
     * @throws IOException
     */
    @RabbitListener(bindings = @QueueBinding(
            value = @Queue(value = "LEYOU.SEARCH.SAVE.QUEUE",durable = "true"),
            exchange = @Exchange(value = "LEYOU.ITEM.EXCHANGE",type = ExchangeTypes.TOPIC,ignoreDeclarationExceptions = "true"),
            key = {"item.insert","item.update"}
    ))
    public void save(Long spuId) throws IOException {
        System.out.println(spuId);
        if(spuId == null){
            return;
        }

        //索引库的保存数据
        this.searchService.save(spuId);

    }


    /**
     * 异常往上抛 默认配置ACK  Spring 的特性AOP
     * @param spuId
     * @throws IOException
     */
    @RabbitListener(bindings = @QueueBinding(
            value = @Queue(value = "LEYOU.SEARCH.DELETE.QUEUE",durable = "true"),
            exchange = @Exchange(value = "LEYOU.ITEM.EXCHANGE",type = ExchangeTypes.TOPIC,ignoreDeclarationExceptions = "true"),
            key = {"item.delete"}
    ))
    public void delete(Long spuId) throws IOException {

        if(spuId == null){
            return;
        }

        //索引库的保存数据
        this.searchService.delete(spuId);

    }
}
