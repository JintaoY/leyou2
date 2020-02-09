package com.leyou.item.service;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.leyou.common.pojo.PageResult;
import com.leyou.item.bo.SpuBo;
import com.leyou.item.mapper.*;
import com.leyou.item.pojo.*;
import org.apache.commons.lang.StringUtils;
import org.springframework.amqp.AmqpException;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tk.mybatis.mapper.entity.Example;

import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class GoodsService {

    @Autowired
    private SpuMapper spuMapper;

    @Autowired
    private SpuDetailMapper spuDetailMapper;

    @Autowired
    private BrandMapper brandMapper;

    @Autowired
    private CategoryService categoryService;

    @Autowired
    private SkuMapper skuMapper;

    @Autowired
    private StockMapper stockMapper;

    @Autowired
    private AmqpTemplate amqpTemplate;

    /**
     * 分页查看Spu商品信息
     * @param key
     * @param saleable
     * @param page
     * @param rows
     * @return
     */
    public PageResult<SpuBo> querySpuBoByPage(String key, Boolean saleable, Integer page, Integer rows) {

        Example example = new Example(Spu.class);
        Example.Criteria criteria = example.createCriteria();

        //搜索条件
        if(StringUtils.isNotBlank(key)){
            criteria.andLike("title","%"+key+"%");
        }

        //是否上架 tinyInt 可以转换
        if(saleable != null){
            criteria.andEqualTo("saleable",saleable);
        }

        //分页条件
        PageHelper.startPage(page,rows);

        //执行结果，获得Spu集合
        List<Spu> spus = this.spuMapper.selectByExample(example);
        //PageInfo
        PageInfo pageInfo = new PageInfo(spus);

        //Spu集合转化为SpuBo集合  stream遍历效率更高
        List<SpuBo> spuBos = spus.stream().map(spu -> {
            SpuBo spuBo = new SpuBo();
            //工具赋值属性
            BeanUtils.copyProperties(spu, spuBo);

            Brand brand = this.brandMapper.selectByPrimaryKey(spu.getBrandId());
            //brandId获得品牌名
            spuBo.setBname(brand.getName());

            //获得一二三级的品牌种类  //初始化集合更方法的做法
            List<String> brandNames = this.categoryService.queryNamesByIds(Arrays.asList(spuBo.getCid1(), spuBo.getCid2(), spuBo.getCid3()));
            spuBo.setCname(StringUtils.join(brandNames, "/"));

            return spuBo;

        }).collect(Collectors.toList());


        //返回PageResult<SpuBo>
        return new PageResult<SpuBo>(pageInfo.getTotal(),spuBos);
    }

    /**
     * 保存商品信息
     * @param spuBo  是一个复杂的对象
     * @return
     */
    @Transactional
    public void saveGoods(SpuBo spuBo) {

        //添加spu信息

        //设置默认字段，有一些没有时间

         //id为null 一定程度防止恶心注入  token

        spuBo.setId(null);
        spuBo.setSaleable(true);
        spuBo.setValid(true);
        spuBo.setCreateTime(new Date());
        spuBo.setLastUpdateTime(spuBo.getCreateTime());

        this.spuMapper.insertSelective(spuBo);

        //添加spu_detail信息
        SpuDetail spuDetail = spuBo.getSpuDetail();
        spuDetail.setSpuId(spuBo.getId());
        this.spuDetailMapper.insertSelective(spuDetail);

        saveSkuAndStock(spuBo);

        //发送消息到交换机
        sendMsg("insert",spuBo.getId());


    }

    /**
     * 封装了发送交换机的信息
     * @param type  操作的类型
     * @param spuId   spuId
     */
    private void sendMsg(String type , Long spuId) {
        //保存商品发送消息到消息队列中(交换机)
        //try-catch 保证发送消息不影响原本逻辑
        try {
            this.amqpTemplate.convertAndSend("item."+type, spuId);
            System.out.println(type+"   "+spuId);
        } catch (AmqpException e) {
            e.printStackTrace();
        }
    }

    /**
     * 抽取方法，保存sku和stock
     * @param spuBo
     */
    private void saveSkuAndStock(SpuBo spuBo) {
        //添加sku信息
        spuBo.getSkus().forEach(sku -> {
            sku.setId(null);
            sku.setSpuId(spuBo.getId());
            sku.setEnable(true);
            sku.setCreateTime(new Date());
            sku.setLastUpdateTime(sku.getCreateTime());

            this.skuMapper.insertSelective(sku);

            //添加stock信息
            Stock stock = new Stock();
            stock.setSkuId(sku.getId());
            stock.setStock(sku.getStock());
            this.stockMapper.insertSelective(stock);

        });
    }

    /**
     * 根据spuId查询spuDetail
     * @param spuId
     * @return
     */
    public SpuDetail querySpuDetailBySpuId(Long spuId) {

        return this.spuDetailMapper.selectByPrimaryKey(spuId);
    }

    /**
     * 根据spuId查询sku集合信息
     * @param spuId
     * @return
     */
    public List<Sku> querySkusBySpuId(Long spuId) {

        Sku record = new Sku();
        record.setSpuId(spuId);

        List<Sku> skus = this.skuMapper.select(record);

        //还需要查询库存信息
        skus.forEach(sku->{

            Stock stock = this.stockMapper.selectByPrimaryKey(sku.getId());
            sku.setStock(stock.getStock());
        });

        return skus;
    }

    /**
     * 更改产品信息
     * @param spuBo
     * @return
     */
    public void updateGoods(SpuBo spuBo) {

        //获得spuId
        Long id = spuBo.getId();
        Sku record = new Sku();
        record.setSpuId(id);

        List<Sku> skus = this.skuMapper.select(record);

        //删除stock
        skus.forEach(sku -> {
            this.stockMapper.deleteByPrimaryKey(sku.getId());

            //删除sku
            this.skuMapper.deleteByPrimaryKey(sku.getId());
        });

        //增加sku
        //增加stock
        this.saveSkuAndStock(spuBo);

        //更改sku_detail


        this.spuDetailMapper.updateByPrimaryKeySelective(spuBo.getSpuDetail());

        //更改sku
        //一些属性不让修改  一定程度防止恶意
        spuBo.setCreateTime(null);
        spuBo.setLastUpdateTime(new Date());
        spuBo.setSaleable(null);
        spuBo.setValid(null);
        this.spuMapper.updateByPrimaryKeySelective(spuBo);


        //发送消息到交换机
        sendMsg("update", spuBo.getId());

    }

    /**
     * 根据spuId查询Spu对象
     * @param id
     * @return
     */
    public Spu querySpuById(Long id) {

        return this.spuMapper.selectByPrimaryKey(id);
    }

    /**
     * 根据skuId查询Sku
     * @param skuId
     * @return
     */
    public Sku querySkuBySkuId(Long skuId) {

        return  this.skuMapper.selectByPrimaryKey(skuId);
    }
}
