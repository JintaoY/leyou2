package com.leyou.item.mapper;

import com.leyou.item.pojo.Category;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import tk.mybatis.mapper.additional.idlist.SelectByIdListMapper;
import tk.mybatis.mapper.common.Mapper;

import java.util.List;

public interface CategoryMapper extends Mapper<Category>,SelectByIdListMapper<Category,Long> {

    @Select("SELECT * from tb_category where id in(SELECT category_id from tb_category_brand where brand_id = #{bid})")
    List<Category> queryByBrandId(@Param("bid") Long bid);
}
