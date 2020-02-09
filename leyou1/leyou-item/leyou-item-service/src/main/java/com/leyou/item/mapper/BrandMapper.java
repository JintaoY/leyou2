package com.leyou.item.mapper;

import com.leyou.item.pojo.Brand;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import tk.mybatis.mapper.common.Mapper;

import java.util.List;

public interface BrandMapper extends Mapper<Brand> {


    @Insert("insert into tb_category_brand(category_id,brand_id) VALUES(#{cid},#{bid})")
    void insertCategoryAndBrand(@Param("cid") Long cid, @Param("bid") Long bid);

    @Delete("DELETE from tb_category_brand where brand_id=#{bid}")
    void deleteByBid(@Param("bid") Long bid);

    @Select("select * from tb_brand INNER JOIN tb_category_brand on tb_brand.id = tb_category_brand.brand_id where category_id=#{cid}")
    List<Brand> queryBrandsByCid(@Param("cid") Long cid);

}
