package com.leyou.item.controller;

import com.leyou.common.pojo.PageResult;
import com.leyou.item.bo.SpuBo;
import com.leyou.item.pojo.Sku;
import com.leyou.item.pojo.Spu;
import com.leyou.item.pojo.SpuDetail;
import com.leyou.item.service.GoodsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
public class GoodsController {

    @Autowired
    private GoodsService goodsService;


    /**
     * 分页查看Spu商品信息
     * @param key
     * @param saleable
     * @param page
     * @param rows
     * @return
     */
    @GetMapping("spu/page")
    public ResponseEntity<PageResult<SpuBo>> querySpuBoByPage(
            @RequestParam(value = "key",required = false) String key,
            @RequestParam(value = "saleable",required = false) Boolean saleable,
            @RequestParam(value = "page",defaultValue = "1") Integer page,
            @RequestParam(value = "rows",defaultValue = "5") Integer rows
    ){

        PageResult<SpuBo> result = this.goodsService.querySpuBoByPage(key,saleable,page,rows);

        //判断result是否为null 或者 items 是否为空
        if(result == null || CollectionUtils.isEmpty(result.getItems())){

            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok(result);
    }


    /**
     * 保存商品信息
     * @param spuBo  是一个复杂的对象
     * @return
     */
    @PostMapping("goods")
    public ResponseEntity<Void> saveGoods(@RequestBody SpuBo spuBo){

        this.goodsService.saveGoods(spuBo);

        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    /**
     * 根据spuId查询spuDetail
     * @param spuId
     * @return
     */
    @GetMapping("spu/detail/{spuId}")
    public ResponseEntity<SpuDetail> querySpuDetailBySpuId(@PathVariable("spuId") Long spuId){

        SpuDetail spuDetail = this.goodsService.querySpuDetailBySpuId(spuId);

        if(spuDetail == null){
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok(spuDetail);
    }


    /**
     * 根据spuId查询sku集合信息
     * @param spuId
     * @return
     */
    @GetMapping("sku/list")
    public ResponseEntity<List<Sku>> querySkusBySpuId(@RequestParam("id") Long spuId){

        List<Sku> skus = this.goodsService.querySkusBySpuId(spuId);

        if(CollectionUtils.isEmpty(skus)){
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok(skus);
    }

    /**
     * 更改产品信息
     * @param spuBo
     * @return
     */
    @PutMapping("goods")
    public ResponseEntity<Void> updateGoods(@RequestBody SpuBo spuBo ){

        this.goodsService.updateGoods(spuBo);

        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();

    }

    /**
     * 根据spuId查询Spu对象
     * @param id
     * @return
     */
    @GetMapping("spu/{id}")
    public ResponseEntity<Spu> querySpuById(@PathVariable("id") Long id){

        Spu spu = this.goodsService.querySpuById(id);

        if(spu == null){
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok(spu);
    }

    /**
     * 根据skuId查询Sku
     * @param skuId
     * @return
     */
    @GetMapping("skuId/{skuId}")
    public ResponseEntity<Sku> querySkuBySkuId(@PathVariable("skuId") Long skuId){

        Sku sku = this.goodsService.querySkuBySkuId(skuId);

        if(sku == null){
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok(sku);
    }


}
