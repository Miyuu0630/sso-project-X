/**
 * Refresh Token客户端集成示例
 * 演示如何在前端应用中使用Refresh Token功能
 */

// 全局配置
const CONFIG = {
    baseURL: 'http://localhost:8081',
    tokenStorageKey: 'sso_tokens',
    refreshThreshold: 300000, // 5分钟前开始刷新
    maxRetryAttempts: 3,
    retryDelay: 1000
};

/**
 * Token管理器
 * 负责Access Token和Refresh Token的存储和管理
 */
class TokenManager {
    constructor() {
        this.tokens = this.loadTokens();
        this.refreshPromise = null; // 防止并发刷新
        this.initAutoRefresh();
    }

    /**
     * 加载存储的tokens
     */
    loadTokens() {
        try {
            const stored = localStorage.getItem(CONFIG.tokenStorageKey);
            return stored ? JSON.parse(stored) : {};
        } catch (e) {
            console.warn('Failed to load tokens from storage:', e);
            return {};
        }
    }

    /**
     * 保存tokens到存储
     */
    saveTokens(tokens) {
        try {
            this.tokens = { ...this.tokens, ...tokens };
            localStorage.setItem(CONFIG.tokenStorageKey, JSON.stringify(this.tokens));
        } catch (e) {
            console.error('Failed to save tokens:', e);
        }
    }

    /**
     * 清除所有tokens
     */
    clearTokens() {
        this.tokens = {};
        localStorage.removeItem(CONFIG.tokenStorageKey);
        this.stopAutoRefresh();
    }

    /**
     * 获取Access Token
     */
    getAccessToken() {
        return this.tokens.accessToken;
    }

    /**
     * 获取Refresh Token
     */
    getRefreshToken() {
        return this.tokens.refreshToken;
    }

    /**
     * 检查Access Token是否即将过期
     */
    isAccessTokenExpiringSoon() {
        if (!this.tokens.expiresAt) return true;

        const now = Date.now();
        const expiresAt = new Date(this.tokens.expiresAt).getTime();
        const timeUntilExpiry = expiresAt - now;

        return timeUntilExpiry < CONFIG.refreshThreshold;
    }

    /**
     * 初始化自动刷新
     */
    initAutoRefresh() {
        if (this.tokens.refreshToken && this.tokens.expiresAt) {
            const expiresAt = new Date(this.tokens.expiresAt).getTime();
            const now = Date.now();
            const timeUntilRefresh = Math.max(0, expiresAt - now - CONFIG.refreshThreshold);

            if (timeUntilRefresh > 0) {
                this.autoRefreshTimer = setTimeout(() => {
                    this.refreshTokens();
                }, timeUntilRefresh);
            }
        }
    }

    /**
     * 停止自动刷新
     */
    stopAutoRefresh() {
        if (this.autoRefreshTimer) {
            clearTimeout(this.autoRefreshTimer);
            this.autoRefreshTimer = null;
        }
    }

    /**
     * 刷新tokens
     */
    async refreshTokens() {
        if (this.refreshPromise) {
            return this.refreshPromise; // 如果已经在刷新中，返回现有Promise
        }

        if (!this.tokens.refreshToken) {
            throw new Error('No refresh token available');
        }

        this.refreshPromise = this.performTokenRefresh();

        try {
            const result = await this.refreshPromise;
            this.saveTokens(result);
            this.initAutoRefresh(); // 重新初始化自动刷新
            return result;
        } finally {
            this.refreshPromise = null;
        }
    }

    /**
     * 执行token刷新请求
     */
    async performTokenRefresh(attempt = 1) {
        try {
            const deviceFingerprint = this.generateDeviceFingerprint();

            const response = await fetch(`${CONFIG.baseURL}/auth/refresh/secure`, {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/x-www-form-urlencoded',
                },
                body: new URLSearchParams({
                    refreshToken: this.tokens.refreshToken,
                    deviceFingerprint: deviceFingerprint
                })
            });

            if (!response.ok) {
                if (response.status === 401) {
                    // Refresh token无效，清除所有tokens
                    this.clearTokens();
                    throw new Error('Refresh token expired or invalid');
                }
                throw new Error(`Token refresh failed: ${response.status}`);
            }

            const data = await response.json();
            if (data.code !== 200) {
                throw new Error(data.message || 'Token refresh failed');
            }

            // 计算新的过期时间
            const expiresAt = new Date(Date.now() + (data.data.expiresIn * 1000));

            return {
                accessToken: data.data.accessToken,
                refreshToken: this.tokens.refreshToken, // Refresh token通常保持不变
                expiresIn: data.data.expiresIn,
                expiresAt: expiresAt.toISOString(),
                tokenType: data.data.tokenType
            };

        } catch (error) {
            console.error(`Token refresh attempt ${attempt} failed:`, error);

            if (attempt < CONFIG.maxRetryAttempts) {
                // 延迟重试
                await new Promise(resolve => setTimeout(resolve, CONFIG.retryDelay * attempt));
                return this.performTokenRefresh(attempt + 1);
            }

            throw error;
        }
    }

    /**
     * 生成设备指纹
     */
    generateDeviceFingerprint() {
        const canvas = document.createElement('canvas');
        const ctx = canvas.getContext('2d');
        ctx.textBaseline = 'top';
        ctx.font = '14px Arial';
        ctx.fillText('Device fingerprint test', 2, 2);

        const fingerprint = [
            navigator.userAgent,
            navigator.language,
            screen.width + 'x' + screen.height,
            new Date().getTimezoneOffset(),
            !!window.sessionStorage,
            !!window.localStorage,
            !!window.indexedDB,
            canvas.toDataURL()
        ].join('|');

        return btoa(fingerprint).substring(0, 32);
    }
}

