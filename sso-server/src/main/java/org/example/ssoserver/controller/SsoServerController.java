package org.example.ssoserver.controller;

import cn.dev33.satoken.sso.processor.SaSsoServerProcessor;
import cn.dev33.satoken.sso.template.SaSsoServerTemplate;
import cn.dev33.satoken.stp.StpUtil;
import cn.dev33.satoken.util.SaResult;
import org.example.common.entity.SysUser;
import org.example.ssoserver.service.SysUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletResponse;
import java.util.concurrent.TimeUnit;

/**
 * Sa-Token-SSO Server端 Controller
 */
@RestController
public class SsoServerController {

    @Autowired
    private SysUserService sysUserService;

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    /**
     * SSO认证页面 - 处理登录授权请求
     */
    @GetMapping("/sso/auth")
    public void ssoAuth(@RequestParam(value = "redirect", required = false) String redirect,
                        @RequestParam(value = "return_url", required = false) String returnUrl,
                        HttpServletResponse response) throws Exception {
        // 检查是否已登录
        if (StpUtil.isLogin()) {
            // 已登录，生成ticket并重定向
            // 使用 Sa-Token 的内置方法生成 ticket
            String ticket = "ST-" + System.currentTimeMillis() + "-" + StpUtil.getLoginId();
            // 将 ticket 存储到Redis中，有效期5分钟
            redisTemplate.opsForValue().set("sso-ticket-" + ticket, StpUtil.getLoginId().toString(), 300, TimeUnit.SECONDS);

            String redirectUrl = redirect + "?ticket=" + ticket;
            response.sendRedirect(redirectUrl);
        } else {
            // 未登录，返回登录页面
            response.setContentType("text/html;charset=UTF-8");
            response.getWriter().write(getLoginPageHtml());
        }
    }

    /**
     * SSO登录处理
     */
    @PostMapping("/sso/doLogin")
    public Object ssoDoLogin(@RequestParam("name") String name,
                            @RequestParam("pwd") String pwd) {
        try {
            // 查询数据库进行真实登录验证
            SysUser user = sysUserService.simpleLogin(name, pwd);
            if (user != null) {
                // 登录成功，设置会话
                StpUtil.login(user.getId());
                return SaResult.ok("登录成功！").setData(StpUtil.getTokenValue());
            }
            return SaResult.error("用户名或密码错误！");
        } catch (Exception e) {
            return SaResult.error("登录失败：" + e.getMessage());
        }
    }

    /**
     * 测试登录页面
     */
    @GetMapping("/test-login")
    public void testLogin(HttpServletResponse response) throws Exception {
        response.setContentType("text/html;charset=UTF-8");
        response.getWriter().write(getLoginPageHtml());
    }

    /**
     * 测试登录状态
     */
    @GetMapping("/test-status")
    @ResponseBody
    public Object testStatus() {
        return SaResult.ok("SSO Server 运行正常")
                .set("isLogin", StpUtil.isLogin())
                .set("loginId", StpUtil.isLogin() ? StpUtil.getLoginId() : null)
                .set("token", StpUtil.isLogin() ? StpUtil.getTokenValue() : null);
    }

    /**
     * SSO-Server端：处理其他SSO相关请求
     */
    @RequestMapping("/sso/signout")
    public Object ssoSignout() {
        return SaSsoServerProcessor.instance.dister();
    }

    /**
     * 配置SSO相关参数（保留基本配置）
     */
    @Autowired
    private void configSso(SaSsoServerTemplate ssoServerTemplate) {
        // 基本配置已在 application.yml 中完成
        // 这里可以添加其他自定义配置
    }

