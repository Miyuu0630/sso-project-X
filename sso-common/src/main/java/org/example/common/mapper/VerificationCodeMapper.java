package org.example.common.mapper;

import org.apache.ibatis.annotations.*;
import org.example.common.entity.VerificationCode;
import java.util.List;

/**
 * 验证码记录数据访问层
 */
@Mapper
public interface VerificationCodeMapper {
    
    /**
     * 插入验证码记录
     */
    @Insert("INSERT INTO verification_code (code_type, target, code, purpose, is_used, expire_time, create_time) VALUES " +
            "(#{codeType}, #{target}, #{code}, #{purpose}, #{isUsed}, #{expireTime}, #{createTime})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(VerificationCode verificationCode);
    
    /**
     * 根据ID查询
     */
    @Select("SELECT * FROM verification_code WHERE id = #{id}")
    VerificationCode selectById(Long id);
    
    /**
     * 根据目标和类型查询最新的验证码
     */
    @Select("SELECT * FROM verification_code WHERE target = #{target} AND code_type = #{codeType} " +
            "AND purpose = #{purpose} ORDER BY create_time DESC LIMIT 1")
    VerificationCode selectLatestByTargetAndType(@Param("target") String target, 
                                               @Param("codeType") String codeType,
                                               @Param("purpose") String purpose);
    
    /**
     * 根据目标、类型和验证码查询
     */
    @Select("SELECT * FROM verification_code WHERE target = #{target} AND code_type = #{codeType} " +
            "AND code = #{code} AND purpose = #{purpose} AND is_used = 0 AND expire_time > NOW()")
    VerificationCode selectValidCode(@Param("target") String target, 
                                   @Param("codeType") String codeType,
                                   @Param("code") String code,
                                   @Param("purpose") String purpose);
    
    /**
     * 更新验证码为已使用
     */
    @Update("UPDATE verification_code SET is_used = 1 WHERE id = #{id}")
    int markAsUsed(Long id);
    
    /**
     * 删除过期的验证码
     */
    @Delete("DELETE FROM verification_code WHERE expire_time < NOW()")
    int deleteExpired();
    
    /**
     * 根据目标查询指定时间内的验证码数量
     */
    @Select("SELECT COUNT(1) FROM verification_code WHERE target = #{target} AND code_type = #{codeType} " +
            "AND create_time >= #{startTime}")
    int countByTargetAndTimeRange(@Param("target") String target, 
                                @Param("codeType") String codeType,
                                @Param("startTime") java.time.LocalDateTime startTime);
    
    /**
     * 删除指定ID的验证码
     */
    @Delete("DELETE FROM verification_code WHERE id = #{id}")
    int deleteById(Long id);
}
