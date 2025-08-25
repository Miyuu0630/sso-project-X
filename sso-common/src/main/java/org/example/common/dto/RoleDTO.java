package org.example.common.dto;

import lombok.Data;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 角色信息DTO
 * 用于前后端数据传输和模块间数据交换
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class RoleDTO {
    
    /**
     * 角色ID
     */
    private Long id;
    
    /**
     * 角色名称
     */
    private String roleName;
    
    /**
     * 角色权限字符串
     */
    private String roleKey;
    
    /**
     * 显示顺序
     */
    private Integer roleSort;
    
    /**
     * 数据范围：1-全部，2-自定义，3-本部门，4-本部门及以下，5-仅本人
     */
    private String dataScope;
    
    /**
     * 数据范围描述
     */
    private String dataScopeDesc;
    
    /**
     * 菜单树选择项是否关联显示
     */
    private Boolean menuCheckStrictly;
    
    /**
     * 部门树选择项是否关联显示
     */
    private Boolean deptCheckStrictly;
    
    /**
     * 状态：0-停用，1-正常
     */
    private String status;
    
    /**
     * 状态描述
     */
    private String statusDesc;
    
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
     * 角色关联的菜单权限列表
     */
    private List<MenuDTO> menus;
    
    /**
     * 角色关联的菜单ID列表
     */
    private List<Long> menuIds;
    
    /**
     * 角色关联的用户数量
     */
    private Integer userCount;
    
    /**
     * 权限标识列表
     */
    private List<String> permissions;
    
    // ========================================
    // 业务方法
    // ========================================
    
    /**
     * 是否为管理员角色
     */
    public boolean isAdmin() {
        return "ADMIN".equals(roleKey);
    }
    
    /**
     * 是否启用
     */
    public boolean isEnabled() {
        return "1".equals(status);
    }
    
    /**
     * 是否为系统内置角色
     */
    public boolean isBuiltIn() {
        return roleKey != null && (
            roleKey.equals("ADMIN") || 
            roleKey.equals("PERSONAL_USER") || 
            roleKey.equals("ENTERPRISE_USER") || 
            roleKey.equals("AIRLINE_USER")
        );
    }
    
    /**
     * 获取数据范围描述
     */
    public String getDataScopeDescription() {
        if (dataScope == null) {
            return "未设置";
        }
        switch (dataScope) {
            case "1":
                return "全部数据权限";
            case "2":
                return "自定数据权限";
            case "3":
                return "本部门数据权限";
            case "4":
                return "本部门及以下数据权限";
            case "5":
                return "仅本人数据权限";
            default:
                return "未知数据权限";
        }
    }
}
