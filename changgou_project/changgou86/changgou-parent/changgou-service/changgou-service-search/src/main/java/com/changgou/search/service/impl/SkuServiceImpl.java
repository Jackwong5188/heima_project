package com.changgou.search.service.impl;

import com.alibaba.fastjson.JSON;
import com.changgou.goods.feign.SkuFeign;
import com.changgou.goods.pojo.Sku;
import com.changgou.search.dao.SkuEsMapper;
import com.changgou.search.pojo.SkuInfo;
import com.changgou.search.service.SkuService;
import entity.Result;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.bucket.terms.StringTerms;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightBuilder;
import org.elasticsearch.search.sort.SortBuilders;
import org.elasticsearch.search.sort.SortOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.elasticsearch.core.ElasticsearchTemplate;
import org.springframework.data.elasticsearch.core.aggregation.AggregatedPage;
import org.springframework.data.elasticsearch.core.query.NativeSearchQuery;
import org.springframework.data.elasticsearch.core.query.NativeSearchQueryBuilder;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import javax.swing.*;
import java.util.*;

@Service
public class SkuServiceImpl implements SkuService {
    @Autowired
    private SkuFeign skuFeign;

    @Autowired
    private SkuEsMapper skuMapper;

    @Autowired
    private ElasticsearchTemplate esTemplate;

    /**
     * 导入sku数据到es
     */
    @Override
    public void importSku() {
        //1.调用changgou-service-goods微服务
        Result<List<Sku>> skuListResult = skuFeign.findByStatus("1");
        List<Sku> data = skuListResult.getData();//所有的sku的数据
        //2,将数据转成 List<SkuInfo> 是ES的数据
        List<SkuInfo> skuInfos = JSON.parseArray(JSON.toJSONString(data), SkuInfo.class);

        for (SkuInfo skuInfo : skuInfos) {
            String spec = skuInfo.getSpec();//{"电视音响效果":"环绕","电视屏幕尺寸":"20英寸","尺码":"170"}
            Map<String, Object> specMap = JSON.parseObject(spec, Map.class);
            skuInfo.setSpecMap(specMap);
        }
        //3.将数据导入到ES中 慢 墙裂
        skuMapper.saveAll(skuInfos);
    }

    //关键字搜索
    @Override
    public Map search(Map<String, String> searchMap) {
        //1.获取关键字的值
        String keywords = searchMap.get("keywords");
        if (StringUtils.isEmpty(keywords)) {
            keywords = "华为";//赋值给一个默认的值
        }

        //2.创建查询对象 的构建对象
        NativeSearchQueryBuilder nativeSearchQueryBuilder = new NativeSearchQueryBuilder();

        //3.设置查询的条件
        //设置分组条件  商品分类   参数1 指定别名  参数2 指定要分组的字段名 参数3 指定分组的数据大小，默认是10
        nativeSearchQueryBuilder.addAggregation(AggregationBuilders.terms("skuCategorygroup").field("categoryName").size(50));
        //设置分组条件  商品品牌
        nativeSearchQueryBuilder.addAggregation(AggregationBuilders.terms("skuBrandgroup").field("brandName").size(50));
        //设置分组条件  商品的规格
        nativeSearchQueryBuilder.addAggregation(AggregationBuilders.terms("skuSpecgroup").field("spec.keyword").size(100));

        //设置高亮条件
        nativeSearchQueryBuilder.withHighlightFields(new HighlightBuilder.Field("name"));  //高亮的字段
        nativeSearchQueryBuilder.withHighlightBuilder(new HighlightBuilder().preTags("<em style=\"color:red\">").postTags("</em>")); //高亮的前缀和后缀

        //关键字查询
        nativeSearchQueryBuilder.withQuery(QueryBuilders.matchQuery("name",keywords));

        //多条件组合(过滤)查询
        BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery();
        //品牌条件
        if(!StringUtils.isEmpty(searchMap.get("brand"))){
            boolQueryBuilder.filter(QueryBuilders.termQuery("brandName",searchMap.get("brand")));
        }
        //分类条件
        if(!StringUtils.isEmpty(searchMap.get("category"))){
            boolQueryBuilder.filter(QueryBuilders.termQuery("categoryName",searchMap.get("category")));
        }
        //规格条件
        for (String key : searchMap.keySet()) {  //键的集合：分类  品牌  spec_网络制式  spec_显示尺寸  ...
            if(key.startsWith("spec_")){  //与前端约定好，以 spec_规格名=规格选项值 的方式传递过来
                //真实数据在spechMap.规格名字.keyword中
                boolQueryBuilder.filter(QueryBuilders.termQuery("specMap."+ key.substring(5) + ".keyword",searchMap.get(key)));
            }
        }
        //价格条件
        String price = searchMap.get("price");  // 500-1000   3000-*
        if(!StringUtils.isEmpty(price)){
            String[] split = price.split("-");
            if(split[1].equals("*")){
                boolQueryBuilder.filter(QueryBuilders.rangeQuery("price").gte(split[0])); // ≥3000
            }else {
                boolQueryBuilder.filter(QueryBuilders.rangeQuery("price").gte(split[0]).lte(split[1])); // ≥500 且 ≤1000
            }
        }

        //构建过滤查询
        nativeSearchQueryBuilder.withFilter(boolQueryBuilder);

        //构建分页查询
        String pageNumStr = searchMap.get("pageNum"); //用户点击的页码, 如：1  2 3
        Integer pageNum=1;  //默认值, 第一页
        Integer pageSize=40;  //每页展示数量
        if(!StringUtils.isEmpty(pageNumStr)){
            pageNum = Integer.parseInt(pageNumStr);
        }
        //参数1 指定的是页码的值 0 表示第一页, 参数2 指定每页显示的行
        Pageable pageable = PageRequest.of(pageNum - 1, pageSize);
        nativeSearchQueryBuilder.withPageable(pageable);

        //构建排序查询
        String sortField = searchMap.get("sortField"); //字符串 price
        String sortRule = searchMap.get("sortRule"); //DESC 一定是大写
        if(!StringUtils.isEmpty(sortField) && !StringUtils.isEmpty(sortRule)){
            nativeSearchQueryBuilder.withSort(SortBuilders.fieldSort(sortField).order(SortOrder.valueOf(sortRule)));
        }

        //4.构建查询对象
        NativeSearchQuery query = nativeSearchQueryBuilder.build();

        //5.执行查询
        AggregatedPage<SkuInfo> skuInfos = esTemplate.queryForPage(query, SkuInfo.class, new SearchResultMapperImpl());

        //获取分组结果  商品分类的分组
        StringTerms stringTermsCategory = (StringTerms) skuInfos.getAggregation("skuCategorygroup");
        List<String> categoryList = getStringsCategoryList(stringTermsCategory);

        //获取分组结果  商品品牌的分组
        StringTerms stringTermsBrand = (StringTerms) skuInfos.getAggregation("skuBrandgroup");
        List<String> brandList = getStringsBrandList(stringTermsBrand);

        //获取分组结果  商品规格的分组
        StringTerms stringTermsSpec = (StringTerms) skuInfos.getAggregation("skuSpecgroup");
        Map<String, Set<String>> specMap = getStringsSpecMap(stringTermsSpec);

        //6.返回结果
        Map resultMap = new HashMap<>();
        resultMap.put("rows",skuInfos.getContent()); //当前页的集合
        resultMap.put("total", skuInfos.getTotalElements()); //总记录数
        resultMap.put("totalPages", skuInfos.getTotalPages()); //总页数
        //设置商品分类的数据
        resultMap.put("categoryList", categoryList);
        //设置品牌列表的数据
        resultMap.put("brandList", brandList);
        //设置规格列表的数据
        resultMap.put("specMap", specMap);
        //用户点击的页码
        resultMap.put("pageNum", pageNum);
        //每页展示数量
        resultMap.put("pageSize", pageSize);

        return resultMap;
    }

