package org.example.ssoserver.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import lombok.EqualsAndHashCode;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 权限实体类
 */
@Data
@EqualsAndHashCode(callSuper = false)
public class SysPermission {
    
    /**
     * 权限ID
     */
    private Long id;
    
    /**
     * 权限编码
     */
    @NotBlank(message = "权限编码不能为空")
    @Size(max = 100, message = "权限编码长度不能超过100个字符")
    private String permissionCode;
    
    /**
     * 权限名称
     */
    @NotBlank(message = "权限名称不能为空")
    @Size(max = 50, message = "权限名称长度不能超过50个字符")
    private String permissionName;
    
    /**
     * 权限类型:1-菜单,2-按钮,3-接口
     */
    private Integer permissionType;
    
    /**
     * 父权限ID
     */
    private Long parentId;
    
    /**
     * 路径
     */
    @Size(max = 200, message = "路径长度不能超过200个字符")
    private String path;
    
    /**
     * 组件
     */
    @Size(max = 200, message = "组件长度不能超过200个字符")
    private String component;
    
    /**
     * 图标
     */
    @Size(max = 50, message = "图标长度不能超过50个字符")
    private String icon;
    
    /**
     * 排序
     */
    private Integer sortOrder;
    
    /**
     * 状态:0-禁用,1-启用
     */
    private Integer status;
    
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
     * 子权限列表(非数据库字段)
     */
    private List<SysPermission> children;
    
    /**
     * 判断权限是否启用
     */
    public boolean isEnabled() {
        return status != null && status == 1;
    }
    
    /**
     * 获取权限类型描述
     */
    public String getPermissionTypeDesc() {
        if (permissionType == null) {
            return "未知";
        }
        switch (permissionType) {
            case 1:
                return "菜单";
            case 2:
                return "按钮";
            case 3:
                return "接口";
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
    
    /**
     * 判断是否为根权限
     */
    public boolean isRoot() {
        return parentId == null || parentId == 0;
    }
}
