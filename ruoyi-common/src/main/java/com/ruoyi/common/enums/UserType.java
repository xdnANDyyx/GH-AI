package com.ruoyi.common.enums;

/**
 * 用户类型枚举
 * 
 * @author ruoyi
 */
public enum UserType {
    
    ADMIN("1", "系统管理员"),
    CUSTOMER("2", "客户");
    
    private final String code;
    private final String info;
    
    UserType(String code, String info) {
        this.code = code;
        this.info = info;
    }
    
    public String getCode() {
        return code;
    }
    
    public String getInfo() {
        return info;
    }
    
    public static UserType fromCode(String code) {
        for (UserType type : UserType.values()) {
            if (type.getCode().equals(code)) {
                return type;
            }
        }
        return null;
    }
    
    public static boolean isAdmin(String code) {
        return ADMIN.getCode().equals(code);
    }
    
    public static boolean isCustomer(String code) {
        return CUSTOMER.getCode().equals(code);
    }
}