    //规格列表
    private Map<String, Set<String>> getStringsSpecMap(StringTerms stringTermsSpec) {
        Map<String, Set<String>> specMap = new HashMap<>(); // key: 规格名  value： 规格的选项值的【集合】不重复
        Set<String> values = new HashSet<>();  // values: 规格的选项值的【集合】不重复
        if (stringTermsSpec != null) {
            for (StringTerms.Bucket bucket : stringTermsSpec.getBuckets()) {
                //{"手机屏幕尺寸":"5.5寸","网络":"电信4G","颜色":"白","测试":"s11","机身内存":"128G","存储":"16G","像素":"300万像素"}
                // {"手机屏幕尺寸":"5.6寸","网络":"电信4G","颜色":"白","测试":"s11","机身内存":"128G","存储":"16G","像素":"800万像素"}
                String keyAsString = bucket.getKeyAsString(); //分组的值
                //转换成MAP  有key 有value
                Map<String, String> map = JSON.parseObject(keyAsString, Map.class);
                for (Map.Entry<String, String> entry : map.entrySet()) {
                    String key = entry.getKey(); //手机屏幕尺寸
                    String value = entry.getValue(); //5.5寸
                    values = specMap.get(key);
                    if (values == null) {
                        values = new HashSet<String>();
                    }
                    values.add(value); //["5.5"]
                    specMap.put(key,values);
                }
            }
        }
        return specMap;
    }

    //品牌列表
    private List<String> getStringsBrandList(StringTerms stringTermsBrand) {
        List<String> brandList = new ArrayList<>();
        if (stringTermsBrand != null) {
            for (StringTerms.Bucket bucket : stringTermsBrand.getBuckets()) {
                String keyAsString = bucket.getKeyAsString(); //分组的值
                brandList.add(keyAsString);
            }
        }
        return brandList;
    }

    //商品分类
    private List<String> getStringsCategoryList(StringTerms stringTerms) {
        List<String> categoryList = new ArrayList<>();
        if (stringTerms != null) {
            for (StringTerms.Bucket bucket : stringTerms.getBuckets()) {
                String keyAsString = bucket.getKeyAsString(); //分组的值
                categoryList.add(keyAsString);
            }
        }
        return categoryList;
    }
}
