package com.atguigu.gulimall.search.service;

import com.atguigu.gulimall.search.vo.SearchParamVo;
import com.atguigu.gulimall.search.vo.SearchResult;

public interface MallSearchService {
    /**
     * 检索结果
     * @param paramVo
     * @return
     */
    SearchResult search(SearchParamVo paramVo);
}
