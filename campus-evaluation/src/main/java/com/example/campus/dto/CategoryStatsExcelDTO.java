package com.example.campus.dto;

import com.alibaba.excel.annotation.ExcelProperty;
import com.alibaba.excel.annotation.write.style.ColumnWidth;
import com.alibaba.excel.annotation.write.style.HeadRowHeight;
import lombok.Data;

@Data
@HeadRowHeight(20)
@ColumnWidth(25)
public class CategoryStatsExcelDTO {

    @ExcelProperty(value = "分类名称", index = 0)
    @ColumnWidth(30)
    private String categoryName;

    @ExcelProperty(value = "设施数量", index = 1)
    @ColumnWidth(15)
    private Integer facilityCount;

    @ExcelProperty(value = "评价数量", index = 2)
    @ColumnWidth(15)
    private Integer evaluationCount;

    @ExcelProperty(value = "平均评分", index = 3)
    @ColumnWidth(15)
    private Double avgRating;

    @ExcelProperty(value = "占比(%)", index = 4)
    @ColumnWidth(15)
    private Double percent;
}