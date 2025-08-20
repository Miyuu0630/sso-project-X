package org.example.common.mapper;

import org.apache.ibatis.annotations.*;
import org.example.common.entity.SysLoginLog;
import java.util.List;

/**
 * 登录记录数据访问层
 */
@Mapper
public interface SysLoginLogMapper {

    /**
     * 插入登录记录
     */
    @Insert("INSERT INTO sys_login_log (user_id, username, login_type, login_ip, login_location, " +
            "browser, os, device_type, device_fingerprint, is_new_device, is_abnormal, " +
            "login_time, logout_time, login_duration, status, msg) VALUES " +
            "(#{userId}, #{username}, #{loginType}, #{loginIp}, #{loginLocation}, #{browser}, #{os}, " +
            "#{deviceType}, #{deviceFingerprint}, #{isNewDevice}, #{isAbnormal}, " +
            "#{loginTime}, #{logoutTime}, #{loginDuration}, #{status}, #{msg})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(SysLoginLog loginLog);

    /**
     * 根据ID查询
     */
    @Select("SELECT * FROM sys_login_log WHERE id = #{id}")
    SysLoginLog selectById(Long id);

    /**
     * 根据用户ID查询登录记录
     */
    @Select("SELECT * FROM sys_login_log WHERE user_id = #{userId} ORDER BY login_time DESC")
    List<SysLoginLog> selectByUserId(Long userId);

    /**
     * 更新登出时间和登录时长
     */
    @Update("UPDATE sys_login_log SET logout_time = #{logoutTime}, " +
            "login_duration = #{loginDuration} WHERE id = #{id}")
    int updateLogout(@Param("id") Long id,
                    @Param("logoutTime") java.time.LocalDateTime logoutTime,
                    @Param("loginDuration") Integer loginDuration);

    /**
     * 删除记录
     */
    @Delete("DELETE FROM sys_login_log WHERE id = #{id}")
    int deleteById(Long id);

    /**
     * 根据用户ID删除登录记录
     */
    @Delete("DELETE FROM sys_login_log WHERE user_id = #{userId}")
    int deleteByUserId(Long userId);

    /**
     * 查询指定时间内的登录失败次数
     */
    @Select("SELECT COUNT(1) FROM sys_login_log WHERE username = #{username} " +
            "AND status = '0' AND login_time >= #{startTime}")
    int countFailuresByUsername(@Param("username") String username,
                               @Param("startTime") java.time.LocalDateTime startTime);

    /**
     * 查询用户最近的登录记录
     */
    @Select("SELECT * FROM sys_login_log WHERE user_id = #{userId} " +
            "ORDER BY login_time DESC LIMIT #{limit}")
    List<SysLoginLog> selectRecentByUserId(@Param("userId") Long userId, @Param("limit") Integer limit);

    /**
     * 分页查询登录记录
     */
    @Select("<script>" +
            "SELECT * FROM sys_login_log WHERE 1=1 " +
            "<if test='userId != null'> AND user_id = #{userId} </if>" +
            "<if test='username != null and username != \"\"'> AND username LIKE CONCAT('%', #{username}, '%') </if>" +
            "<if test='loginType != null and loginType != \"\"'> AND login_type = #{loginType} </if>" +
            "<if test='status != null and status != \"\"'> AND status = #{status} </if>" +
            "<if test='startTime != null'> AND login_time >= #{startTime} </if>" +
            "<if test='endTime != null'> AND login_time &lt;= #{endTime} </if>" +
            "ORDER BY login_time DESC LIMIT #{offset}, #{pageSize}" +
            "</script>")
    List<SysLoginLog> selectPage(@Param("userId") Long userId,
                                @Param("username") String username,
                                @Param("loginType") String loginType,
                                @Param("status") String status,
                                @Param("startTime") java.time.LocalDateTime startTime,
                                @Param("endTime") java.time.LocalDateTime endTime,
                                @Param("offset") Integer offset,
                                @Param("pageSize") Integer pageSize);

    /**
     * 统计登录记录数量
     */
    @Select("<script>" +
            "SELECT COUNT(1) FROM sys_login_log WHERE 1=1 " +
            "<if test='userId != null'> AND user_id = #{userId} </if>" +
            "<if test='username != null and username != \"\"'> AND username LIKE CONCAT('%', #{username}, '%') </if>" +
            "<if test='loginType != null and loginType != \"\"'> AND login_type = #{loginType} </if>" +
            "<if test='status != null and status != \"\"'> AND status = #{status} </if>" +
            "<if test='startTime != null'> AND login_time >= #{startTime} </if>" +
            "<if test='endTime != null'> AND login_time &lt;= #{endTime} </if>" +
            "</script>")
    int selectCount(@Param("userId") Long userId,
                   @Param("username") String username,
                   @Param("loginType") String loginType,
                   @Param("status") String status,
                   @Param("startTime") java.time.LocalDateTime startTime,
                   @Param("endTime") java.time.LocalDateTime endTime);

    /**
     * 查询用户在线设备（最近登录且未登出的记录）
     */
    @Select("SELECT * FROM sys_login_log WHERE user_id = #{userId} " +
            "AND status = '1' AND logout_time IS NULL " +
            "ORDER BY login_time DESC")
    List<SysLoginLog> selectOnlineDevicesByUserId(@Param("userId") Long userId);

    /**
     * 根据设备指纹更新登出时间
     */
    @Update("UPDATE sys_login_log SET logout_time = #{logoutTime}, " +
            "login_duration = TIMESTAMPDIFF(MINUTE, login_time, #{logoutTime}) " +
            "WHERE device_fingerprint = #{deviceFingerprint} AND logout_time IS NULL")
    int updateLogoutTimeByDevice(@Param("deviceFingerprint") String deviceFingerprint,
                                @Param("logoutTime") java.time.LocalDateTime logoutTime);

    /**
     * 删除指定时间之前的记录
     */
    @Delete("DELETE FROM sys_login_log WHERE login_time < #{beforeTime}")
    int deleteBeforeTime(@Param("beforeTime") java.time.LocalDateTime beforeTime);
}
