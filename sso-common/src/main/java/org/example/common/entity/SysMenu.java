package org.example.common.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import lombok.EqualsAndHashCode;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 菜单权限实体类
 */
@Data
@EqualsAndHashCode(callSuper = false)
public class SysMenu {
    
    /**
     * 菜单ID
     */
    private Long id;
    
    /**
     * 菜单名称
     */
    @NotBlank(message = "菜单名称不能为空")
    @Size(max = 50, message = "菜单名称长度不能超过50个字符")
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
    @Size(max = 200, message = "路由地址长度不能超过200个字符")
    private String path;
    
    /**
     * 组件路径
     */
    @Size(max = 255, message = "组件路径长度不能超过255个字符")
    private String component;
    
    /**
     * 路由参数
     */
    @Size(max = 255, message = "路由参数长度不能超过255个字符")
    private String queryParam;
    
    /**
     * 是否为外链：0-是，1-否
     */
    private Integer isFrame;

    /**
     * 是否缓存：0-缓存，1-不缓存
     */
    private Integer isCache;
    
    /**
     * 菜单类型：M-目录，C-菜单，F-按钮
     */
    private String menuType;
    
    /**
     * 显示状态：0-隐藏，1-显示
     */
    private String visible;
    
    /**
     * 菜单状态：0-停用，1-正常
     */
    private String status;
    
    /**
     * 权限标识
     */
    @Size(max = 100, message = "权限标识长度不能超过100个字符")
    private String perms;
    
    /**
     * 菜单图标
     */
    @Size(max = 100, message = "菜单图标长度不能超过100个字符")
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
     * 子菜单列表(非数据库字段)
     */
    private List<SysMenu> children;
    
    /**
     * 判断菜单是否启用
     */
    public boolean isEnabled() {
        return status != null && "1".equals(status);
    }
    
    /**
     * 判断是否显示
     */
    public boolean isVisible() {
        return visible != null && "1".equals(visible);
    }
    
    /**
     * 获取菜单类型描述
     */
    public String getMenuTypeDesc() {
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
     * 获取状态描述
     */
    public String getStatusDesc() {
        if (status == null) {
            return "未知";
        }
        return "1".equals(status) ? "正常" : "停用";
    }
    
    /**
     * 获取显示状态描述
     */
    public String getVisibleDesc() {
        if (visible == null) {
            return "未知";
        }
        return "1".equals(visible) ? "显示" : "隐藏";
    }
    
    /**
     * 判断是否为根菜单
     */
    public boolean isRoot() {
        return parentId == null || parentId == 0;
    }
    
    /**
     * 判断是否为目录
     */
    public boolean isDirectory() {
        return "M".equals(menuType);
    }
    
    /**
     * 判断是否为菜单
     */
    public boolean isMenu() {
        return "C".equals(menuType);
    }
    
    /**
     * 判断是否为按钮
     */
    public boolean isButton() {
        return "F".equals(menuType);
    }
}