/**
 * API客户端
 * 自动处理token刷新和请求重试
 */
class ApiClient {
    constructor(tokenManager) {
        this.tokenManager = tokenManager;
        this.baseURL = CONFIG.baseURL;
    }

    /**
     * 发送API请求
     */
    async request(url, options = {}) {
        const fullUrl = url.startsWith('http') ? url : `${this.baseURL}${url}`;

        // 检查是否需要刷新token
        if (this.tokenManager.isAccessTokenExpiringSoon()) {
            try {
                await this.tokenManager.refreshTokens();
            } catch (error) {
                console.warn('Token refresh failed, proceeding with current token:', error);
            }
        }

        const headers = {
            'Content-Type': 'application/json',
            ...options.headers
        };

        const accessToken = this.tokenManager.getAccessToken();
        if (accessToken) {
            headers['Authorization'] = `Bearer ${accessToken}`;
        }

        const requestOptions = {
            ...options,
            headers
        };

        let response = await fetch(fullUrl, requestOptions);

        // 如果返回401且有refresh token，尝试刷新后重试一次
        if (response.status === 401 && this.tokenManager.getRefreshToken()) {
            try {
                console.log('Access token expired, attempting refresh...');
                await this.tokenManager.refreshTokens();

                // 使用新的token重试请求
                const newAccessToken = this.tokenManager.getAccessToken();
                if (newAccessToken) {
                    headers['Authorization'] = `Bearer ${newAccessToken}`;
                    requestOptions.headers = headers;
                    response = await fetch(fullUrl, requestOptions);
                }
            } catch (refreshError) {
                console.error('Token refresh failed:', refreshError);
                // 如果刷新失败，清除tokens并抛出错误
                this.tokenManager.clearTokens();
                throw new Error('Authentication failed');
            }
        }

        if (!response.ok) {
            const errorData = await response.json().catch(() => ({}));
            throw new Error(errorData.message || `HTTP ${response.status}`);
        }

        return response.json();
    }

    /**
     * GET请求
     */
    async get(url, params) {
        const queryString = params ? '?' + new URLSearchParams(params) : '';
        return this.request(url + queryString);
    }

    /**
     * POST请求
     */
    async post(url, data) {
        return this.request(url, {
            method: 'POST',
            body: JSON.stringify(data)
        });
    }

    /**
     * PUT请求
     */
    async put(url, data) {
        return this.request(url, {
            method: 'PUT',
            body: JSON.stringify(data)
        });
    }

    /**
     * DELETE请求
     */
    async delete(url) {
        return this.request(url, {
            method: 'DELETE'
        });
    }
}

/**
 * 认证管理器
 * 处理登录、登出等认证相关操作
 */
class AuthManager {
    constructor(tokenManager, apiClient) {
        this.tokenManager = tokenManager;
        this.apiClient = apiClient;
    }

    /**
     * 用户登录
     */
    async login(username, password, role = 'PERSONAL_USER') {
        try {
            const response = await fetch(`${CONFIG.baseURL}/auth/login`, {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json'
                },
                body: JSON.stringify({
                    account: username,
                    password: password,
                    loginType: 'password',
                    expectedRole: role
                })
            });

            if (!response.ok) {
                const error = await response.json();
                throw new Error(error.message || 'Login failed');
            }

            const data = await response.json();
            if (data.code !== 200) {
                throw new Error(data.message || 'Login failed');
            }

            // 保存tokens
            const expiresAt = new Date(Date.now() + (data.data.expiresIn * 1000));
            this.tokenManager.saveTokens({
                accessToken: data.data.accessToken,
                refreshToken: data.data.refreshToken,
                expiresIn: data.data.expiresIn,
                refreshTokenExpiresIn: data.data.refreshTokenExpiresIn,
                expiresAt: expiresAt.toISOString(),
                tokenType: data.data.tokenType
            });

            // 初始化自动刷新
            this.tokenManager.initAutoRefresh();

            return data.data;
        } catch (error) {
            console.error('Login failed:', error);
            throw error;
        }
    }

    /**
     * 用户登出
     */
    async logout() {
        try {
            const refreshToken = this.tokenManager.getRefreshToken();
            if (refreshToken) {
                // 撤销refresh token
                await this.apiClient.request('/auth/revoke', {
                    method: 'POST',
                    headers: {
                        'Content-Type': 'application/x-www-form-urlencoded'
                    },
                    body: new URLSearchParams({
                        refreshToken: refreshToken
                    })
                });
            }
        } catch (error) {
            console.warn('Token revocation failed:', error);
        } finally {
            // 无论如何都要清除本地tokens
            this.tokenManager.clearTokens();
        }
    }

    /**
     * 检查登录状态
     */
    isLoggedIn() {
        return !!this.tokenManager.getAccessToken();
    }

    /**
     * 获取当前用户信息
     */
    async getCurrentUser() {
        return this.apiClient.get('/auth/userinfo');
    }
}

// 使用示例
document.addEventListener('DOMContentLoaded', function() {
    // 初始化Token管理器和API客户端
    const tokenManager = new TokenManager();
    const apiClient = new ApiClient(tokenManager);
    const authManager = new AuthManager(tokenManager, apiClient);

    // 暴露到全局作用域供其他脚本使用
    window.TokenManager = tokenManager;
    window.ApiClient = apiClient;
    window.AuthManager = authManager;

    console.log('Refresh Token客户端已初始化');
});

// 导出模块（如果使用模块系统）
if (typeof module !== 'undefined' && module.exports) {
    module.exports = {
        TokenManager,
        ApiClient,
        AuthManager,
        CONFIG
    };
}
