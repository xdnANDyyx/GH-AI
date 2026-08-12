package com.ruoyi.system.mapper;

import java.util.List;
import com.ruoyi.system.domain.UserRegisterInfo;

/**
 * 用户注册信息Mapper接口
 * 
 * @author ruoyi
 * @date 2026-08-01
 */
public interface UserRegisterInfoMapper {
    /**
     * 查询用户注册信息
     * 
     * @param id 用户注册信息主键
     * @return 用户注册信息
     */
    public UserRegisterInfo selectUserRegisterInfoById(Long id);

    /**
     * 根据用户ID查询注册信息
     * 
     * @param userId 用户ID
     * @return 用户注册信息
     */
    public UserRegisterInfo selectUserRegisterInfoByUserId(Long userId);

    /**
     * 查询用户注册信息列表
     * 
     * @param userRegisterInfo 用户注册信息
     * @return 用户注册信息集合
     */
    public List<UserRegisterInfo> selectUserRegisterInfoList(UserRegisterInfo userRegisterInfo);

    /**
     * 新增用户注册信息
     * 
     * @param userRegisterInfo 用户注册信息
     * @return 结果
     */
    public int insertUserRegisterInfo(UserRegisterInfo userRegisterInfo);

    /**
     * 修改用户注册信息
     * 
     * @param userRegisterInfo 用户注册信息
     * @return 结果
     */
    public int updateUserRegisterInfo(UserRegisterInfo userRegisterInfo);

    /**
     * 删除用户注册信息
     * 
     * @param id 用户注册信息主键
     * @return 结果
     */
    public int deleteUserRegisterInfoById(Long id);

    /**
     * 批量删除用户注册信息
     * 
     * @param ids 需要删除的数据主键集合
     * @return 结果
     */
    public int deleteUserRegisterInfoByIds(Long[] ids);
}