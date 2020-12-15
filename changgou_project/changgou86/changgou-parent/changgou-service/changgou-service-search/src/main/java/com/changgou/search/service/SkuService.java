package com.changgou.search.service;

import java.util.Map;

public interface SkuService {
    /***
     * 导入SKU数据
     */
    void importSku();

    /***
     * 关键字搜索
     * @param searchMap
     * @return
     */
    Map search(Map<String, String> searchMap);
}
