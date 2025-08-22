package org.example.common.model;

import lombok.Data;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;

import java.time.LocalDateTime;
import java.util.List;
import java.util.ArrayList;

/**
 * 菜单权限DTO
 * 用于前后端数据传输和模块间数据交换
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class MenuDTO {
    
    /**
     * 菜单ID
     */
    private Long id;
    
    /**
     * 菜单名称
     */
    private String menuName;
    
    /**
     * 父菜单ID
     */
    private Long parentId;
    
    /**
     * 显示顺序
     */
    private Integer orderNum;
    
    /**
     * 路由地址
     */
    private String path;
    
    /**
     * 组件路径
     */
    private String component;
    
    /**
     * 路由参数
     */
    private String queryParam;
    
    /**
     * 是否为外链：0-是，1-否
     */
    private Boolean isFrame;
    
    /**
     * 是否缓存：0-缓存，1-不缓存
     */
    private Boolean isCache;
    
    /**
     * 菜单类型：M-目录，C-菜单，F-按钮
     */
    private String menuType;
    
    /**
     * 菜单类型描述
     */
    private String menuTypeDesc;
    
    /**
     * 显示状态：0-隐藏，1-显示
     */
    private String visible;
    
    /**
     * 显示状态描述
     */
    private String visibleDesc;
    
    /**
     * 菜单状态：0-停用，1-正常
     */
    private String status;
    
    /**
     * 状态描述
     */
    private String statusDesc;
    
    /**
     * 权限标识
     */
    private String perms;
    
    /**
     * 菜单图标
     */
    private String icon;
    
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
     * 子菜单列表
     */
    private List<MenuDTO> children;
    
    /**
     * 父菜单名称
     */
    private String parentName;
    
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
        return isFrame != null && !isFrame;
    }
    
    /**
     * 是否缓存
     */
    public boolean isCached() {
        return isCache != null && !isCache;
    }
    
    /**
     * 获取菜单类型描述
     */
    public String getMenuTypeDescription() {
        if (menuType == null) {
            return "未知";
        }
        switch (menuType) {
            case "M":
                return "目录";
            case "C":
                return "菜单";
            case "F":
                return "按钮";
            default:
                return "未知";
        }
    }
    
    /**
     * 添加子菜单
     */
    public void addChild(MenuDTO child) {
        if (children == null) {
            children = new ArrayList<>();
        }
        children.add(child);
    }
    
    /**
     * 获取所有子菜单ID（递归）
     */
    public List<Long> getAllChildIds() {
        List<Long> ids = new ArrayList<>();
        if (children != null && !children.isEmpty()) {
            for (MenuDTO child : children) {
                ids.add(child.getId());
                ids.addAll(child.getAllChildIds());
            }
        }
        return ids;
    }
    
    /**
     * 获取所有权限标识（递归）
     */
    public List<String> getAllPermissions() {
        List<String> permissions = new ArrayList<>();
        if (perms != null && !perms.trim().isEmpty()) {
            permissions.add(perms);
        }
        if (children != null && !children.isEmpty()) {
            for (MenuDTO child : children) {
                permissions.addAll(child.getAllPermissions());
            }
        }
        return permissions;
    }
}
