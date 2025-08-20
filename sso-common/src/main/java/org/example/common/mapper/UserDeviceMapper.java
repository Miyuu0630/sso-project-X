package org.example.common.mapper;

import org.apache.ibatis.annotations.*;
import org.example.common.entity.UserDevice;
import java.util.List;

/**
 * 用户设备数据访问层
 */
@Mapper
public interface UserDeviceMapper {
    
    /**
     * 插入用户设备
     */
    @Insert("INSERT INTO user_device (user_id, device_id, device_name, device_type, browser, " +
            "os, login_ip, login_location, last_login_time, is_active, is_trusted, create_time) VALUES " +
            "(#{userId}, #{deviceId}, #{deviceName}, #{deviceType}, #{browser}, #{os}, " +
            "#{loginIp}, #{loginLocation}, #{lastLoginTime}, #{isActive}, #{isTrusted}, #{createTime})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(UserDevice device);
    
    /**
     * 根据ID查询
     */
    @Select("SELECT * FROM user_device WHERE id = #{id}")
    UserDevice selectById(Long id);
    
    /**
     * 根据用户ID查询设备列表
     */
    @Select("SELECT * FROM user_device WHERE user_id = #{userId} ORDER BY last_login_time DESC")
    List<UserDevice> selectByUserId(Long userId);
    
    /**
     * 根据用户ID和设备ID查询
     */
    @Select("SELECT * FROM user_device WHERE user_id = #{userId} AND device_id = #{deviceId}")
    UserDevice selectByUserIdAndDeviceId(@Param("userId") Long userId, @Param("deviceId") String deviceId);
    
    /**
     * 更新设备信息
     */
    @Update("UPDATE user_device SET device_name = #{deviceName}, device_type = #{deviceType}, " +
            "browser = #{browser}, os = #{os}, login_ip = #{loginIp}, login_location = #{loginLocation}, " +
            "last_login_time = #{lastLoginTime}, is_active = #{isActive}, is_trusted = #{isTrusted}, " +
            "update_time = #{updateTime} WHERE id = #{id}")
    int update(UserDevice device);
    
    /**
     * 更新最后登录时间
     */
    @Update("UPDATE user_device SET last_login_time = #{lastLoginTime}, login_ip = #{loginIp}, " +
            "login_location = #{loginLocation}, update_time = NOW() WHERE user_id = #{userId} AND device_id = #{deviceId}")
    int updateLastLogin(@Param("userId") Long userId, @Param("deviceId") String deviceId, 
                       @Param("lastLoginTime") java.time.LocalDateTime lastLoginTime,
                       @Param("loginIp") String loginIp, @Param("loginLocation") String loginLocation);
    
    /**
     * 删除设备
     */
    @Delete("DELETE FROM user_device WHERE id = #{id}")
    int deleteById(Long id);
    
    /**
     * 根据用户ID删除所有设备
     */
    @Delete("DELETE FROM user_device WHERE user_id = #{userId}")
    int deleteByUserId(Long userId);
}
