package org.example.ssoserver.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

/**
 * 角色菜单关联实体类
 * 对应数据库表：sys_role_menu
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("sys_role_menu")
public class SysRoleMenu {
    
    /**
     * 角色ID
     */
    @TableField("role_id")
    private Long roleId;
    
    /**
     * 菜单ID
     */
    @TableField("menu_id")
    private Long menuId;
}
