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
import com.ruoyi.common.core.domain.model.LoginUser;
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
    
    @Autowired
    private TokenService tokenService;
    
    @Autowired
    private CustomerTokenService customerTokenService;

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
     * 客户端路径使用CustomerTokenService，其他路径使用TokenService
     */
    private LoginUser getLoginUser(HttpServletRequest request) {
        String requestURI = request.getRequestURI();
        if (requestURI.startsWith(CUSTOMER_PATH_PREFIX) || requestURI.startsWith(API_CUSTOMER_PATH_PREFIX)) {
            return customerTokenService.getLoginUser(request);
        }
        return tokenService.getLoginUser(request);
    }
    
    /**
     * 根据请求路径验证Token
     */
    private void verifyToken(LoginUser loginUser, HttpServletRequest request) {
        String requestURI = request.getRequestURI();
        if (requestURI.startsWith(CUSTOMER_PATH_PREFIX) || requestURI.startsWith(API_CUSTOMER_PATH_PREFIX)) {
            customerTokenService.verifyToken(loginUser);
        } else {
            tokenService.verifyToken(loginUser);
        }
    }
}
