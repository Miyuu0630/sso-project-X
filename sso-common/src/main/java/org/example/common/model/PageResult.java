package org.example.common.model;

import lombok.Data;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.List;
import java.util.Collections;

/**
 * 分页结果封装类
 * 统一的分页数据返回格式
 * 
 * @param <T> 数据类型
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class PageResult<T> {
    
    /**
     * 当前页码（从1开始）
     */
    private Integer pageNum;
    
    /**
     * 每页大小
     */
    private Integer pageSize;
    
    /**
     * 总记录数
     */
    private Long total;
    
    /**
     * 总页数
     */
    private Integer pages;
    
    /**
     * 当前页数据列表
     */
    private List<T> list;
    
    /**
     * 是否有上一页
     */
    private Boolean hasPrevious;
    
    /**
     * 是否有下一页
     */
    private Boolean hasNext;
    
    /**
     * 是否为第一页
     */
    private Boolean isFirst;
    
    /**
     * 是否为最后一页
     */
    private Boolean isLast;
    
    /**
     * 导航页码列表
     */
    private List<Integer> navigatePages;
    
    /**
     * 导航页码数量
     */
    private Integer navigatePageNums;
    
    // ========================================
    // 构造方法
    // ========================================
    
    /**
     * 构造分页结果
     * @param pageNum 当前页码
     * @param pageSize 每页大小
     * @param total 总记录数
     * @param list 数据列表
     */
    public PageResult(Integer pageNum, Integer pageSize, Long total, List<T> list) {
        this.pageNum = pageNum;
        this.pageSize = pageSize;
        this.total = total;
        this.list = list != null ? list : Collections.emptyList();
        
        // 计算总页数
        this.pages = (int) Math.ceil((double) total / pageSize);
        
        // 计算分页状态
        this.hasPrevious = pageNum > 1;
        this.hasNext = pageNum < pages;
        this.isFirst = pageNum == 1;
        this.isLast = pageNum.equals(pages) || pages == 0;
        
        // 生成导航页码
        this.navigatePageNums = 8; // 默认显示8个导航页码
        this.navigatePages = generateNavigatePages();
    }
    
    // ========================================
    // 静态工厂方法
    // ========================================
    
    /**
     * 创建空的分页结果
     */
    public static <T> PageResult<T> empty() {
        return new PageResult<>(1, 10, 0L, Collections.emptyList());
    }
    
    /**
     * 创建空的分页结果（指定页码和页大小）
     */
    public static <T> PageResult<T> empty(Integer pageNum, Integer pageSize) {
        return new PageResult<>(pageNum, pageSize, 0L, Collections.emptyList());
    }
    
    /**
     * 创建分页结果
     */
    public static <T> PageResult<T> of(Integer pageNum, Integer pageSize, Long total, List<T> list) {
        return new PageResult<>(pageNum, pageSize, total, list);
    }
    
    /**
     * 创建分页结果（从MyBatis Plus的Page对象）
     */
    public static <T> PageResult<T> of(com.baomidou.mybatisplus.extension.plugins.pagination.Page<T> page) {
        return new PageResult<>(
            (int) page.getCurrent(),
            (int) page.getSize(),
            page.getTotal(),
            page.getRecords()
        );
    }
    
    /**
     * 创建单页结果（不分页）
     */
    public static <T> PageResult<T> single(List<T> list) {
        int size = list != null ? list.size() : 0;
        return new PageResult<>(1, size, (long) size, list);
    }
    
    // ========================================
    // 业务方法
    // ========================================
    
    /**
     * 是否有数据
     */
    public boolean hasData() {
        return list != null && !list.isEmpty();
    }
    
    /**
     * 获取当前页数据数量
     */
    public int getCurrentPageSize() {
        return list != null ? list.size() : 0;
    }
    
    /**
     * 获取起始记录号（从1开始）
     */
    public long getStartRow() {
        return (long) (pageNum - 1) * pageSize + 1;
    }
    
    /**
     * 获取结束记录号
     */
    public long getEndRow() {
        return Math.min(getStartRow() + getCurrentPageSize() - 1, total);
    }
    
    /**
     * 设置导航页码数量并重新生成导航页码
     */
    public PageResult<T> navigatePageNums(Integer navigatePageNums) {
        this.navigatePageNums = navigatePageNums;
        this.navigatePages = generateNavigatePages();
        return this;
    }
    
    /**
     * 生成导航页码列表
     */
    private List<Integer> generateNavigatePages() {
        if (pages == null || pages <= 0 || navigatePageNums == null || navigatePageNums <= 0) {
            return Collections.emptyList();
        }
        
        List<Integer> navigatePages = new java.util.ArrayList<>();
        
        // 计算导航页码的起始和结束位置
        int startNum = Math.max(1, pageNum - navigatePageNums / 2);
        int endNum = Math.min(pages, startNum + navigatePageNums - 1);
        
        // 如果结束页码不足，调整起始页码
        if (endNum - startNum + 1 < navigatePageNums) {
            startNum = Math.max(1, endNum - navigatePageNums + 1);
        }
        
        // 生成页码列表
        for (int i = startNum; i <= endNum; i++) {
            navigatePages.add(i);
        }
        
        return navigatePages;
    }
    
    /**
     * 转换数据类型
     */
    public <R> PageResult<R> map(java.util.function.Function<T, R> mapper) {
        List<R> mappedList = list != null ? 
            list.stream().map(mapper).collect(java.util.stream.Collectors.toList()) : 
            Collections.emptyList();
        
        return PageResult.<R>builder()
                .pageNum(pageNum)
                .pageSize(pageSize)
                .total(total)
                .pages(pages)
                .list(mappedList)
                .hasPrevious(hasPrevious)
                .hasNext(hasNext)
                .isFirst(isFirst)
                .isLast(isLast)
                .navigatePages(navigatePages)
                .navigatePageNums(navigatePageNums)
                .build();
    }
    
    /**
     * 获取分页信息摘要
     */
    public String getSummary() {
        if (total == 0) {
            return "暂无数据";
        }
        return String.format("第 %d 页，共 %d 页，总计 %d 条记录", pageNum, pages, total);
    }
    
    /**
     * 获取分页范围描述
     */
    public String getRangeDescription() {
        if (total == 0) {
            return "0 条记录";
        }
        return String.format("第 %d-%d 条，共 %d 条记录", getStartRow(), getEndRow(), total);
    }
}
