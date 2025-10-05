package com.trip.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.trip.dto.LoginFormDTO;
import com.trip.dto.Result;
import com.trip.entity.User;

import jakarta.servlet.http.HttpSession;
import jakarta.servlet.http.HttpServletRequest;

public interface IUserService extends IService<User> {

    Result sendCode(String phone, HttpSession session);

    Result login(LoginFormDTO loginForm, HttpSession session);

    Result logout(HttpServletRequest request);

    Result sign();

    Result signCount();

}
