package org.example.ssoserver.mapper;

import org.apache.ibatis.annotations.*;
import org.example.ssoserver.entity.SysLoginLog;
import java.util.List;

/**
 * 登录记录数据访问层
 */
@Mapper
public interface SysLoginLogMapper {
    
    /**
     * 根据ID查询登录记录
     */
    @Select("SELECT * FROM sys_login_log WHERE id = #{id}")
    SysLoginLog selectById(Long id);
    
    /**
     * 插入登录记录
     */
    @Insert("INSERT INTO sys_login_log (user_id, username, login_type, login_ip, login_location, " +
            "browser, os, device_type, device_id, login_time, login_status, failure_reason, session_id) VALUES " +
            "(#{userId}, #{username}, #{loginType}, #{loginIp}, #{loginLocation}, #{browser}, #{os}, " +
            "#{deviceType}, #{deviceId}, #{loginTime}, #{loginStatus}, #{failureReason}, #{sessionId})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(SysLoginLog loginLog);
    
    /**
     * 更新登出时间
     */
    @Update("UPDATE sys_login_log SET logout_time = #{logoutTime} WHERE session_id = #{sessionId}")
    int updateLogoutTime(@Param("sessionId") String sessionId, 
                        @Param("logoutTime") java.time.LocalDateTime logoutTime);
    
    /**
     * 查询用户登录记录
     */
    @Select("<script>" +
            "<![CDATA[" +
            "SELECT * FROM sys_login_log WHERE 1=1 " +
            "]]>" +
            "<if test='userId != null'>" +
            "AND user_id = #{userId} " +
            "</if>" +
            "<if test='username != null and username != \"\"'>" +
            "<![CDATA[" +
            "AND username LIKE CONCAT('%', #{username}, '%') " +
            "]]>" +
            "</if>" +
            "<if test='loginType != null and loginType != \"\"'>" +
            "AND login_type = #{loginType} " +
            "</if>" +
            "<if test='loginStatus != null'>" +
            "AND login_status = #{loginStatus} " +
            "</if>" +
            "<if test='startTime != null'>" +
            "<![CDATA[" +
            "AND login_time >= #{startTime} " +
            "]]>" +
            "</if>" +
            "<if test='endTime != null'>" +
            "<![CDATA[" +
            "AND login_time <= #{endTime} " +
            "]]>" +
            "</if>" +
            "<![CDATA[" +
            "ORDER BY login_time DESC " +
            "LIMIT #{offset}, #{limit}" +
            "]]>" +
            "</script>")
    List<SysLoginLog> selectPage(@Param("userId") Long userId,
                                @Param("username") String username,
                                @Param("loginType") String loginType,
                                @Param("loginStatus") Integer loginStatus,
                                @Param("startTime") java.time.LocalDateTime startTime,
                                @Param("endTime") java.time.LocalDateTime endTime,
                                @Param("offset") Integer offset,
                                @Param("limit") Integer limit);
    
    /**
     * 查询登录记录总数
     */
    @Select("<script>" +
            "<![CDATA[" +
            "SELECT COUNT(1) FROM sys_login_log WHERE 1=1 " +
            "]]>" +
            "<if test='userId != null'>" +
            "AND user_id = #{userId} " +
            "</if>" +
            "<if test='username != null and username != \"\"'>" +
            "<![CDATA[" +
            "AND username LIKE CONCAT('%', #{username}, '%') " +
            "]]>" +
            "</if>" +
            "<if test='loginType != null and loginType != \"\"'>" +
            "AND login_type = #{loginType} " +
            "</if>" +
            "<if test='loginStatus != null'>" +
            "AND login_status = #{loginStatus} " +
            "</if>" +
            "<if test='startTime != null'>" +
            "<![CDATA[" +
            "AND login_time >= #{startTime} " +
            "]]>" +
            "</if>" +
            "<if test='endTime != null'>" +
            "<![CDATA[" +
            "AND login_time <= #{endTime} " +
            "]]>" +
            "</if>" +
            "</script>")
    int selectCount(@Param("userId") Long userId,
                   @Param("username") String username,
                   @Param("loginType") String loginType,
                   @Param("loginStatus") Integer loginStatus,
                   @Param("startTime") java.time.LocalDateTime startTime,
                   @Param("endTime") java.time.LocalDateTime endTime);
    
    /**
     * 查询用户最近登录记录
     */
    @Select("SELECT * FROM sys_login_log WHERE user_id = #{userId} AND login_status = 1 " +
            "ORDER BY login_time DESC LIMIT #{limit}")
    List<SysLoginLog> selectRecentByUserId(@Param("userId") Long userId, @Param("limit") Integer limit);
    
    /**
     * 查询指定时间内的登录失败次数
     */
    @Select("SELECT COUNT(1) FROM sys_login_log WHERE username = #{username} " +
            "AND login_status = 0 AND login_time >= #{startTime}")
    int countFailuresByUsername(@Param("username") String username, 
                               @Param("startTime") java.time.LocalDateTime startTime);
    
    /**
     * 查询指定IP的登录失败次数
     */
    @Select("SELECT COUNT(1) FROM sys_login_log WHERE login_ip = #{loginIp} " +
            "AND login_status = 0 AND login_time >= #{startTime}")
    int countFailuresByIp(@Param("loginIp") String loginIp, 
                         @Param("startTime") java.time.LocalDateTime startTime);
    
    /**
     * 删除指定时间之前的登录记录
     */
    @Delete("DELETE FROM sys_login_log WHERE login_time < #{beforeTime}")
    int deleteBeforeTime(@Param("beforeTime") java.time.LocalDateTime beforeTime);
    
    /**
     * 查询用户在线设备
     */
    @Select("SELECT DISTINCT device_type, device_id, login_ip, login_location, login_time " +
            "FROM sys_login_log WHERE user_id = #{userId} AND login_status = 1 " +
            "AND logout_time IS NULL ORDER BY login_time DESC")
    List<SysLoginLog> selectOnlineDevicesByUserId(Long userId);
}
