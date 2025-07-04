package com.pdsu.charge_palteform.entity.dto;

import lombok.Data;

import java.util.List;

@Data
public class PageResponse<T> {
    private List<T> records;
    private Long total;
    private Integer pageNum;
    private Integer pageSize;
    private Integer totalPages;

    public static <T> PageResponse<T> of(List<T> records, Long total, Integer pageNum, Integer pageSize) {
        PageResponse<T> response = new PageResponse<>();
        response.setRecords(records);
        response.setTotal(total);
        response.setPageNum(pageNum);
        response.setPageSize(pageSize);
        response.setTotalPages((int) Math.ceil((double) total / pageSize));
        return response;
    }
}
