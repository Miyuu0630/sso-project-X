package org.example.ssoclient.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 菜单DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MenuDTO {
    
    /**
     * 菜单ID
     */
    private Long id;
    
    /**
     * 菜单名称
     */
    private String name;
    
    /**
     * 菜单路径
     */
    private String path;
    
    /**
     * 菜单图标
     */
    private String icon;
    
    /**
     * 父菜单ID
     */
    private Long parentId;
    
    /**
     * 菜单类型（menu: 菜单, button: 按钮）
     */
    private String type;
    
    /**
     * 排序号
     */
    private Integer sort;
    
    /**
     * 是否隐藏
     */
    private Boolean hidden;
    
    /**
     * 权限标识
     */
    private String permission;
    
    /**
     * 子菜单列表
     */
    private List<MenuDTO> children;
    
    /**
     * 菜单元数据
     */
    private MenuMeta meta;
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class MenuMeta {
        /**
         * 页面标题
         */
        private String title;
        
        /**
         * 是否需要认证
         */
        private Boolean requiresAuth;
        
        /**
         * 需要的角色
         */
        private List<String> requiredRoles;
        
        /**
         * 需要的权限
         */
        private String requiredPermission;
        
        /**
         * 是否缓存
         */
        private Boolean keepAlive;
        
        /**
         * 是否在面包屑中显示
         */
        private Boolean breadcrumb;
        
        /**
         * 是否在标签页中显示
         */
        private Boolean showInTabs;
    }
}
