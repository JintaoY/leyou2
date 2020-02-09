package com.leyou.search.pojo;

import java.util.Map;

public class SearchRequest {

    private String key; //搜素条件

    private Integer page;//当前页

    private String sortBy; //排序字段

    private Boolean descending; //是否排序

    private Map<String,Object> filter; //删选的字段

    private static final Integer DEFAULT_PAGE = 1; //默认的页码
    private static final Integer DEFAULT_SIZE = 20; //每个几个，不从页面接收，而是固定大小

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public Integer getPage() {
        if(page == null){
            return DEFAULT_PAGE;
        }
        //两个取最大，确保最小为1
        return Math.max(DEFAULT_PAGE,page);
    }

    public void setPage(Integer page) {
        this.page = page;
    }

    public Integer getSize() {
        return DEFAULT_SIZE;
    }

    public String getSortBy() {
        return sortBy;
    }

    public void setSortBy(String sortBy) {
        this.sortBy = sortBy;
    }

    public Boolean getDescending() {
        return descending;
    }

    public void setDescending(Boolean descending) {
        this.descending = descending;
    }

    public Map<String, Object> getFilter() {
        return filter;
    }

    public void setFilter(Map<String, Object> filter) {
        this.filter = filter;
    }
}
