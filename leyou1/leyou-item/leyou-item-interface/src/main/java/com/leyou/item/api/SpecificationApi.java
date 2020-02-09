package com.leyou.item.api;

import com.leyou.item.pojo.SpecGroup;
import com.leyou.item.pojo.SpecParam;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@RequestMapping("spec")
public interface SpecificationApi {


    /**
     * 根据种类id查询起规格参数组信息
     * @param cid
     * @return
     *//*
    @GetMapping("groups/{cid}")
    public List<SpecGroup> queryGroupsByCid(@PathVariable("cid") Long cid);*/


    /**规格参数组id查询其规则参数信息
     * 使用包装类的好处  还有其他的参数
     * 根据
     * @param gid
     * @return
     */
    @GetMapping("params")
    public List<SpecParam> queryParams(
            @RequestParam(value = "gid",required = false) Long gid,
            @RequestParam(value = "cid",required = false) Long cid,
            @RequestParam(value = "generic",required = false) Boolean generic,
            @RequestParam(value = "searching",required = false) Boolean searching

    );

    /**
     * 根据cid  查询到规格参数组及对应的规格参数
     * @param cid
     * @return
     */
    @GetMapping("group/param/{cid}")
    public List<SpecGroup> queryGroupWithParamByCid(@PathVariable("cid") Long cid);

}
