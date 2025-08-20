package org.example.common.result;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

/**
 * 分页响应结果类
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class PageResult<T> extends Result<List<T>> {
    
    /**
     * 当前页码
     */
    private Long current;
    
    /**
     * 每页大小
     */
    private Long size;
    
    /**
     * 总记录数
     */
    private Long total;
    
    /**
     * 总页数
     */
    private Long pages;
    
    public PageResult() {
        super();
    }
    
    public PageResult(Long current, Long size, Long total, List<T> data) {
        super(ResultCode.SUCCESS.getCode(), ResultCode.SUCCESS.getMessage(), data);
        this.current = current;
        this.size = size;
        this.total = total;
        this.pages = (total + size - 1) / size; // 计算总页数
    }
    
    /**
     * 成功分页响应
     */
    public static <T> PageResult<T> success(Long current, Long size, Long total, List<T> data) {
        return new PageResult<>(current, size, total, data);
    }
    
    /**
     * 空分页响应
     */
    public static <T> PageResult<T> empty(Long current, Long size) {
        return new PageResult<>(current, size, 0L, List.of());
    }
    
    /**
     * 判断是否有下一页
     */
    public boolean hasNext() {
        return current < pages;
    }
    
    /**
     * 判断是否有上一页
     */
    public boolean hasPrevious() {
        return current > 1;
    }
    
    /**
     * 判断是否为空
     */
    public boolean isEmpty() {
        return total == 0 || this.getData() == null || this.getData().isEmpty();
    }
}
