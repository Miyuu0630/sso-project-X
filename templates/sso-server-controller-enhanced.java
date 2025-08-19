/**
 * 增强版 SSO Server 控制器
 * 支持 Thymeleaf 页面渲染和完整的认证流程
 */

@Controller
@Slf4j
public class SsoServerController {

    @Autowired
    private SysUserService userService;
    
    @Autowired
    private SaSsoServerTemplate ssoServerTemplate;

    /**
     * SSO 统一处理入口
     */
    @RequestMapping("/sso/*")
    public Object ssoRequest() {
        return SaSsoServerProcessor.instance.dister();
    }

    /**
     * 登录页面
     */
    @GetMapping("/login")
    public String loginPage(Model model, HttpServletRequest request) {
        // 检查是否已经登录
        if (StpUtil.isLogin()) {
            String redirect = request.getParameter("redirect");
            if (StringUtils.hasText(redirect)) {
                return "redirect:" + redirect;
            }
            return "redirect:/";
        }
        
        // 传递参数到模板
        model.addAttribute("redirect", request.getParameter("redirect"));
        model.addAttribute("error", request.getParameter("error"));
        model.addAttribute("message", request.getParameter("message"));
        
        return "login";
    }

    /**
     * 注册页面
     */
    @GetMapping("/register")
    public String registerPage(Model model) {
        return "register";
    }

    /**
     * 处理登录请求
     */
    @PostMapping("/sso/doLogin")
    @ResponseBody
    public SaResult doLogin(@RequestParam String name, 
                           @RequestParam String pwd,
                           @RequestParam(defaultValue = "username") String loginType,
                           @RequestParam(defaultValue = "false") boolean rememberMe,
                           HttpServletRequest request) {
        try {
            log.info("用户登录请求: loginType={}, credential={}", loginType, name);
            
            // 1. 参数验证
            if (StringUtils.isEmpty(name) || StringUtils.isEmpty(pwd)) {
                return SaResult.error("用户名和密码不能为空");
            }
            
            // 2. 根据登录类型查找用户
            SysUser user = findUserByCredential(name, loginType);
            if (user == null) {
                return SaResult.error("用户不存在");
            }
            
            // 3. 验证密码
            if (!passwordEncoder.matches(pwd, user.getPassword())) {
                // 记录登录失败
                recordLoginFailure(user, request);
                return SaResult.error("密码错误");
            }
            
            // 4. 检查账号状态
            if (user.getStatus() == 0) {
                return SaResult.error("账号已被禁用");
            }
            if (user.getStatus() == 2) {
                return SaResult.error("账号已被锁定");
            }
            
            // 5. 执行登录
            StpUtil.login(user.getId(), rememberMe);
            
            // 6. 记录登录成功
            recordLoginSuccess(user, request);
            
            // 7. 返回成功结果
            return SaResult.ok("登录成功")
                .setData(Map.of(
                    "token", StpUtil.getTokenValue(),
                    "userInfo", buildUserInfo(user)
                ));
                
        } catch (Exception e) {
            log.error("登录处理异常", e);
            return SaResult.error("登录失败，请重试");
        }
    }

    /**
     * 处理注册请求
     */
    @PostMapping("/api/auth/register")
    @ResponseBody
    public SaResult register(@RequestParam String username,
                            @RequestParam String realName,
                            @RequestParam String phone,
                            @RequestParam String email,
                            @RequestParam String password,
                            @RequestParam(defaultValue = "1") Integer userType,
                            HttpServletRequest request) {
        try {
            log.info("用户注册请求: username={}, email={}, phone={}", username, email, phone);
            
            // 1. 参数验证
            String validationError = validateRegisterParams(username, realName, phone, email, password);
            if (validationError != null) {
                return SaResult.error(validationError);
            }
            
            // 2. 检查用户名是否已存在
            if (userService.existsByUsername(username)) {
                return SaResult.error("用户名已存在");
            }
            
            // 3. 检查手机号是否已存在
            if (userService.existsByPhone(phone)) {
                return SaResult.error("手机号已被注册");
            }
            
            // 4. 检查邮箱是否已存在
            if (userService.existsByEmail(email)) {
                return SaResult.error("邮箱已被注册");
            }
            
            // 5. 创建用户
            SysUser user = new SysUser();
            user.setUsername(username);
            user.setRealName(realName);
            user.setPhone(phone);
            user.setEmail(email);
            user.setPassword(passwordEncoder.encode(password));
            user.setUserType(userType);
            user.setStatus(1); // 默认启用
            user.setCreateTime(LocalDateTime.now());
            
            // 6. 保存用户
            userService.save(user);
            
            // 7. 分配默认角色
            assignDefaultRole(user);
            
            // 8. 记录注册日志
            recordRegistration(user, request);
            
            log.info("用户注册成功: userId={}, username={}", user.getId(), username);
            return SaResult.ok("注册成功");
            
        } catch (Exception e) {
            log.error("注册处理异常", e);
            return SaResult.error("注册失败，请重试");
        }
    }

