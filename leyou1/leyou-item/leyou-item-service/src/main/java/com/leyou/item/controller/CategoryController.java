package com.leyou.item.controller;

import com.leyou.item.pojo.Category;
import com.leyou.item.service.CategoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@Controller
@RequestMapping("category")
public class CategoryController {

    @Autowired
    private CategoryService categoryService;


    /**
     * 根据父节点的id查找子节点
     * @param pid
     * @return
     */
    @GetMapping("list")
    public ResponseEntity<List<Category>> queryCategoryByPid(@RequestParam(value = "pid",defaultValue = "0") Long pid ){

        //不用写try-catch  Spring的异常处理机制
        if(pid == null || pid < 0){
            //400:参数不合适
            //return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
            return ResponseEntity.badRequest().build();
        }
        List<Category> categories = this.categoryService.queryCategoryByPid(pid);

        //if(categories == null || categories.size() = 0)
        if(CollectionUtils.isEmpty(categories)){
            //404:服务器资源没有找到
            //return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
            return ResponseEntity.notFound().build();
        }
        //200 查询成功
        return ResponseEntity.ok(categories);

    }


    /**
     * 通过品牌id查询产品分类
     * @param bid
     * @return
     */
    @GetMapping("bid/{bid}")
    public ResponseEntity<List<Category>> queryByBrandId(@PathVariable("bid") Long bid){

        List<Category> categories = this.categoryService.queryByBrandId(bid);

        if(CollectionUtils.isEmpty(categories)){
            //404
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok(categories);
    }

    /**
     * 根据多个cid 返回多个cname
     * @param ids
     * @return
     */
    @GetMapping("name")
    public ResponseEntity<List<String>> queryNamesByIds(@RequestParam("ids") List<Long> ids){
        List<String> names = this.categoryService.queryNamesByIds(ids);

        if(CollectionUtils.isEmpty(names)){
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok(names);
    }



}
