package com.ruoyi.common.filter;

import java.io.IOException;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import com.ruoyi.common.constant.Constants;
import com.ruoyi.common.utils.StringUtils;
import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.FilterConfig;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

public class ApiRequestLogFilter implements Filter {
    private static final Logger log = LoggerFactory.getLogger(ApiRequestLogFilter.class);

    private static final String[] EXCLUDE_URLS = {
        "/swagger-ui", "/v3/api-docs", "/doc.html", "/webjars/", "/druid/",
        "/actuator/", "/favicon.ico", "/error"
    };

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;

        String requestUri = httpRequest.getRequestURI();
        if (isExcludeUrl(requestUri)) {
            chain.doFilter(request, response);
            return;
        }

        long startTime = System.currentTimeMillis();
        String method = httpRequest.getMethod();
        String queryString = httpRequest.getQueryString();
        String contentType = httpRequest.getContentType();

        Map<String, String> headers = getRequestHeaders(httpRequest);

        HttpServletRequest wrappedRequest = httpRequest;
        String requestBody = null;

        if (httpRequest instanceof RepeatedlyRequestWrapper) {
            RepeatedlyRequestWrapper wrapper = (RepeatedlyRequestWrapper) httpRequest;
            requestBody = new String(wrapper.getBody(), Constants.UTF8);
        } else if (StringUtils.startsWithIgnoreCase(contentType, MediaType.APPLICATION_JSON_VALUE)) {
            RepeatedlyRequestWrapper wrapper = new RepeatedlyRequestWrapper(httpRequest, response);
            requestBody = new String(wrapper.getBody(), Constants.UTF8);
            wrappedRequest = wrapper;
        }

        log.info("================ API Request Start ================");
        log.info("URI         : {}", requestUri);
        log.info("Method      : {}", method);
        log.info("Query       : {}", queryString);
        log.info("ContentType : {}", contentType);
//        log.info("Client IP   : {}", getIpAddress(httpRequest));
        log.info("Headers     : {}", headers);
        if (StringUtils.isNotEmpty(requestBody)) {
            log.info("Body        : {}", truncateBody(requestBody, 2000));
        }

        try {
            chain.doFilter(wrappedRequest, response);
        } finally {
            long endTime = System.currentTimeMillis();
            long duration = endTime - startTime;
            int status = httpResponse.getStatus();

            log.info("Status      : {}", status);
            log.info("Duration    : {} ms", duration);
            log.info("================ API Request End ==================");
        }
    }

    @Override
    public void destroy() {
    }

    private boolean isExcludeUrl(String uri) {
        for (String exclude : EXCLUDE_URLS) {
            if (uri.contains(exclude)) {
                return true;
            }
        }
        return false;
    }

    private Map<String, String> getRequestHeaders(HttpServletRequest request) {
        Map<String, String> headers = new HashMap<>();
        Enumeration<String> headerNames = request.getHeaderNames();
        while (headerNames.hasMoreElements()) {
            String name = headerNames.nextElement();
            if (!"authorization".equalsIgnoreCase(name) && !"cookie".equalsIgnoreCase(name)) {
                headers.put(name, request.getHeader(name));
            }
        }
        return headers;
    }

    private String getIpAddress(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (StringUtils.isEmpty(ip) || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("Proxy-Client-IP");
        }
        if (StringUtils.isEmpty(ip) || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("WL-Proxy-Client-IP");
        }
        if (StringUtils.isEmpty(ip) || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("HTTP_CLIENT_IP");
        }
        if (StringUtils.isEmpty(ip) || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("HTTP_X_FORWARDED_FOR");
        }
        if (StringUtils.isEmpty(ip) || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        return ip;
    }

    private String truncateBody(String body, int maxLength) {
        if (body == null) {
            return null;
        }
        if (body.length() <= maxLength) {
            return body;
        }
        return body.substring(0, maxLength) + "... (truncated)";
    }
}
