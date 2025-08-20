package org.example.common.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import lombok.EqualsAndHashCode;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 角色实体类
 */
@Data
@EqualsAndHashCode(callSuper = false)
public class SysRole {
    
    /**
     * 角色ID
     */
    private Long id;
    
    /**
     * 角色权限字符串
     */
    @NotBlank(message = "角色权限字符串不能为空")
    @Size(max = 50, message = "角色权限字符串长度不能超过50个字符")
    private String roleKey;
    
    /**
     * 角色名称
     */
    @NotBlank(message = "角色名称不能为空")
    @Size(max = 50, message = "角色名称长度不能超过50个字符")
    private String roleName;
    
    /**
     * 显示顺序
     */
    private Integer roleSort;

    /**
     * 数据范围：1-全部，2-自定义，3-本部门，4-本部门及以下，5-仅本人
     */
    private String dataScope;

    /**
     * 菜单树选择项是否关联显示：0-否，1-是
     */
    private Integer menuCheckStrictly;

    /**
     * 部门树选择项是否关联显示：0-否，1-是
     */
    private Integer deptCheckStrictly;

    /**
     * 状态:0-停用,1-正常
     */
    private String status;
    
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
     * 角色菜单列表(非数据库字段)
     */
    private List<SysMenu> menus;
    
    /**
     * 菜单ID列表(非数据库字段)
     */
    private List<Long> menuIds;
    
    /**
     * 判断角色是否启用
     */
    public boolean isEnabled() {
        return status != null && "1".equals(status);
    }

    /**
     * 获取状态描述
     */
    public String getStatusDesc() {
        if (status == null) {
            return "未知";
        }
        return "1".equals(status) ? "正常" : "停用";
    }
}
