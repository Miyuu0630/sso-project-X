package org.example.common.mapper;

import org.apache.ibatis.annotations.*;
import org.example.common.entity.UserPasswordHistory;
import java.util.List;

/**
 * 用户密码历史数据访问层
 */
@Mapper
public interface UserPasswordHistoryMapper {
    
    /**
     * 插入密码历史记录
     */
    @Insert("INSERT INTO user_password_history (user_id, password_hash, create_time) VALUES " +
            "(#{userId}, #{passwordHash}, #{createTime})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(UserPasswordHistory passwordHistory);
    
    /**
     * 根据ID查询
     */
    @Select("SELECT * FROM user_password_history WHERE id = #{id}")
    UserPasswordHistory selectById(Long id);
    
    /**
     * 根据用户ID查询密码历史
     */
    @Select("SELECT * FROM user_password_history WHERE user_id = #{userId} ORDER BY create_time DESC")
    List<UserPasswordHistory> selectByUserId(Long userId);
    
    /**
     * 根据用户ID查询最近N个密码历史
     */
    @Select("SELECT * FROM user_password_history WHERE user_id = #{userId} " +
            "ORDER BY create_time DESC LIMIT #{limit}")
    List<UserPasswordHistory> selectRecentByUserId(@Param("userId") Long userId, @Param("limit") Integer limit);
    
    /**
     * 检查用户是否使用过指定密码
     */
    @Select("SELECT COUNT(1) FROM user_password_history WHERE user_id = #{userId} AND password_hash = #{passwordHash}")
    int checkPasswordUsed(@Param("userId") Long userId, @Param("passwordHash") String passwordHash);
    
    /**
     * 删除用户指定时间之前的密码历史
     */
    @Delete("DELETE FROM user_password_history WHERE user_id = #{userId} AND create_time < #{beforeTime}")
    int deleteByUserIdAndTime(@Param("userId") Long userId, @Param("beforeTime") java.time.LocalDateTime beforeTime);
    
    /**
     * 根据用户ID删除所有密码历史
     */
    @Delete("DELETE FROM user_password_history WHERE user_id = #{userId}")
    int deleteByUserId(Long userId);
    
    /**
     * 删除指定ID的密码历史
     */
    @Delete("DELETE FROM user_password_history WHERE id = #{id}")
    int deleteById(Long id);
    
    /**
     * 保留用户最近N个密码历史，删除其他的
     */
    @Delete("DELETE FROM user_password_history WHERE user_id = #{userId} AND id NOT IN " +
            "(SELECT id FROM (SELECT id FROM user_password_history WHERE user_id = #{userId} " +
            "ORDER BY create_time DESC LIMIT #{keepCount}) AS temp)")
    int keepRecentAndDeleteOthers(@Param("userId") Long userId, @Param("keepCount") Integer keepCount);
    
    /**
     * 统计用户密码历史数量
     */
    @Select("SELECT COUNT(1) FROM user_password_history WHERE user_id = #{userId}")
    int countByUserId(Long userId);
}
