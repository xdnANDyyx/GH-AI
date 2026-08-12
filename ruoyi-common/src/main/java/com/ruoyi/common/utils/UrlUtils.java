package com.ruoyi.common.utils;

import com.ruoyi.common.config.ServerConfig;
import com.ruoyi.common.constant.Constants;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class UrlUtils {

    private static final String API_CONTEXT_PATH = "/api";

    private static ServerConfig serverConfig;

    @Autowired
    public void setServerConfig(ServerConfig serverConfig) {
        UrlUtils.serverConfig = serverConfig;
    }

    public static String getFullUrl(String path) {
        if (path == null || path.isEmpty()) {
            return path;
        }
        if (path.startsWith("http://") || path.startsWith("https://")) {
            return path;
        }
        String domain = getDomain();
        if (domain == null || domain.isEmpty()) {
            return path;
        }
        domain = normalizeDomain(domain, path);
        if (domain.endsWith("/")) {
            domain = domain.substring(0, domain.length() - 1);
        }
        if (!path.startsWith("/")) {
            path = "/" + path;
        }
        return domain + path;
    }

    private static String getDomain() {
        if (serverConfig != null) {
            return serverConfig.getUrl();
        }
        return null;
    }

    private static String normalizeDomain(String domain, String path) {
        if (path != null && path.startsWith(Constants.RESOURCE_PREFIX + "/") && domain.endsWith(API_CONTEXT_PATH)) {
            return domain.substring(0, domain.length() - API_CONTEXT_PATH.length());
        }
        return domain;
    }
}
