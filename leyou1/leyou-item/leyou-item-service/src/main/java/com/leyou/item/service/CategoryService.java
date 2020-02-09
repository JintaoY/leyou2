package com.leyou.item.service;

import com.leyou.item.mapper.CategoryMapper;
import com.leyou.item.pojo.Category;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class CategoryService {


    @Autowired
    private CategoryMapper categoryMapper;

    /**
     * 根据父节点查找子节点
     * @param pid
     * @return
     */
    public List<Category> queryCategoryByPid(Long pid) {
        Category record = new Category();
        record.setParentId(pid);

        return this.categoryMapper.select(record);
    }

    /**
     * 根据品牌id查询产品种类
     * @param bid
     * @return
     */
    public List<Category> queryByBrandId(Long bid) {

        List<Category> categories = this.categoryMapper.queryByBrandId(bid);

        return categories;
    }

    /**
     * 根据多个id查询多个名
     * @param ids
     * @return
     */
    public List<String> queryNamesByIds(List<Long> ids){

        //可以通过一个一个来获得，但是有更便捷的方法
        List<Category> categories = this.categoryMapper.selectByIdList(ids);

        List<String> names = new ArrayList<String>();
        for (Category category:categories){
            names.add(category.getName());
        }
        return names;
        //return categories.stream().map(category -> category.getName()).collect(Collectors.toList());

    }


}
