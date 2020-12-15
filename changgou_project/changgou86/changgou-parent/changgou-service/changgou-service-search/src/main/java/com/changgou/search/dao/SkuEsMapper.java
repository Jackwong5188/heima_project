package com.changgou.search.dao;

import com.changgou.search.pojo.SkuInfo;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

/***
 * 操作ES的接口
 * <SkuInfo,Long>
 *     1.指定要操作的POJO的数据类型
 *     2.要操作的POJO的文档的唯一标识的数据类型
 * @author ljh
 * @packagename com.changgou.search.dao
 * @version 1.0
 * @date 2020/3/23
 */
public interface SkuEsMapper extends ElasticsearchRepository<SkuInfo,Long> {
}
