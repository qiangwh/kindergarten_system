package com.kindergarten.system.common.result;

import com.baomidou.mybatisplus.core.metadata.IPage;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

@Data
public class PageResult<T> implements Serializable {

    private static final long serialVersionUID = 1L;

    private List<T> records;
    private Long page;
    private Long pageSize;
    private Long total;
    private Long pages;

    public PageResult() {}

    public PageResult(List<T> records, Long page, Long pageSize, Long total, Long pages) {
        this.records = records;
        this.page = page;
        this.pageSize = pageSize;
        this.total = total;
        this.pages = pages;
    }

    public static <T> PageResult<T> of(IPage<T> page) {
        return new PageResult<>(
                page.getRecords(),
                page.getCurrent(),
                page.getSize(),
                page.getTotal(),
                page.getPages()
        );
    }

    public static <T> PageResult<T> of(List<T> records, Long page, Long pageSize, Long total) {
        long pages = total % pageSize == 0 ? total / pageSize : total / pageSize + 1;
        return new PageResult<>(records, page, pageSize, total, pages);
    }
}
