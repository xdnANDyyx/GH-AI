package com.ruoyi.system.domain;

import java.io.Serializable;
import java.time.LocalDateTime;
import lombok.Data;

@Data
public class SmsSendRecord implements Serializable {
    private static final long serialVersionUID = 1L;

    private Long id;

    private String phone;

    private String scene;

    private String signName;

    private String templateCode;

    private String templateParam;

    private String providerRequestId;

    private String providerBizId;

    private String providerCode;

    private String providerMessage;

    private Integer success;

    private LocalDateTime sendTime;

    private LocalDateTime createTime;
}

