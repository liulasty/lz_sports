package com.lz.common.result;

import java.io.Serializable;
import java.util.List;

/**
 * Pagination Result
 */
public class PageResult implements Serializable {
    private long total;
    private List<?> records;

    public PageResult() {
    }

    public PageResult(long total, List<?> records) {
        this.total = total;
        this.records = records;
    }

    public long getTotal() {
        return total;
    }

    public void setTotal(long total) {
        this.total = total;
    }

    public List<?> getRecords() {
        return records;
    }

    public void setRecords(List<?> records) {
        this.records = records;
    }
}
