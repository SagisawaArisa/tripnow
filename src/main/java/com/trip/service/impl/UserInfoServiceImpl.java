package com.trip.service.impl;

import com.trip.entity.UserInfo;
import com.trip.mapper.UserInfoMapper;
import com.trip.service.IUserInfoService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

@Service
public class UserInfoServiceImpl extends ServiceImpl<UserInfoMapper, UserInfo> implements IUserInfoService {

}
