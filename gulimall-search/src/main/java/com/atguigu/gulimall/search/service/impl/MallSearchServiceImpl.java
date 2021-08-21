package com.atguigu.gulimall.search.service.impl;

import com.atguigu.gulimall.search.config.GulimallElasticSearchConfig;
import com.atguigu.gulimall.search.constant.EsConstant;
import com.atguigu.gulimall.search.service.MallSearchService;
import com.atguigu.gulimall.search.vo.SearchParamVo;
import com.atguigu.gulimall.search.vo.SearchResult;
import org.apache.lucene.search.join.ScoreMode;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.NestedQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.index.query.RangeQueryBuilder;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightBuilder;
import org.elasticsearch.search.sort.SortOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.io.IOException;

@Service
public class MallSearchServiceImpl implements MallSearchService {
    @Autowired
    private RestHighLevelClient client;

    @Override
    public SearchResult search(SearchParamVo paramVo) {
        //1.动态构建出查询需要的DSL语句
        SearchResult result = null;
        //准备检索请求
        SearchRequest searchRequest = buildSearchRequest(paramVo);


        try {
            //执行检索请求
            SearchResponse response = client.search(searchRequest, GulimallElasticSearchConfig.COMMON_OPTIONS);

            //分析封装响应数据
            result = buildSearchResult(response);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return result;
    }

    /**
     * 准备检索请求:
     * 模糊匹配，过滤(按照属性，分类，品牌，价格区间，库存),排序，分类，高亮，聚合分析
     *
     * @return
     */
    private SearchRequest buildSearchRequest(SearchParamVo paramVo) {
        //构建DSL语句
        SearchSourceBuilder builder = new SearchSourceBuilder();
        //查询:模糊匹配，过滤(按照属性，分类，品牌，价格区间，库存)
        //1.构建bool - query
        BoolQueryBuilder boolQuery = QueryBuilders.boolQuery();
        //1.1 must - 模糊匹配
        if (!StringUtils.isEmpty(paramVo.getKeyWord())) {
            boolQuery.must(QueryBuilders.matchQuery("skuTitle", paramVo.getKeyWord()));
        }
        //1.2 bool - fitler 按照三级分类id查询
        if (paramVo.getCatalog3Id() != null) {
            boolQuery.filter(QueryBuilders.termQuery("catalogId", paramVo.getCatalog3Id()));
        }
        //1.2 bool - filter 按照品牌id查询
        if (paramVo.getBrandId() != null && paramVo.getBrandId().size() > 0) {
            boolQuery.filter(QueryBuilders.termsQuery("brandId", paramVo.getBrandId()));
        }
        //1.2 bool - filter 按照指定属性进行查询，嵌入式查询,ScoreMode相关性得分
        if (paramVo.getAttrs() != null && paramVo.getAttrs().size() > 0) {
            for (String attrStr : paramVo.getAttrs()) {
                //attrs=1_5寸:8寸&attrs=2_16G:8G
                BoolQueryBuilder nestedBoolQuery = QueryBuilders.boolQuery();
                String[] s = attrStr.split("_");
                String attrId = s[0];//属性id
                String[] attrValues = s[1].split(":");//检索的属性值
                nestedBoolQuery.must(QueryBuilders.termQuery("attrs.attrId", attrId));
                nestedBoolQuery.must(QueryBuilders.termsQuery("attrs.attrValue", attrValues));
                //每一个必须得生成一个nested查询
                NestedQueryBuilder nestedQuery = QueryBuilders.nestedQuery("attrs", nestedBoolQuery, ScoreMode.None);
                boolQuery.filter(nestedQuery);
            }
        }
        //1.2 bool - filter 按照库存进行查询 todo
        builder.query(QueryBuilders.termsQuery("hasStock", paramVo.getHasStock() == 1));
        //1.2 bool - filter 按照价格区间
        if (!StringUtils.isEmpty(paramVo.getSkuPrice())) {
            //1_500/_500/500_
            RangeQueryBuilder rangeQuery = QueryBuilders.rangeQuery("skuPrice");
            String[] s = paramVo.getSkuPrice().split("_");
            if (s.length == 2) {
                //gte大于等于，lte小于等于，gt大于，lt小于
                //区间
                rangeQuery.gte(s[0]).lte(s[1]);
            } else if (s.length == 1) {
                //大于
                if (paramVo.getSkuPrice().startsWith("_")) {
                    rangeQuery.lte(s[0]);
                }
                //小于
                if (paramVo.getSkuPrice().endsWith("_")) {
                    rangeQuery.gte(s[0]);
                }
            }
            boolQuery.filter(rangeQuery);
        }
        builder.query(boolQuery);
        //排序，分类，高亮
        //2.1 排序
        if (!StringUtils.isEmpty(paramVo.getSort())) {
            //sort = skuPrice_asc/desc
            String sort = paramVo.getSort();
            String[] s = sort.split("_");
            SortOrder order = s[1].equalsIgnoreCase("asc") ? SortOrder.ASC : SortOrder.DESC;
            builder.sort(s[0], order);
        }
        //2.2 分页
        //from = (pageNum-1)*pageSize
        builder.from((paramVo.getPageNum()-1)*EsConstant.PRODUCT_PAGESIZE);
        builder.size(EsConstant.PRODUCT_PAGESIZE);
        //2.3 高亮
        if(!StringUtils.isEmpty(paramVo.getKeyWord())){
            HighlightBuilder highlightBuilder = new HighlightBuilder();
            highlightBuilder.field("skuTitle");
            highlightBuilder.preTags("<b style='color:red'>");
            highlightBuilder.postTags("</b>");
            builder.highlighter(highlightBuilder);
        }

        //聚合分析

        String s = builder.toString();
        System.out.println("DSL:"+s);
        SearchRequest searchRequest = new SearchRequest(new String[]{EsConstant.PRODUCT_INDEX}, builder);
        return searchRequest;
    }

    /**
     * 分析封装检索结果
     *
     * @return
     */
    private SearchResult buildSearchResult(SearchResponse response) {

        return null;
    }
}
