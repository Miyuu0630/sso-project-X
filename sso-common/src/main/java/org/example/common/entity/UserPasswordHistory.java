package org.example.common.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import lombok.EqualsAndHashCode;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.time.LocalDateTime;

/**
 * 用户密码历史实体类
 */
@Data
@EqualsAndHashCode(callSuper = false)
public class UserPasswordHistory {
    
    /**
     * ID
     */
    private Long id;
    
    /**
     * 用户ID
     */
    private Long userId;
    
    /**
     * 密码哈希值
     */
    @NotBlank(message = "密码哈希值不能为空")
    @Size(max = 255, message = "密码哈希值长度不能超过255个字符")
    private String passwordHash;
    
    /**
     * 创建时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;
    
    /**
     * 默认构造函数
     */
    public UserPasswordHistory() {}
    
    /**
     * 构造函数
     */
    public UserPasswordHistory(Long userId, String passwordHash) {
        this.userId = userId;
        this.passwordHash = passwordHash;
        this.createTime = LocalDateTime.now();
    }
}
