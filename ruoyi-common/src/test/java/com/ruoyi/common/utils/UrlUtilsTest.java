package com.ruoyi.common.utils;

import com.ruoyi.common.config.ServerConfig;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class UrlUtilsTest {

    private final UrlUtils urlUtils = new UrlUtils();

    @AfterEach
    void tearDown() {
        urlUtils.setServerConfig(null);
    }

    @Test
    void getFullUrlShouldDropApiContextForProfileResources() {
        ServerConfig serverConfig = new ServerConfig() {
            @Override
            public String getUrl() {
                return "http://guanghe3d.com/api";
            }
        };
        urlUtils.setServerConfig(serverConfig);

        String fullUrl = UrlUtils.getFullUrl("/profile/upload/2026/06/27/avatar.jpg");

        assertEquals("http://guanghe3d.com/profile/upload/2026/06/27/avatar.jpg", fullUrl);
    }
}
