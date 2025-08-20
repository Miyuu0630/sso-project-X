package org.example.common.mapper;

import org.apache.ibatis.annotations.*;
import org.example.common.entity.UserOauthBinding;
import java.util.List;

/**
 * 第三方账号绑定数据访问层
 */
@Mapper
public interface UserOauthBindingMapper {
    
    /**
     * 插入第三方账号绑定
     */
    @Insert("INSERT INTO user_oauth_binding (user_id, provider, open_id, union_id, nickname, " +
            "avatar, access_token, refresh_token, expires_in, bind_time, is_active) VALUES " +
            "(#{userId}, #{provider}, #{openId}, #{unionId}, #{nickname}, #{avatar}, " +
            "#{accessToken}, #{refreshToken}, #{expiresIn}, #{bindTime}, #{isActive})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(UserOauthBinding binding);
    
    /**
     * 根据ID查询
     */
    @Select("SELECT * FROM user_oauth_binding WHERE id = #{id}")
    UserOauthBinding selectById(Long id);
    
    /**
     * 根据用户ID查询绑定列表
     */
    @Select("SELECT * FROM user_oauth_binding WHERE user_id = #{userId}")
    List<UserOauthBinding> selectByUserId(Long userId);
    
    /**
     * 根据第三方平台和openId查询
     */
    @Select("SELECT * FROM user_oauth_binding WHERE provider = #{provider} AND open_id = #{openId}")
    UserOauthBinding selectByProviderAndOpenId(@Param("provider") String provider, @Param("openId") String openId);
    
    /**
     * 更新绑定信息
     */
    @Update("UPDATE user_oauth_binding SET nickname = #{nickname}, avatar = #{avatar}, " +
            "access_token = #{accessToken}, refresh_token = #{refreshToken}, " +
            "expires_in = #{expiresIn}, last_login_time = #{lastLoginTime}, " +
            "is_active = #{isActive} WHERE id = #{id}")
    int update(UserOauthBinding binding);
    
    /**
     * 删除绑定
     */
    @Delete("DELETE FROM user_oauth_binding WHERE id = #{id}")
    int deleteById(Long id);
    
    /**
     * 根据用户ID和平台删除绑定
     */
    @Delete("DELETE FROM user_oauth_binding WHERE user_id = #{userId} AND provider = #{provider}")
    int deleteByUserIdAndProvider(@Param("userId") Long userId, @Param("provider") String provider);
}
