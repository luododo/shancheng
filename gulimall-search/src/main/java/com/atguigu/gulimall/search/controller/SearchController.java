package com.atguigu.gulimall.search.controller;

import com.atguigu.gulimall.search.service.MallSearchService;
import com.atguigu.gulimall.search.vo.SearchParamVo;
import com.atguigu.gulimall.search.vo.SearchResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class SearchController {

    @Autowired
    MallSearchService mallSearchService;


    @GetMapping("/list.html")
    public String lisgPage(SearchParamVo paramVo,Model model){
        //根据页面传递的数据查询参数，去es中检索商品
        SearchResult reslut = mallSearchService.search(paramVo);
        model.addAttribute("result",reslut);

        return "list";
    }
}
