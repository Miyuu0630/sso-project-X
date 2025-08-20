package org.example.common.entity;

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
     * 昵称
     */
    @Size(max = 100, message = "昵称长度不能超过100个字符")
    private String nickname;

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
     * 状态:0-禁用,1-正常
     */
    private String status;

    /**
     * 用户类型:normal-普通用户,enterprise-企业用户,airline-航司用户
     */
    private String userType;
    
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
     * 登录次数
     */
    private Integer loginCount;
    
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
    @Size(max = 500, message = "备注长度不能超过500个字符")
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
        return status != null && "1".equals(status);
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
            case "normal":
                return "普通用户";
            case "enterprise":
                return "企业用户";
            case "airline":
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
        return "1".equals(status) ? "正常" : "禁用";
    }
}
