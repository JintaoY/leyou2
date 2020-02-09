package com.leyou.search.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.leyou.item.pojo.*;
import com.leyou.search.client.BrandClient;
import com.leyou.search.client.CategoryClient;
import com.leyou.search.client.GoodsClient;
import com.leyou.search.client.SpecificationClient;
import com.leyou.search.pojo.Goods;
import com.leyou.search.pojo.SearchRequest;
import com.leyou.search.pojo.SearchResult;
import com.leyou.search.respository.GoodsRespository;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.math.NumberUtils;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.Operator;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.aggregations.Aggregation;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.bucket.terms.LongTerms;
import org.elasticsearch.search.aggregations.bucket.terms.StringTerms;
import org.elasticsearch.search.sort.SortBuilders;
import org.elasticsearch.search.sort.SortOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.elasticsearch.core.aggregation.AggregatedPage;
import org.springframework.data.elasticsearch.core.query.FetchSourceFilter;
import org.springframework.data.elasticsearch.core.query.NativeSearchQueryBuilder;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class SearchService {

    @Autowired
    private CategoryClient categoryClient;

    @Autowired
    private BrandClient brandClient;

    @Autowired
    private GoodsClient goodsClient;

    @Autowired
    private SpecificationClient specificationClient;

    @Autowired
    private GoodsRespository goodsRespository;


    //序列化工具
    private static final ObjectMapper MAPPER = new ObjectMapper();


    /**
     * 通过spu 建立 Good
     * @param spu
     * @return
     * @throws IOException
     */
    public Goods buildGoods(Spu spu) throws IOException {

        Goods goods = new Goods();


        //根据分类id查询分类名
        List<String> names = this.categoryClient.queryNameByIds(Arrays.asList(spu.getCid1(), spu.getCid2(), spu.getCid3()));

        //根据bid查询Brand
        Brand brand = this.brandClient.queryBrandById(spu.getBrandId());

        //根据spu_id查询sku集合
        List<Sku> skus = this.goodsClient.querySkusBySpuId(spu.getId());
        //根据skus获得sku的所有的价格
        List<Long> prices = new ArrayList<Long>();

        //收集sku的必要字段  4个已经够了
        List<Map<String,Object>> skuMapList = new ArrayList<>();
        skus.forEach(sku -> {
            prices.add(sku.getPrice());

            Map<String,Object> map = new HashMap<>();
            map.put("id",sku.getId());
            map.put("title",sku.getTitle());
            map.put("price",sku.getPrice());
            //images 可能是多张，其中以，分割；其实都为一张
            map.put("image",StringUtils.isBlank(sku.getImages())?"":StringUtils.split(sku.getImages(),","));

            skuMapList.add(map);
        });

        //根据cid3搜索所有作为搜索的规格参数  需要对应的规格参数值
        List<SpecParam> params = this.specificationClient.queryParams(null, spu.getCid3(), null, true);

        //根据spuId查询spuDetail
        SpuDetail spuDetail = this.goodsClient.querySpuDetailBySpuId(spu.getId());
        //通用规则参数值，进行反序列化  写成json格式可以反序列化加上为了节约数据
        Map<String,Object> genericSpecMap =  MAPPER.readValue(spuDetail.getGenericSpec(),new TypeReference<Map<String,Object>>(){});
        //特殊规则参数值，进行反序列化   一对多的关系
        Map<String,List<Object>> specialSpecMap =  MAPPER.readValue(spuDetail.getSpecialSpec(),new TypeReference<Map<String,List<Object>>>(){});

        //定义map接收 当前sku的搜索参数和对应的值
        Map<String,Object> specs = new HashMap<>();

        params.forEach(param->{
            //判断规格参数是否为通用
            if(param.getGeneric()){
                String value = genericSpecMap.get(param.getId().toString()).toString();
                //如果是数字类型
                if(param.getNumeric()){
                    value = chooseSegment(value, param);
                }
                specs.put(param.getName(),value);
            }else{
                List<Object> objects = specialSpecMap.get(param.getId().toString());
                //因为是多选的 不用判断数值型
                specs.put(param.getName(),objects);
            }
        });

        goods.setId(spu.getId());
        goods.setSubTitle(spu.getSubTitle());
        goods.setCid1(spu.getCid1());
        goods.setCid2(spu.getCid2());
        goods.setCid3(spu.getCid3());
        goods.setBrandId(spu.getBrandId());
        goods.setCreateTime(spu.getCreateTime());

        //加空格防止乱分词  有title和三个分类名+品牌名
        goods.setAll(spu.getTitle()+" "+ StringUtils.join(names," ")+" "+brand.getName());
        //一个spu下的sku的所有价格
        goods.setPrice(prices);
        //spu下的所有的sku集合
        goods.setSkus(MAPPER.writeValueAsString(skuMapList));

        //获得所有查询的规格参数 {name:value}  json序列化
        goods.setSpecs(specs);



        return goods;

    }

    /**
     * 通过值 返回 一个区间 []
     * @param value
     * @param p
     * @return
     */
    public String chooseSegment(String value,SpecParam p){

        double val = NumberUtils.toDouble(value);
        String result = "其他";

        String[] segments = p.getSegments().split(",");
        for(String segment:segments){
            String[] segs = segment.split("-");
            //提取范围
            Double begin = NumberUtils.toDouble(segs[0]);
            Double end = Double.MAX_VALUE;
            if(segs.length == 2){
                end = NumberUtils.toDouble(segs[1]);
            }
            //判断是否在这个判断之内
            if(val>=begin && val<end){
                if(segs.length == 1){
                    result = segs[0] + p.getUnit() + "以上";
                }else if(begin == 0){
                    result = segs[0] + p.getUnit() + "以下";
                }else{
                    result = segment + p.getUnit();
                }
                break;
            }
        }
        return result;
    }


    /**
     * 使用elasticSearch查询
     * 根据查询条件返回分页结构集
     * @param search
     * @return
     */
    public SearchResult search(SearchRequest search) {

        if(StringUtils.isBlank(search.getKey())){
            search.setKey("手机");
        }

        //自定义查询构造器
        NativeSearchQueryBuilder queryBuilder = new NativeSearchQueryBuilder();
        //对all字段记性全文检索查询  查询并且为and  一定为并集 不能出现小米手机和小米电视
        //抽取，另外一个方法可以用
        //QueryBuilder basicQuery = QueryBuilders.matchQuery("all", search.getKey()).operator(Operator.AND);

        //建立boolean查询
        BoolQueryBuilder basicQuery = buildBooleanQueryBuilder(search);

        queryBuilder.withQuery(basicQuery);
        //分页  记得减1
        queryBuilder.withPageable(PageRequest.of(search.getPage()-1 ,search.getSize()));
        //过滤查询结构 只需要这三个字段  skus  可以负责图片和价格了
        queryBuilder.withSourceFilter(new FetchSourceFilter(new String[]{"id","subTitle","skus"},null));

        //排序
        String sortBy = search.getSortBy();
        Boolean descending = search.getDescending();
        if(StringUtils.isNotBlank(sortBy)){
            queryBuilder.withSort(SortBuilders.fieldSort(sortBy).order(descending? SortOrder.DESC:SortOrder.ASC));
        }

        //添加分类和品牌的聚合
        String categoryAggName = "categories";
        String brandAggName = "brands";
        queryBuilder.addAggregation(AggregationBuilders.terms(categoryAggName).field("cid3"));
        queryBuilder.addAggregation(AggregationBuilders.terms(brandAggName).field("brandId"));

        //强转  AggregatedPage 可以获得有聚合
        AggregatedPage<Goods> pageInfo = (AggregatedPage)this.goodsRespository.search(queryBuilder.build());

        //获得聚合结果集并且解析
        List<Map<String,Object>> categories = getCategoryAggResult(pageInfo.getAggregation(categoryAggName));
        List<Brand> brands = getBrandAggResult(pageInfo.getAggregation(brandAggName));

        //判断是否为一个分类，并且只有分类时才能做规格聚合  然后也因此从这个种类我们去找出我们要进行的规格聚合字段
        //然后去从我们的elasticSearch 大数据中找出对应的聚合
        List<Map<String,Object>> specs = new ArrayList<>();
        if(!CollectionUtils.isEmpty(categories) && categories.size() == 1){
            //因为这个参数的比较复杂，要进行一些额外的处理，比如查看类，所以很复杂，我们另外封装一个方法去写
            //参数1  分类id  
            specs = getParamAggResult((Long)categories.get(0).get("id"),basicQuery);
        }

        //返回封装数据
        return new SearchResult(pageInfo.getTotalElements(),pageInfo.getTotalPages(),pageInfo.getContent(),categories,brands,specs);
    }

    /**
     * 构建boolean查询器
     * @param search
     * @return
     */
    private BoolQueryBuilder buildBooleanQueryBuilder(SearchRequest search) {
        //初始化boolean查询器
        BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery();

        //添加基本查询
        boolQueryBuilder.must(QueryBuilders.matchQuery("all",search.getKey()).operator(Operator.AND));

        //添加过滤条件
        if(CollectionUtils.isEmpty(search.getFilter())){
            return boolQueryBuilder;
        }

        for (Map.Entry<String, Object> entry : search.getFilter().entrySet()) {

            String key = entry.getKey();
            //如果是品牌
            if(StringUtils.equals(key,"品牌")){
                key = "brandId";
            }
            //如果是分类
            else if(StringUtils.equals(key,"分类")){
                key = "cid3";
            }
            //这是规格参数
            else{
                key = "specs."+key+".keyword";
            }

            boolQueryBuilder.filter(QueryBuilders.termQuery(key,entry.getValue()));
        }
        return  boolQueryBuilder;
    }


    /**
     * 根据查询条件聚合规格参数
     * @param cid
     * @param basicQuery
     * @return
     */
    private List<Map<String, Object>> getParamAggResult(Long cid, QueryBuilder basicQuery) {
        //自定义查询对象构建
        NativeSearchQueryBuilder queryBuilder = new NativeSearchQueryBuilder();
        //添加基本查询
        queryBuilder.withQuery(basicQuery);

        //查询要聚合的规格参数
        List<SpecParam> params = this.specificationClient.queryParams(null, cid, null, true);
        //添加规格参数的聚合
        params.forEach(param->{
            queryBuilder.addAggregation(AggregationBuilders.terms(param.getName()).field("specs."+param.getName()+".keyword"));
        });

        //添加结果集过滤
        queryBuilder.withSourceFilter(new FetchSourceFilter(new String[]{},null));

        //执行聚合查询
        AggregatedPage<Goods> goodsPage = (AggregatedPage<Goods>)this.goodsRespository.search(queryBuilder.build());

        //初始化一个集合，收集聚合结果集
        List<Map<String, Object>> paramMapList = new ArrayList<>();

        //解析聚合结果集
        Map<String, Aggregation> aggregationMap = goodsPage.getAggregations().asMap();
        for (Map.Entry<String, Aggregation> entry : aggregationMap.entrySet()) {
            //初始化一个map {key:规格参数名，options:聚合的规格参数值}
            Map<String,Object> map = new HashMap<>();
            map.put("k",entry.getKey());
            //初始化options为一个list，手机桶中的所有key值
            List<String> options = new ArrayList<>();
            //解析每个聚合
            StringTerms terms = (StringTerms)entry.getValue();
            terms.getBuckets().forEach(bucket -> {
                //遍历桶中的key，把key放在list集合中
                options.add(bucket.getKeyAsString());
            });

            map.put("options",options);

            paramMapList.add(map);
        }


        return paramMapList;
    }

    /**
     * 解析品牌的聚合结果集
     * @param aggregation
     * @return
     */
    private List<Brand> getBrandAggResult(Aggregation aggregation) {
        //强转获得桶
        LongTerms terms = (LongTerms)aggregation;
        //使用stream 将一个集合转为话另外一个集合
        List<Brand> brands = terms.getBuckets().stream().map(
                bucket -> {
                    return this.brandClient.queryBrandById(bucket.getKeyAsNumber().longValue());
                }
        ).collect(Collectors.toList());

        return brands;
    }

    /**
     * 解析分类的聚合结果集
     * @param aggregation
     * @return
     */
    private List<Map<String, Object>> getCategoryAggResult(Aggregation aggregation) {
        //强转
        LongTerms terms = (LongTerms)aggregation;
        //解析集合结果的桶，把桶集合转成为 分类的集合
        List<Map<String, Object>> categories = terms.getBuckets().stream().map(
                bucket -> {
                    //new 一个list的类型 到时候进行返回
                    Map<String, Object> map = new HashMap<>();
                    //查询出分类名 并获得第一个即是
                    List<String> names = this.categoryClient.queryNameByIds(Arrays.asList(bucket.getKeyAsNumber().longValue()));
                    //只需要id  和 name
                    map.put("id", bucket.getKeyAsNumber().longValue());
                    map.put("name", names.get(0));

                    return map;
                }
        ).collect(Collectors.toList());


        return categories;
    }


    /**
     * 增加goods到索引库
     * @param spuId
     */
    public void save(Long spuId) throws IOException {
        //根据spuId查询spu
        Spu spu = this.goodsClient.querySpuById(spuId);

        Goods goods = buildGoods(spu);
        this.goodsRespository.save(goods);
    }


    /**
     * 根据id删除goods  spuId 即为Goods的 Id
     * @param spuId
     */
    public void delete(Long spuId) {
        this.goodsRespository.deleteById(spuId);
    }
}
