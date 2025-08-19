package org.example.ssoserver.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;
import lombok.EqualsAndHashCode;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 用户实体类
 */
@Data
@EqualsAndHashCode(callSuper = false)
public class SysUser {
    
    /**
     * 用户ID
     */
    private Long id;
    
    /**
     * 用户名
     */
    @NotBlank(message = "用户名不能为空")
    @Size(min = 3, max = 50, message = "用户名长度必须在3-50个字符之间")
    @Pattern(regexp = "^[a-zA-Z0-9_]+$", message = "用户名只能包含字母、数字和下划线")
    private String username;
    
    /**
     * 密码(BCrypt加密)
     */
    @JsonIgnore
    @NotBlank(message = "密码不能为空")
    @Size(min = 6, max = 20, message = "密码长度必须在6-20个字符之间")
    private String password;
    
    /**
     * 手机号
     */
    @Pattern(regexp = "^1[3-9]\\d{9}$", message = "手机号格式不正确")
    private String phone;
    
    /**
     * 邮箱
     */
    @Email(message = "邮箱格式不正确")
    private String email;
    
    /**
     * 真实姓名
     */
    @Size(max = 50, message = "真实姓名长度不能超过50个字符")
    private String realName;
    
    /**
     * 头像URL
     */
    private String avatar;
    
    /**
     * 性别:0-未知,1-男,2-女
     */
    private Integer gender;
    
    /**
     * 生日
     */
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate birthday;
    
    /**
     * 状态:0-禁用,1-启用
     */
    private Integer status;
    
    /**
     * 用户类型:1-个人用户,2-企业用户,3-航司用户
     */
    private Integer userType;
    
    /**
     * 最后登录时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime lastLoginTime;
    
    /**
     * 最后登录IP
     */
    private String lastLoginIp;
    
    /**
     * 创建时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;
    
    /**
     * 更新时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updateTime;
    
    /**
     * 创建人
     */
    private Long createBy;
    
    /**
     * 更新人
     */
    private Long updateBy;
    
    /**
     * 备注
     */
    private String remark;
    
    /**
     * 用户角色列表(非数据库字段)
     */
    private List<SysRole> roles;
    
    /**
     * 用户权限列表(非数据库字段)
     */
    private List<String> permissions;
    
    /**
     * 判断用户是否启用
     */
    public boolean isEnabled() {
        return status != null && status == 1;
    }
    
    /**
     * 判断是否为管理员
     */
    public boolean isAdmin() {
        return "admin".equals(username);
    }
    
    /**
     * 获取用户类型描述
     */
    public String getUserTypeDesc() {
        if (userType == null) {
            return "未知";
        }
        switch (userType) {
            case 1:
                return "个人用户";
            case 2:
                return "企业用户";
            case 3:
                return "航司用户";
            default:
                return "未知";
        }
    }
    
    /**
     * 获取性别描述
     */
    public String getGenderDesc() {
        if (gender == null) {
            return "未知";
        }
        switch (gender) {
            case 1:
                return "男";
            case 2:
                return "女";
            default:
                return "未知";
        }
    }
    
    /**
     * 获取状态描述
     */
    public String getStatusDesc() {
        if (status == null) {
            return "未知";
        }
        return status == 1 ? "启用" : "禁用";
    }
}
