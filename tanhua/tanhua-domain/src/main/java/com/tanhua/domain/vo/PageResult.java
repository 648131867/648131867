package com.tanhua.domain.vo;
import lombok.Data;
import java.io.Serializable;
import java.util.Collections;
import java.util.List;
@Data
public class PageResult<T> implements Serializable {
    private Long counts; // 总记录数
    private Long pagesize;// 每页大小
    private Long pages;// 总页数
    private Long page;// 页码
    private List<T> items = Collections.emptyList();
    /**
     * @param page 当前页码
     * @param pageSize 每页大小
     * @param items 分页结果集
     * @param total 总记录数
     * @return
     */
    public static PageResult pageResult(Long page, Long pageSize, List items, Long total){
        PageResult pageResult = new PageResult();
        pageResult.setPage(page);
        pageResult.setPagesize(pageSize);
        pageResult.setItems(items);
        // 计算总页数
        long pages = total / pageSize + (total % pageSize > 0 ? 1 : 0);
        pageResult.setPages(pages);
        pageResult.setCounts(total);
        return pageResult;
    }
}