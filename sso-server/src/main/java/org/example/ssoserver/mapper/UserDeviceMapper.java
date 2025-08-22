package org.example.ssoserver.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;
import org.example.ssoserver.entity.UserDevice;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 用户设备Mapper接口
 */
public interface UserDeviceMapper extends BaseMapper<UserDevice> {
    
    /**
     * 根据用户ID查询设备列表
     * @param userId 用户ID
     * @return 设备列表
     */
    @Select("SELECT * FROM user_device WHERE user_id = #{userId} AND status = '1' ORDER BY last_active_time DESC")
    List<UserDevice> selectByUserId(@Param("userId") Long userId);
    
    /**
     * 根据设备指纹查询设备
     * @param deviceFingerprint 设备指纹
     * @return 设备信息
     */
    @Select("SELECT * FROM user_device WHERE device_fingerprint = #{deviceFingerprint} AND status = '1'")
    UserDevice selectByDeviceFingerprint(@Param("deviceFingerprint") String deviceFingerprint);
    
    /**
     * 根据用户ID和设备指纹查询设备
     * @param userId 用户ID
     * @param deviceFingerprint 设备指纹
     * @return 设备信息
     */
    @Select("SELECT * FROM user_device WHERE user_id = #{userId} AND device_fingerprint = #{deviceFingerprint}")
    UserDevice selectByUserIdAndFingerprint(@Param("userId") Long userId, 
                                           @Param("deviceFingerprint") String deviceFingerprint);
    
    /**
     * 更新设备最后活跃时间
     * @param id 设备ID
     * @param lastActiveTime 最后活跃时间
     * @return 更新行数
     */
    @Update("UPDATE user_device SET last_active_time = #{lastActiveTime} WHERE id = #{id}")
    int updateLastActiveTime(@Param("id") Long id, @Param("lastActiveTime") LocalDateTime lastActiveTime);
    
    /**
     * 更新设备信任状态
     * @param id 设备ID
     * @param isTrusted 是否信任
     * @return 更新行数
     */
    @Update("UPDATE user_device SET is_trusted = #{isTrusted} WHERE id = #{id}")
    int updateTrustStatus(@Param("id") Long id, @Param("isTrusted") Integer isTrusted);
    
    /**
     * 根据用户ID查询信任设备列表
     * @param userId 用户ID
     * @return 信任设备列表
     */
    @Select("SELECT * FROM user_device WHERE user_id = #{userId} AND is_trusted = 1 AND status = '1'")
    List<UserDevice> selectTrustedDevicesByUserId(@Param("userId") Long userId);
    
    /**
     * 根据用户ID统计设备数量
     * @param userId 用户ID
     * @return 设备数量
     */
    @Select("SELECT COUNT(*) FROM user_device WHERE user_id = #{userId} AND status = '1'")
    int countByUserId(@Param("userId") Long userId);
    
    /**
     * 根据设备类型统计数量
     * @param userId 用户ID
     * @param deviceType 设备类型
     * @return 设备数量
     */
    @Select("SELECT COUNT(*) FROM user_device WHERE user_id = #{userId} AND device_type = #{deviceType} AND status = '1'")
    int countByUserIdAndDeviceType(@Param("userId") Long userId, @Param("deviceType") String deviceType);
    
    /**
     * 查询长时间未活跃的设备
     * @param beforeTime 时间点
     * @return 设备列表
     */
    @Select("SELECT * FROM user_device WHERE last_active_time < #{beforeTime} AND status = '1'")
    List<UserDevice> selectInactiveDevices(@Param("beforeTime") LocalDateTime beforeTime);
    
    /**
     * 批量更新设备状态
     * @param deviceIds 设备ID列表
     * @param status 状态
     * @return 更新行数
     */
    int batchUpdateStatus(@Param("deviceIds") List<Long> deviceIds, @Param("status") String status);
    
    /**
     * 删除用户的所有设备记录
     * @param userId 用户ID
     * @return 删除行数
     */
    @Update("UPDATE user_device SET status = '0' WHERE user_id = #{userId}")
    int deleteByUserId(@Param("userId") Long userId);
}