    /**
     * 登出处理
     */
    @PostMapping("/logout")
    public String logout(HttpServletRequest request) {
        if (StpUtil.isLogin()) {
            Long userId = StpUtil.getLoginIdAsLong();
            StpUtil.logout();
            
            // 记录登出日志
            recordLogout(userId, request);
            
            log.info("用户登出: userId={}", userId);
        }
        
        return "redirect:/login?message=" + URLEncoder.encode("已安全登出", StandardCharsets.UTF_8);
    }

    /**
     * 配置 SSO 相关参数
     */
    @Autowired
    private void configSso(SaSsoServerTemplate ssoServerTemplate) {
        // 配置：未登录时返回的View
        ssoServerTemplate.strategy.notLoginView = () -> {
            return "redirect:/login";
        };

        // 配置：登录处理函数
        ssoServerTemplate.strategy.doLoginHandle = (name, pwd) -> {
            // 委托给上面的 doLogin 方法处理
            return doLogin(name, pwd, "username", false, 
                getCurrentRequest()).getData();
        };
    }

    // ========================================
    // 私有辅助方法
    // ========================================

    /**
     * 根据凭证类型查找用户
     */
    private SysUser findUserByCredential(String credential, String loginType) {
        switch (loginType) {
            case "phone":
                return userService.findByPhone(credential);
            case "email":
                return userService.findByEmail(credential);
            case "username":
            default:
                return userService.findByUsername(credential);
        }
    }

    /**
     * 验证注册参数
     */
    private String validateRegisterParams(String username, String realName, 
                                        String phone, String email, String password) {
        // 用户名验证
        if (!username.matches("^[a-zA-Z0-9_]{3,20}$")) {
            return "用户名只能包含字母、数字和下划线，长度3-20位";
        }
        
        // 真实姓名验证
        if (realName.length() < 2 || realName.length() > 20) {
            return "真实姓名长度应在2-20位之间";
        }
        
        // 手机号验证
        if (!phone.matches("^1[3-9]\\d{9}$")) {
            return "请输入正确的手机号格式";
        }
        
        // 邮箱验证
        if (!email.matches("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$")) {
            return "请输入正确的邮箱格式";
        }
        
        // 密码验证
        if (password.length() < 6) {
            return "密码长度至少6位";
        }
        if (!password.matches(".*[a-zA-Z].*") || !password.matches(".*\\d.*")) {
            return "密码必须包含字母和数字";
        }
        
        return null;
    }

    /**
     * 构建用户信息
     */
    private Map<String, Object> buildUserInfo(SysUser user) {
        Map<String, Object> userInfo = new HashMap<>();
        userInfo.put("userId", user.getId());
        userInfo.put("username", user.getUsername());
        userInfo.put("realName", user.getRealName());
        userInfo.put("email", user.getEmail());
        userInfo.put("phone", user.getPhone());
        userInfo.put("userType", user.getUserType());
        userInfo.put("avatar", user.getAvatar());
        
        // 获取用户角色
        List<String> roles = roleService.getUserRoles(user.getId())
            .stream()
            .map(SysRole::getRoleCode)
            .collect(Collectors.toList());
        userInfo.put("roles", roles);
        
        return userInfo;
    }

    /**
     * 记录登录成功
     */
    private void recordLoginSuccess(SysUser user, HttpServletRequest request) {
        // 更新用户最后登录信息
        user.setLastLoginTime(LocalDateTime.now());
        user.setLastLoginIp(getClientIp(request));
        user.setLoginFailureCount(0); // 重置失败次数
        userService.updateById(user);
        
        // 记录登录日志
        // ... 实现登录日志记录
    }

    /**
     * 记录登录失败
     */
    private void recordLoginFailure(SysUser user, HttpServletRequest request) {
        // 增加失败次数
        user.setLoginFailureCount(user.getLoginFailureCount() + 1);
        
        // 如果失败次数过多，锁定账号
        if (user.getLoginFailureCount() >= 5) {
            user.setStatus(2); // 锁定状态
            user.setLockTime(LocalDateTime.now());
        }
        
        userService.updateById(user);
        
        // 记录失败日志
        // ... 实现失败日志记录
    }

    /**
     * 分配默认角色
     */
    private void assignDefaultRole(SysUser user) {
        // 根据用户类型分配默认角色
        String defaultRoleCode;
        switch (user.getUserType()) {
            case 2:
                defaultRoleCode = "ENTERPRISE_USER";
                break;
            case 3:
                defaultRoleCode = "AIRLINE_USER";
                break;
            default:
                defaultRoleCode = "PERSONAL_USER";
                break;
        }
        
        // 分配角色
        roleService.assignRoleToUser(user.getId(), defaultRoleCode);
    }

    /**
     * 获取客户端IP
     */
    private String getClientIp(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("X-Real-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        return ip;
    }

    /**
     * 获取当前请求
     */
    private HttpServletRequest getCurrentRequest() {
        return ((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes()).getRequest();
    }
}
