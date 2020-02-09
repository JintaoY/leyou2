package com.leyou.item.service;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.leyou.common.pojo.PageResult;
import com.leyou.item.mapper.BrandMapper;
import com.leyou.item.pojo.Brand;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tk.mybatis.mapper.entity.Example;

import java.util.List;

@Service
public class BrandService {

    @Autowired
    private BrandMapper brandMapper;


    public PageResult<Brand> queryBrandsByPage(String key, Integer page, Integer rows, String sortBy, Boolean desc) {

        //初始化example对象
        Example example = new Example(Brand.class);
        Example.Criteria criteria = example.createCriteria();

        //根据name进行模糊查询或者根据首字母查询  isNotBlank范围比empty更大
        if(StringUtils.isNotBlank(key)){
            criteria.andLike("name","%"+key+"%").orEqualTo("letter",key);
        }

        //添加分页 第几页，每页多少个
        PageHelper.startPage(page,rows);

        if(StringUtils.isNotBlank(sortBy)){
            example.setOrderByClause(sortBy+" "+(desc ? "desc" : "asc"));
        }

        List<Brand> brands = this.brandMapper.selectByExample(example);
        //包装为分页对象，更好的获得值
        PageInfo<Brand> pageInfo = new PageInfo<>(brands);
        //把分页对象 封装为自定义的分类对象并访问
        return new PageResult<Brand>(pageInfo.getTotal(),pageInfo.getList());

    }

    /**
     * 新增 品牌
     * @param brand
     * @param cids
     */
    @Transactional //事务管理
    public void saveBrand(Brand brand, List<Long> cids) {

        //先新增到Brand表中
        this.brandMapper.insertSelective(brand);

        //在新增到中间表  中间表不应该有pojo对象 自己写sql即可
        cids.forEach(cid->{
            this.brandMapper.insertCategoryAndBrand(cid,brand.getId());
        });

    }

    /**
     * 修改 品牌
     * @param brand
     * @param cids
     */
    @Transactional //事务管理
    public void updateBrand(Brand brand, List<Long> cids) {

        //先修改到Brand表中
        this.brandMapper.updateByPrimaryKeySelective(brand);

        //先删除再增加  （不知道有没有更好的办法）
        this.brandMapper.deleteByBid(brand.getId());

        cids.forEach(cid->{
            this.brandMapper.insertCategoryAndBrand(cid,brand.getId());
        });

    }


    /**
     * 删除品牌
     * @param bid
     */
    @Transactional
    public void deleteBrandByBid(Long bid) {

        //先删除Brand表信息
        this.brandMapper.deleteByPrimaryKey(bid);

        //删除中间表信息
        this.brandMapper.deleteByBid(bid);
}

    /**
     * 根据分类id查询品牌信息
     * @param cid
     * @return
     */
    public List<Brand> queryBrandsByCid(Long cid) {

        return this.brandMapper.queryBrandsByCid(cid);
    }

    /**
     * 根据brandId查询Brand
     * @param id
     * @return
     */
    public Brand quertBrandById(Long id) {

        return this.brandMapper.selectByPrimaryKey(id);
    }
}
