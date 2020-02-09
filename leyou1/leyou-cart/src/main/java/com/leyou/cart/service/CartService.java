package com.leyou.cart.service;

import com.leyou.cart.client.GoodsClient;
import com.leyou.cart.interceptor.LoginInterceptor;
import com.leyou.cart.pojo.Cart;
import com.leyou.common.pojo.UserInfo;
import com.leyou.common.utils.JsonUtils;
import com.leyou.item.pojo.Sku;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.BoundHashOperations;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class CartService {

    @Autowired
    private StringRedisTemplate redisTemplate;

    @Autowired
    private GoodsClient goodsClient;

    private static final String KEY_PREFIX = "user:cart:";


    /**
     * 添加购物车
     * @param cart
     */
    public void addCart(Cart cart) {

        //获得登录用户
        UserInfo userInfo = LoginInterceptor.getLoginUser();
            //redis中的key值
        String key = KEY_PREFIX+userInfo.getId();

        //类型为Map<id,Map<spuId,cart>>  hash操作
        //根据userId获得 Map<spuId,cart>
        //如果存在，则获得；不存在则创建  都为String
        BoundHashOperations<String, Object, Object> hashOps = this.redisTemplate.boundHashOps(key);

        String skuId = cart.getSkuId().toString();
        Integer num = cart.getNum();

        //判断当前商品是否在购物车中
        if(hashOps.hasKey(skuId)){
            //如果存在，则修改数量
                //获得cart的序列化
            String cartJson = hashOps.get(skuId).toString();
            //反序列化
            cart = JsonUtils.parse(cartJson, Cart.class);
            cart.setNum(cart.getNum()+num);

        }else{
            //不存在，则添加购物车

            //根据skuId查询sku
            Sku sku = this.goodsClient.querySkuBySkuId(cart.getSkuId());
            cart.setUserId(userInfo.getId());
            cart.setTitle(sku.getTitle());
            cart.setPrice(sku.getPrice());
            cart.setOwnSpec(sku.getOwnSpec());
            cart.setImage(StringUtils.isBlank(sku.getImages())?"":StringUtils.split(sku.getImages(), ",")[0]);

        }
        //将购物车数据写入redis中
        hashOps.put(skuId, JsonUtils.serialize(cart));

    }

    /**
     * 查询购物车列表
     * @return
     */
    public List<Cart> queryCartList() {

        //获得当前登录的用户信息
        UserInfo userInfo = LoginInterceptor.getLoginUser();
            //redis中的key值
        String key = KEY_PREFIX+userInfo.getId();
        //判断redis是否存在key
        if(!this.redisTemplate.hasKey(key)){
            return null;
        }

        //根据userId获得 Map<spuId,cart>
        BoundHashOperations<String, Object, Object> hashOps = this.redisTemplate.boundHashOps(key);

        //从redis获得的 字符串数组
        List<Object> cartsJson = hashOps.values();

        //判断是否为null
        if(CollectionUtils.isEmpty(cartsJson)){
            return null;
        }

        //序列化一下 成List<Cart>  查询购物车数据
        return  cartsJson.stream().map(cartJson->{
                    return JsonUtils.parse(cartJson.toString(), Cart.class);
                }).collect(Collectors.toList());
    }


    /**
     * 修改购物车商品数量
     * @param cart
     */
    public void updateNum(Cart cart) {

        //获得用户信息
        UserInfo userInfo = LoginInterceptor.getLoginUser();
            //redis中的key userId值
        String  key = KEY_PREFIX+userInfo.getId();

        if(!this.redisTemplate.hasKey(key)){
            return;
        }

        //userId 获得购物车 Map
        BoundHashOperations<String, Object, Object> hashOps = redisTemplate.boundHashOps(key);

        String skuId = cart.getSkuId().toString();
        Integer num = cart.getNum();

        if(!hashOps.hasKey(skuId)){
            return;
        }

        //根据cartId查询获得Cart信息
        String cartJson = hashOps.get(skuId).toString();
        Cart cart1 = JsonUtils.parse(cartJson, Cart.class);

        //更改数量
        cart1.setNum(num);
        //写入购物车
        hashOps.put(skuId, JsonUtils.serialize(cart1));

    }

    /**
     * 删除购物车
     * @param skuId
     */
    public void deleteCart(String skuId) {

        //获得用户信息
        UserInfo userInfo = LoginInterceptor.getLoginUser();
        //redis中的key userId值
        String  key = KEY_PREFIX+userInfo.getId();

        if(!this.redisTemplate.hasKey(key)){
            return;
        }

        //userId 获得购物车 Map
        BoundHashOperations<String, Object, Object> hashOps = this.redisTemplate.boundHashOps(key);

        if(!hashOps.hasKey(skuId)){
            return;
        }

        hashOps.delete(skuId);
    }
}
