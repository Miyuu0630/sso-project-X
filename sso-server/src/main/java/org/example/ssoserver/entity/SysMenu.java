package org.example.ssoserver.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDateTime;

/**
 * 菜单权限实体类
 * 对应数据库表：sys_menu
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("sys_menu")
public class SysMenu {
    
    /**
     * 菜单ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;
    
    /**
     * 菜单名称
     */
    @TableField("menu_name")
    private String menuName;
    
    /**
     * 父菜单ID
     */
    @TableField("parent_id")
    private Long parentId;
    
    /**
     * 显示顺序
     */
    @TableField("order_num")
    private Integer orderNum;
    
    /**
     * 路由地址
     */
    @TableField("path")
    private String path;
    
    /**
     * 组件路径
     */
    @TableField("component")
    private String component;
    
    /**
     * 路由参数
     */
    @TableField("query_param")
    private String queryParam;
    
    /**
     * 是否为外链：0-是，1-否
     */
    @TableField("is_frame")
    private Integer isFrame;
    
    /**
     * 是否缓存：0-缓存，1-不缓存
     */
    @TableField("is_cache")
    private Integer isCache;
    
    /**
     * 菜单类型：M-目录，C-菜单，F-按钮
     */
    @TableField("menu_type")
    private String menuType;
    
    /**
     * 显示状态：0-隐藏，1-显示
     */
    @TableField("visible")
    private String visible;
    
    /**
     * 菜单状态：0-停用，1-正常
     */
    @TableField("status")
    private String status;
    
    /**
     * 权限标识
     */
    @TableField("perms")
    private String perms;
    
    /**
     * 菜单图标
     */
    @TableField("icon")
    private String icon;
    
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
     * 是否为目录
     */
    public boolean isDirectory() {
        return "M".equals(menuType);
    }
    
    /**
     * 是否为菜单
     */
    public boolean isMenu() {
        return "C".equals(menuType);
    }
    
    /**
     * 是否为按钮
     */
    public boolean isButton() {
        return "F".equals(menuType);
    }
    
    /**
     * 是否为根菜单
     */
    public boolean isRoot() {
        return parentId == null || parentId == 0L;
    }
    
    /**
     * 是否启用
     */
    public boolean isEnabled() {
        return "1".equals(status);
    }
    
    /**
     * 是否显示
     */
    public boolean isVisible() {
        return "1".equals(visible);
    }
    
    /**
     * 是否为外链
     */
    public boolean isExternalLink() {
        return isFrame != null && isFrame == 0;
    }
    
    /**
     * 是否缓存
     */
    public boolean isCached() {
        return isCache != null && isCache == 0;
    }
}
