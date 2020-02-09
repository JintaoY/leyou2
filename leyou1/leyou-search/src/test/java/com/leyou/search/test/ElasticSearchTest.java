package com.leyou.search.test;

import com.leyou.common.pojo.PageResult;
import com.leyou.item.bo.SpuBo;
import com.leyou.search.client.GoodsClient;
import com.leyou.search.pojo.Goods;
import com.leyou.search.respository.GoodsRespository;
import com.leyou.search.service.SearchService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.elasticsearch.core.ElasticsearchTemplate;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

@SpringBootTest()
@RunWith(SpringRunner.class)
public class ElasticSearchTest {

    @Autowired
    private ElasticsearchTemplate elasticsearchTemplate;

    @Autowired
    private GoodsRespository goodsRespository;

    @Autowired
    private SearchService searchService;

    @Autowired
    private GoodsClient goodsClient;

    @Test
    public void createIndex(){
        this.elasticsearchTemplate.createIndex(Goods.class);
        this.elasticsearchTemplate.putMapping(Goods.class);

        int start = 1;
        int rows = 100;

        do {
            PageResult<SpuBo> result = goodsClient.querySpuBoByPage(null, null, start, rows);
            List<SpuBo> spuBos = result.getItems();
            //把spus转化为goods list
            List<Goods> goods = spuBos.stream().map(
                    spuBo -> {
                        try {
                            return this.searchService.buildGoods(spuBo);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        return null;
                    }
            ).collect(Collectors.toList());
            this.goodsRespository.saveAll(goods);
            //每次循环，页码加1
            start++;
            //也可以获得总条数 到了最后一页，页数就没有100条了  更是这样更方便
            rows = goods.size();
        }while (rows == 100);

    }
}
