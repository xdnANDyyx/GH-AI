package com.ruoyi.system.service.impl;

import java.time.LocalDateTime;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.ruoyi.system.domain.UserRegisterInfo;
import com.ruoyi.system.mapper.UserRegisterInfoMapper;
import com.ruoyi.system.service.IUserRegisterInfoService;

/**
 * 用户注册信息Service业务层处理
 * 
 * @author ruoyi
 * @date 2026-08-01
 */
@Service
public class UserRegisterInfoServiceImpl implements IUserRegisterInfoService {
    @Autowired
    private UserRegisterInfoMapper userRegisterInfoMapper;

    /**
     * 查询用户注册信息
     * 
     * @param id 用户注册信息主键
     * @return 用户注册信息
     */
    @Override
    public UserRegisterInfo selectUserRegisterInfoById(Long id) {
        return userRegisterInfoMapper.selectUserRegisterInfoById(id);
    }

    /**
     * 根据用户ID查询注册信息
     * 
     * @param userId 用户ID
     * @return 用户注册信息
     */
    @Override
    public UserRegisterInfo selectUserRegisterInfoByUserId(Long userId) {
        return userRegisterInfoMapper.selectUserRegisterInfoByUserId(userId);
    }

    /**
     * 查询用户注册信息列表
     * 
     * @param userRegisterInfo 用户注册信息
     * @return 用户注册信息集合
     */
    @Override
    public List<UserRegisterInfo> selectUserRegisterInfoList(UserRegisterInfo userRegisterInfo) {
        return userRegisterInfoMapper.selectUserRegisterInfoList(userRegisterInfo);
    }

    /**
     * 新增用户注册信息
     * 
     * @param userRegisterInfo 用户注册信息
     * @return 结果
     */
    @Override
    public int insertUserRegisterInfo(UserRegisterInfo userRegisterInfo) {
        userRegisterInfo.setCreateTime(LocalDateTime.now());
        userRegisterInfo.setUpdateTime(LocalDateTime.now());
        return userRegisterInfoMapper.insertUserRegisterInfo(userRegisterInfo);
    }

    /**
     * 修改用户注册信息
     * 
     * @param userRegisterInfo 用户注册信息
     * @return 结果
     */
    @Override
    public int updateUserRegisterInfo(UserRegisterInfo userRegisterInfo) {
        userRegisterInfo.setUpdateTime(LocalDateTime.now());
        return userRegisterInfoMapper.updateUserRegisterInfo(userRegisterInfo);
    }

    /**
     * 批量删除用户注册信息
     * 
     * @param ids 需要删除的用户注册信息主键集合
     * @return 结果
     */
    @Override
    public int deleteUserRegisterInfoByIds(Long[] ids) {
        return userRegisterInfoMapper.deleteUserRegisterInfoByIds(ids);
    }

    /**
     * 删除用户注册信息
     * 
     * @param id 用户注册信息主键
     * @return 结果
     */
    @Override
    public int deleteUserRegisterInfoById(Long id) {
        return userRegisterInfoMapper.deleteUserRegisterInfoById(id);
    }

    /**
     * 保存或更新用户注册信息
     * 
     * @param userRegisterInfo 用户注册信息
     * @return 结果
     */
    @Override
    public int saveOrUpdateUserRegisterInfo(UserRegisterInfo userRegisterInfo) {
        UserRegisterInfo existing = userRegisterInfoMapper.selectUserRegisterInfoByUserId(userRegisterInfo.getUserId());
        if (existing != null) {
            userRegisterInfo.setId(existing.getId());
            return userRegisterInfoMapper.updateUserRegisterInfo(userRegisterInfo);
        } else {
            return userRegisterInfoMapper.insertUserRegisterInfo(userRegisterInfo);
        }
    }
}