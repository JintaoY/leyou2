package com.leyou.item.controller;

import com.leyou.item.pojo.SpecGroup;
import com.leyou.item.pojo.SpecParam;
import com.leyou.item.service.SpecificationService;
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
@RequestMapping("spec")
public class SpecificationController {

    @Autowired
    private SpecificationService specificationService;

    /**
     * 根据种类id查询起规格参数组信息
     * @param cid
     * @return
     */
    @GetMapping("groups/{cid}")
    public ResponseEntity<List<SpecGroup>> queryGroupsByCid(@PathVariable("cid") Long cid){

        List<SpecGroup> specGroups = this.specificationService.queryGroupsByCid(cid);

        if(CollectionUtils.isEmpty(specGroups)){
            //404
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok(specGroups);
    }


    /**规格参数组id查询其规则参数信息
     * 使用包装类的好处  还有其他的参数
     * 根据
     * @param gid
     * @return
     */
    @GetMapping("params")
    public ResponseEntity<List<SpecParam>> queryParams(
            @RequestParam(value = "gid",required = false) Long gid,
            @RequestParam(value = "cid",required = false) Long cid,
            @RequestParam(value = "generic",required = false) Boolean generic,
            @RequestParam(value = "searching",required = false) Boolean searching


    ){

        List<SpecParam> params = this.specificationService.queryParams(gid,cid,generic,searching);

        if(CollectionUtils.isEmpty(params)){
            return ResponseEntity.notFound().build();
        }

        return  ResponseEntity.ok(params);
    }

    /**
     * 根据cid  查询到规格参数组及对应的规格参数
     * @param cid
     * @return
     */
    @GetMapping("group/param/{cid}")
    public ResponseEntity<List<SpecGroup>> queryGroupWithParamByCid(@PathVariable("cid") Long cid){

        List<SpecGroup> groups = this.specificationService.queryGroupWithParamByCid(cid);

        if(CollectionUtils.isEmpty(groups)){
            return ResponseEntity.notFound().build();
        }

        return  ResponseEntity.ok(groups);
    }

}
