package com.trip.utils;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.jwt.JWT;
import cn.hutool.jwt.JWTUtil;
import com.trip.constants.RedisLoginConstants;
import com.trip.dto.UserDTO;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.web.servlet.HandlerInterceptor;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import static com.trip.constants.SystemConstants.JWT_SECRET;

public class RefreshTokenInterceptor implements HandlerInterceptor {

    private StringRedisTemplate stringRedisTemplate;

    public RefreshTokenInterceptor(StringRedisTemplate stringRedisTemplate) {
        this.stringRedisTemplate = stringRedisTemplate;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String authHeader = request.getHeader("Authorization");
        if (StrUtil.isBlank(authHeader) || !authHeader.startsWith("Bearer ")) {
            return true;
        }

        String token = authHeader.substring(7);

        // Check Blacklist
        String blacklistKey = RedisLoginConstants.getBlacklistKey(token);
        if (Boolean.TRUE.equals(stringRedisTemplate.hasKey(blacklistKey))) {
            return true;
        }

        try {
            JWT jwt = JWTUtil.parseToken(token);
            // Verify Signature
            if (!jwt.setKey(JWT_SECRET).verify()) {
                return true;
            }

            // Check Expiration
            if (jwt.validate(0)) {
                // Valid and not expired
                UserDTO user = new UserDTO();
                Object id = jwt.getPayload("uid");
                Object nickname = jwt.getPayload("nickname");
                Object icon = jwt.getPayload("icon");
                if (id == null) {
                    return true;
                }
                user.setId(Long.valueOf(id.toString()));
                user.setNickName(nickname.toString());
                if (icon != null) {
                    user.setIcon(icon.toString());
                }
                UserHolder.saveUser(user);
                return true;
            } else {
                // Expired -> Try Refresh
                return tryRefresh(request, response);
            }

        } catch (Exception e) {
            return true;
        }
    }

    private boolean tryRefresh(HttpServletRequest request, HttpServletResponse response) {
        String refreshToken = request.getHeader("X-Refresh-Token");
        if (StrUtil.isBlank(refreshToken)) {
            return true;
        }

        String refreshKey = RedisLoginConstants.getRefreshTokenKey(refreshToken);
        Map<Object, Object> userMap = stringRedisTemplate.opsForHash().entries(refreshKey);
        if (userMap.isEmpty()) {
            return true;
        }

        UserDTO user = BeanUtil.fillBeanWithMap(userMap, new UserDTO(), false);
        
        // Generate new Access Token
        Map<String, Object> jwtPayload = new HashMap<>();
        jwtPayload.put("uid", user.getId());
        jwtPayload.put("nickname", user.getNickName());
        jwtPayload.put("icon", user.getIcon());
        jwtPayload.put("exp", (System.currentTimeMillis() / 1000) + RedisLoginConstants.LOGIN_USER_TTL.toSeconds());
        String newAccessToken = JWTUtil.createToken(jwtPayload, JWT_SECRET);

        // Add to Response Header
        response.setHeader("Authorization", "Bearer " + newAccessToken);
        // Also return the same refresh token (or rotate it if needed)
        response.setHeader("X-Refresh-Token", refreshToken);

        // Extend Refresh Token TTL
        stringRedisTemplate.expire(refreshKey, RedisLoginConstants.LOGIN_REFRESH_TTL.toMinutes(), TimeUnit.MINUTES);

        UserHolder.saveUser(user);
        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
        UserHolder.removeUser();
    }
}
