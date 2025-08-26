package org.example.ssoserver.util;

import cn.hutool.crypto.digest.DigestUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.ssoserver.entity.SysUser;
import org.example.ssoserver.mapper.SysUserMapper;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * 密码哈希修复工具
 * 用于修复数据库中错误的密码哈希值
 */
@Component
@Slf4j
@RequiredArgsConstructor
public class PasswordHashFixUtil implements CommandLineRunner {

    private final SysUserMapper userMapper;

    @Override
    public void run(String... args) throws Exception {
        // 只在启动时运行一次，并且只在开发环境运行
        if (args.length > 0 && "fix-passwords".equals(args[0])) {
            fixPasswordHashes();
        }
    }

    /**
     * 修复密码哈希值
     */
    public void fixPasswordHashes() {
        log.info("开始修复密码哈希值...");

        // 定义需要修复的用户密码信息
        Map<String, UserPasswordInfo> usersToFix = new HashMap<>();
        usersToFix.put("testuser", new UserPasswordInfo("testpass", "b1c2d3e4f5g6789012345678901234bc"));
        usersToFix.put("personal_user", new UserPasswordInfo("123456", "c2d3e4f5g6h789012345678901234cd"));
        usersToFix.put("enterprise_user", new UserPasswordInfo("123456", "d3e4f5g6h7i89012345678901234de"));
        usersToFix.put("airline_user", new UserPasswordInfo("123456", "e4f5g6h7i8j9012345678901234ef"));

        int fixedCount = 0;
        for (Map.Entry<String, UserPasswordInfo> entry : usersToFix.entrySet()) {
            String username = entry.getKey();
            UserPasswordInfo passwordInfo = entry.getValue();

            try {
                // 查询用户
                SysUser user = userMapper.selectByAccount(username);
                if (user == null) {
                    log.warn("用户不存在: {}", username);
                    continue;
                }

                // 计算正确的密码哈希
                String correctHash = DigestUtil.md5Hex(passwordInfo.password + passwordInfo.salt);

                // 检查当前哈希是否正确
                if (correctHash.equals(user.getPassword())) {
                    log.info("用户 {} 的密码哈希已经正确，跳过", username);
                    continue;
                }

                // 更新密码哈希
                SysUser updateUser = new SysUser();
                updateUser.setId(user.getId());
                updateUser.setPassword(correctHash);
                updateUser.setSalt(passwordInfo.salt);
                updateUser.setPasswordUpdateTime(LocalDateTime.now());
                updateUser.setFailedLoginCount(0);
                updateUser.setLastFailedLoginTime(null);

                int updated = userMapper.updateById(updateUser);
                if (updated > 0) {
                    log.info("✅ 修复用户 {} 的密码哈希: {} -> {}", username, user.getPassword(), correctHash);
                    fixedCount++;
                } else {
                    log.error("❌ 修复用户 {} 的密码哈希失败", username);
                }

            } catch (Exception e) {
                log.error("修复用户 {} 的密码哈希时发生异常", username, e);
            }
        }

        log.info("密码哈希修复完成，共修复 {} 个用户", fixedCount);

        // 验证修复结果
        verifyPasswordHashes();
    }

    /**
     * 验证密码哈希是否正确
     */
    public void verifyPasswordHashes() {
        log.info("开始验证密码哈希...");

        Map<String, UserPasswordInfo> usersToVerify = new HashMap<>();
        usersToVerify.put("admin", new UserPasswordInfo("admin123456", "a1b2c3d4e5f6789012345678901234ab"));
        usersToVerify.put("testuser", new UserPasswordInfo("testpass", "b1c2d3e4f5g6789012345678901234bc"));
        usersToVerify.put("personal_user", new UserPasswordInfo("123456", "c2d3e4f5g6h789012345678901234cd"));
        usersToVerify.put("enterprise_user", new UserPasswordInfo("123456", "d3e4f5g6h7i89012345678901234de"));
        usersToVerify.put("airline_user", new UserPasswordInfo("123456", "e4f5g6h7i8j9012345678901234ef"));

        for (Map.Entry<String, UserPasswordInfo> entry : usersToVerify.entrySet()) {
            String username = entry.getKey();
            UserPasswordInfo passwordInfo = entry.getValue();

            try {
                SysUser user = userMapper.selectByAccount(username);
                if (user == null) {
                    log.warn("用户不存在: {}", username);
                    continue;
                }

                boolean matches = Md5SaltUtil.matches(passwordInfo.password, user.getPassword(), user.getSalt());
                if (matches) {
                    log.info("✅ 用户 {} 密码验证通过", username);
                } else {
                    log.error("❌ 用户 {} 密码验证失败", username);
                    log.error("   期望密码: {}", passwordInfo.password);
                    log.error("   数据库哈希: {}", user.getPassword());
                    log.error("   数据库盐值: {}", user.getSalt());
                    log.error("   计算哈希: {}", DigestUtil.md5Hex(passwordInfo.password + user.getSalt()));
                }

            } catch (Exception e) {
                log.error("验证用户 {} 密码时发生异常", username, e);
            }
        }

        log.info("密码哈希验证完成");
    }

    /**
     * 用户密码信息
     */
    private static class UserPasswordInfo {
        final String password;
        final String salt;

        UserPasswordInfo(String password, String salt) {
            this.password = password;
            this.salt = salt;
        }
    }
}
