package com.leyou.item.api;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;


public interface CategoryApi {


    /**
     * 根据多个cid 返回多个cname
     * @param ids
     * @return
     */
    @GetMapping("category/name")
    List<String> queryNameByIds(@RequestParam("ids") List<Long> ids);



}
