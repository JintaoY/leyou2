package com.leyou.goods.service;

import com.leyou.goods.client.BrandClient;
import com.leyou.goods.client.CategoryClient;
import com.leyou.goods.client.GoodsClient;
import com.leyou.goods.client.SpecificationClient;
import com.leyou.item.pojo.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class GoodsService {

    @Autowired
    private BrandClient brandClient;

    @Autowired
    private CategoryClient categoryClient;

    @Autowired
    private GoodsClient goodsClient;

    @Autowired
    private SpecificationClient specificationClient;

    /**
     * 根据spuId查询详情页需要显示的数据
     * @param spuId
     * @return
     */
    public Map<String,Object> loadData(Long spuId){
        //把我们想要会写的数据都放在这个map中  可以写成会写的对象
        Map<String,Object> model = new HashMap<>();

        //根据spuId查询spu
        Spu spu = this.goodsClient.querySpuById(spuId);

        //根据spuId查询spuDetail
        SpuDetail spuDetail = this.goodsClient.querySpuDetailBySpuId(spuId);

        //根据spuId获得三级分类的id和name
        List<Map<String,Object>> categoties = new ArrayList<>();
        List<Long> cids = Arrays.asList(spu.getCid1(), spu.getCid2(), spu.getCid3());
        List<String> names = this.categoryClient.queryNameByIds(cids);

        for (int i = 0; i < cids.size(); i++) {
            Map<String,Object> category = new HashMap<>();
            //一个Map类似一个对象
            category.put("id",cids.get(i));
            category.put("name",names.get(i));

            categoties.add(category);
        }

        //根据bid查询brand
        Brand brand = this.brandClient.queryBrandById(spu.getBrandId());

        //根据spuId查询skus
        List<Sku> skus = this.goodsClient.querySkusBySpuId(spuId);

        //根据cid3查询规格参数组以及对应的规格参数
        List<SpecGroup> groups = this.specificationClient.queryGroupWithParamByCid(spu.getCid3());

        //根据cid3查询特殊规格参数的id对应的名字
        Map<Long,String> paramMap = new HashMap<>();
        //查询不是特殊字段的
        List<SpecParam> params = this.specificationClient.queryParams(null, spu.getCid3(), false, null);
        params.forEach(param->{
            paramMap.put(param.getId(),param.getName());
        });


        model.put("spu",spu);
        model.put("spuDetail",spuDetail);
        model.put("categories",categoties);
        model.put("brand",brand);
        model.put("skus",skus);
        model.put("groups",groups);
        model.put("paramMap",paramMap);

        return model;
    }

}
