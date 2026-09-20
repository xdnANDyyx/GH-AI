package com.ruoyi.framework.security.filter;

import java.io.IOException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import com.ruoyi.common.constant.CacheConstants;
import com.ruoyi.common.core.domain.model.LoginUser;
import com.ruoyi.common.core.redis.RedisCache;
import com.ruoyi.common.utils.SecurityUtils;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.framework.web.service.CustomerTokenService;
import com.ruoyi.framework.web.service.TokenService;

/**
 * token过滤器 验证token有效性
 * 支持管理端和客户端双Token体系：
 * - 管理端接口：使用TokenService验证Token
 * - 客户端接口：使用CustomerTokenService验证Token
 * 
 * @author ruoyi
 */
@Component
public class JwtAuthenticationTokenFilter extends OncePerRequestFilter {
    
    private static final String CUSTOMER_PATH_PREFIX = "/customer/";
    private static final String API_CUSTOMER_PATH_PREFIX = "/api/customer/";
    // CanvasEditorController (/ai/image/**) 也使用客户端Token
    // 同时兼容 context-path=/api 的生产环境（实际路径为 /api/ai/image/）
    private static final String CANVAS_EDITOR_PATH_PREFIX = "/ai/image/";
    private static final String API_CANVAS_EDITOR_PATH_PREFIX = "/api/ai/image/";
    
    @Autowired
    private TokenService tokenService;
    
    @Autowired
    private CustomerTokenService customerTokenService;
    
    @Autowired
    private RedisCache redisCache;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {
        LoginUser loginUser = getLoginUser(request);
        if (StringUtils.isNotNull(loginUser) && StringUtils.isNull(SecurityUtils.getAuthentication())){
            verifyToken(loginUser, request);
            UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(loginUser, null, loginUser.getAuthorities());
            authenticationToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
            SecurityContextHolder.getContext().setAuthentication(authenticationToken);
        }
        chain.doFilter(request, response);
    }
    
    /**
     * 根据请求路径获取对应的登录用户信息
     * 客户端路径优先使用CustomerTokenService，若未找到则回退到TokenService（兼容管理端登录用户）
     */
    private LoginUser getLoginUser(HttpServletRequest request) {
        String requestURI = request.getRequestURI();
        if (isCustomerPath(requestURI)) {
            // 优先尝试客户端 Token（customer_login_tokens: 前缀）
            LoginUser user = customerTokenService.getLoginUser(request);
            if (user != null) {
                return user;
            }
            // 回退到管理端 Token（login_tokens: 前缀）
            // 用户可能通过 /login 或 /login/sms 管理端入口登录
            return tokenService.getLoginUser(request);
        }
        return tokenService.getLoginUser(request);
    }
    
    /**
     * 判断是否为客户端路径
     */
    private boolean isCustomerPath(String requestURI) {
        return requestURI.startsWith(CUSTOMER_PATH_PREFIX)
            || requestURI.startsWith(API_CUSTOMER_PATH_PREFIX)
            || requestURI.startsWith(CANVAS_EDITOR_PATH_PREFIX)
            || requestURI.startsWith(API_CANVAS_EDITOR_PATH_PREFIX);
    }
    
    /**
     * 根据请求路径验证Token
     * 客户端路径根据实际使用的TokenService进行验证
     */
    private void verifyToken(LoginUser loginUser, HttpServletRequest request) {
        // LoginUser 的 token 来源决定了使用哪个 TokenService 验证
        // 通过检查 Redis 中 token 的存储 key 来判断
        String userKey = CacheConstants.LOGIN_TOKEN_KEY + loginUser.getToken();
        if (redisCache.hasKey(userKey)) {
            tokenService.verifyToken(loginUser);
        } else {
            customerTokenService.verifyToken(loginUser);
        }
    }
}
