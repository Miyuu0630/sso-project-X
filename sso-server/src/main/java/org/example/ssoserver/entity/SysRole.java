package org.example.ssoserver.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDateTime;

/**
 * 角色信息实体类
 * 对应数据库表：sys_role
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("sys_role")
public class SysRole {
    
    /**
     * 角色ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;
    
    /**
     * 角色名称
     */
    @TableField("role_name")
    private String roleName;
    
    /**
     * 角色权限字符串
     */
    @TableField("role_key")
    private String roleKey;
    
    /**
     * 显示顺序
     */
    @TableField("role_sort")
    private Integer roleSort;
    
    /**
     * 数据范围：1-全部，2-自定义，3-本部门，4-本部门及以下，5-仅本人
     */
    @TableField("data_scope")
    private String dataScope;
    
    /**
     * 菜单树选择项是否关联显示
     */
    @TableField("menu_check_strictly")
    private Integer menuCheckStrictly;
    
    /**
     * 部门树选择项是否关联显示
     */
    @TableField("dept_check_strictly")
    private Integer deptCheckStrictly;
    
    /**
     * 状态：0-停用，1-正常
     */
    @TableField("status")
    private String status;
    
    /**
     * 创建时间
     */
    @TableField(value = "create_time", fill = FieldFill.INSERT)
    private LocalDateTime createTime;
    
    /**
     * 更新时间
     */
    @TableField(value = "update_time", fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
    
    /**
     * 创建人
     */
    @TableField(value = "create_by", fill = FieldFill.INSERT)
    private Long createBy;
    
    /**
     * 更新人
     */
    @TableField(value = "update_by", fill = FieldFill.INSERT_UPDATE)
    private Long updateBy;
    
    /**
     * 备注
     */
    @TableField("remark")
    private String remark;
    
    // ========================================
    // 业务方法
    // ========================================
    
    /**
     * 是否启用
     */
    public boolean isEnabled() {
        return "1".equals(status);
    }
    
    /**
     * 是否为管理员角色
     */
    public boolean isAdmin() {
        return "ADMIN".equals(roleKey);
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