    /**
     * 获取登录页面HTML
     */
    private String getLoginPageHtml() {
        return """
            <!DOCTYPE html>
            <html lang="zh-CN">
            <head>
                <meta charset="UTF-8">
                <meta name="viewport" content="width=device-width, initial-scale=1.0">
                <title>SSO 统一登录</title>
                <style>
                    * {
                        margin: 0;
                        padding: 0;
                        box-sizing: border-box;
                    }

                    body {
                        font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif;
                        background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
                        min-height: 100vh;
                        display: flex;
                        align-items: center;
                        justify-content: center;
                    }

                    .login-container {
                        background: white;
                        padding: 40px;
                        border-radius: 10px;
                        box-shadow: 0 15px 35px rgba(0, 0, 0, 0.1);
                        width: 100%;
                        max-width: 400px;
                    }

                    .login-header {
                        text-align: center;
                        margin-bottom: 30px;
                    }

                    .login-header h1 {
                        color: #333;
                        font-size: 28px;
                        margin-bottom: 10px;
                    }

                    .login-header p {
                        color: #666;
                        font-size: 14px;
                    }

                    .form-group {
                        margin-bottom: 20px;
                    }

                    .form-group label {
                        display: block;
                        margin-bottom: 5px;
                        color: #333;
                        font-weight: 500;
                    }

                    .form-group input {
                        width: 100%;
                        padding: 12px;
                        border: 2px solid #e1e1e1;
                        border-radius: 5px;
                        font-size: 16px;
                        transition: border-color 0.3s;
                    }

                    .form-group input:focus {
                        outline: none;
                        border-color: #667eea;
                    }

                    .login-btn {
                        width: 100%;
                        padding: 12px;
                        background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
                        color: white;
                        border: none;
                        border-radius: 5px;
                        font-size: 16px;
                        font-weight: 500;
                        cursor: pointer;
                        transition: transform 0.2s;
                    }

                    .login-btn:hover {
                        transform: translateY(-2px);
                    }

                    .login-btn:disabled {
                        opacity: 0.6;
                        cursor: not-allowed;
                        transform: none;
                    }

                    .error-message {
                        color: #e74c3c;
                        font-size: 14px;
                        margin-top: 10px;
                        text-align: center;
                        display: none;
                    }

                    .success-message {
                        color: #27ae60;
                        font-size: 14px;
                        margin-top: 10px;
                        text-align: center;
                        display: none;
                    }

                    .demo-accounts {
                        margin-top: 20px;
                        padding: 15px;
                        background: #f8f9fa;
                        border-radius: 5px;
                        font-size: 12px;
                        color: #666;
                    }

                    .demo-accounts h4 {
                        margin-bottom: 8px;
                        color: #333;
                    }
                </style>
            </head>
            <body>
                <div class="login-container">
                    <div class="login-header">
                        <h1>SSO 统一登录</h1>
                        <p>请使用您的账号密码登录</p>
                    </div>

                    <form id="loginForm">
                        <div class="form-group">
                            <label for="username">用户名</label>
                            <input type="text" id="username" name="username" required placeholder="请输入用户名">
                        </div>

                        <div class="form-group">
                            <label for="password">密码</label>
                            <input type="password" id="password" name="password" required placeholder="请输入密码">
                        </div>

                        <button type="submit" class="login-btn" id="loginBtn">登录</button>

                        <div class="error-message" id="errorMessage"></div>
                        <div class="success-message" id="successMessage"></div>
                    </form>

                    <div class="demo-accounts">
                        <h4>测试账号：</h4>
                        <p>用户名: admin | 密码: admin123456</p>
                        <p>用户名: personal_user | 密码: 123456</p>
                    </div>
                </div>

                <script>
                    document.getElementById('loginForm').addEventListener('submit', async function(e) {
                        e.preventDefault();

                        const username = document.getElementById('username').value;
                        const password = document.getElementById('password').value;
                        const loginBtn = document.getElementById('loginBtn');
                        const errorMessage = document.getElementById('errorMessage');
                        const successMessage = document.getElementById('successMessage');

                        // 隐藏之前的消息
                        errorMessage.style.display = 'none';
                        successMessage.style.display = 'none';

                        // 禁用按钮
                        loginBtn.disabled = true;
                        loginBtn.textContent = '登录中...';

                        try {
                            const response = await fetch('/sso/doLogin', {
                                method: 'POST',
                                headers: {
                                    'Content-Type': 'application/x-www-form-urlencoded',
                                },
                                body: `name=${encodeURIComponent(username)}&pwd=${encodeURIComponent(password)}`
                            });

                            const result = await response.json();

                            if (result.code === 200) {
                                successMessage.textContent = '登录成功，正在跳转...';
                                successMessage.style.display = 'block';

                                // 延迟一下再刷新页面，让用户看到成功消息
                                setTimeout(() => {
                                    window.location.reload();
                                }, 1000);
                            } else {
                                errorMessage.textContent = result.msg || '登录失败，请检查用户名和密码';
                                errorMessage.style.display = 'block';
                            }
                        } catch (error) {
                            errorMessage.textContent = '网络错误，请稍后重试';
                            errorMessage.style.display = 'block';
                        } finally {
                            // 恢复按钮状态
                            loginBtn.disabled = false;
                            loginBtn.textContent = '登录';
                        }
                    });

                    // 快速填充测试账号
                    document.addEventListener('keydown', function(e) {
                        if (e.ctrlKey && e.key === '1') {
                            document.getElementById('username').value = 'admin';
                            document.getElementById('password').value = 'admin123456';
                            e.preventDefault();
                        }
                    });
                </script>
            </body>
            </html>
            """;
    }

}
