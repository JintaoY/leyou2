package com.leyou.goods.controller;

import com.leyou.goods.service.GoodsHtmlService;
import com.leyou.goods.service.GoodsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.Map;

@Controller
public class GoodsController {

    @Autowired
    private GoodsService goodsService;

    @Autowired
    private GoodsHtmlService goodsHtmlService;

    /**
     * 跳转到详情页上去
     * 根据 spuId获得详情页数据
     * @param id
     * @param model
     * @return
     */
    @GetMapping("item/{id}.html")
    public String toItemPage(@PathVariable("id") Long id, Model model){

        Map<String, Object> map = this.goodsService.loadData(id);
        //之前是  addAttribute
        model.addAllAttributes(map);   //然后会在Thymeleaf显示  应该是转发

        //创建静态文件
        //this.goodsHtmlService.createHtml(id);

        //新建一个线程处理页面静态化
        this.goodsHtmlService.asyncExcute(id);

        return "item";
    }
}
