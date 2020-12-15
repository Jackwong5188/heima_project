package com.changgou.search.service.impl;

import com.alibaba.fastjson.JSON;
import com.changgou.search.pojo.SkuInfo;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.common.text.Text;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.aggregations.Aggregations;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightField;
import org.springframework.data.domain.Pageable;
import org.springframework.data.elasticsearch.core.SearchResultMapper;
import org.springframework.data.elasticsearch.core.aggregation.AggregatedPage;
import org.springframework.data.elasticsearch.core.aggregation.impl.AggregatedPageImpl;

import java.util.ArrayList;
import java.util.Map;

public class SearchResultMapperImpl implements SearchResultMapper {
    /**
     * @param response 搜索得到响应的结果
     * @param clazz    泛型
     * @param pageable 分页的数据
     * @param <T>
     * @return
     */
    @Override
    public <T> AggregatedPage<T> mapResults(SearchResponse response, Class<T> clazz, Pageable pageable) {
        //1.获取当前页的记录数
        ArrayList<T> content = new ArrayList<>();
        SearchHits hits = response.getHits();
        if (hits == null || hits.getTotalHits() <= 0) {
            //空的数据
            return new AggregatedPageImpl<T>(content);
        }
        for (SearchHit hit : hits) {
            String sourceAsString = hit.getSourceAsString(); //获取没有高亮的数据
            SkuInfo skuInfo = JSON.parseObject(sourceAsString, SkuInfo.class);  //转换成 对象skuInfo

            Map<String, HighlightField> highlightFields = hit.getHighlightFields(); //高亮数据
            //key就是 高亮的字段, value 高亮的值对象
            HighlightField highlightField = highlightFields.get("name");
            if (highlightField != null) {  // 如: TCL 测试掀暮涩  <em style=\"color:red\">小影院</em>  20英寸  170
                StringBuffer sb = new StringBuffer();
                for (Text text : highlightField.getFragments()) {
                    sb.append(text.toString());  //该数据就是高亮的数据
                }
                skuInfo.setName(sb.toString());  //已经被替换了
            }
            content.add((T) skuInfo); //已经是高亮的数据了
        }
        //2.获取分页的数据 有 了 不需要获取了

        //3.获取总记录数
        long totalHits = hits.getTotalHits();

        //4.获取聚合函数的所有值
        Aggregations aggregations = response.getAggregations();
        //5.获取游标的ID
        String scrollId = response.getScrollId();
        return new AggregatedPageImpl<T>(content,pageable,totalHits,aggregations,scrollId);
    }
}
