package com.trip.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.bean.copier.CopyOptions;
import cn.hutool.core.util.RandomUtil;
import cn.hutool.jwt.JWT;
import cn.hutool.jwt.JWTUtil;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.github.yitter.idgen.YitIdHelper;
import com.trip.dto.LoginFormDTO;
import com.trip.dto.Result;
import com.trip.dto.UserDTO;
import com.trip.entity.User;
import com.trip.mapper.UserMapper;
import com.trip.service.IUserService;
import com.trip.utils.RegexUtils;
import com.trip.utils.UserHolder;
import com.trip.constants.RedisLoginConstants;
import com.trip.constants.RedisUserConstants;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.connection.BitFieldSubCommands;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import static com.trip.constants.SystemConstants.JWT_SECRET;
import static com.trip.constants.SystemConstants.USER_NICK_NAME_PREFIX;

@Slf4j
@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements IUserService {

    @Resource
    private StringRedisTemplate stringRedisTemplate;

    @Override
    public Result sendCode(String phone, HttpSession session) {
        if (RegexUtils.isPhoneInvalid(phone)) {
            return Result.fail("请输入有效的手机号码");
        }
        String code = RandomUtil.randomNumbers(6);

        stringRedisTemplate.opsForValue().set(RedisLoginConstants.getLoginCodeKey(phone), code, RedisLoginConstants.LOGIN_CODE_TTL.toMinutes(), TimeUnit.MINUTES);

        log.debug("发送短信验证码成功，验证码：{}", code);
        return Result.ok();
    }

    @Override
    public Result login(LoginFormDTO loginForm, HttpSession session) {
        String phone = loginForm.getPhone();
        if (RegexUtils.isPhoneInvalid(phone)) {
            return Result.fail("手机号格式错误！");
        }
        String cacheCode = stringRedisTemplate.opsForValue().get(RedisLoginConstants.getLoginCodeKey(phone));
        String code = loginForm.getCode();
        if (cacheCode == null || !cacheCode.equals(code)) {
            return Result.fail("验证码错误");
        }

        User user = query().eq("phone", phone).one();

        if (user == null) {
            try{
                user = createUserWithPhone(phone);
            }catch (Exception e){
                return Result.fail(e.getMessage());
            }
        }

        Map<String, Object> jwtPayload = new HashMap<>();
        jwtPayload.put("uid", user.getId());
        jwtPayload.put("nickname", user.getNickName());
        jwtPayload.put("icon", user.getIcon());
        // JWT Expiration set to Access Token TTL
        jwtPayload.put("exp", (System.currentTimeMillis() / 1000) + RedisLoginConstants.LOGIN_USER_TTL.toSeconds());
        String accessToken = JWTUtil.createToken(jwtPayload, JWT_SECRET);

        // Refresh Token
        String refreshToken = cn.hutool.core.util.IdUtil.simpleUUID();
        UserDTO userDTO = BeanUtil.copyProperties(user, UserDTO.class);
        Map<String, Object> userMap = BeanUtil.beanToMap(userDTO, new HashMap<>(),
                CopyOptions.create()
                        .setIgnoreNullValue(true)
                        .setFieldValueEditor((fieldName, fieldValue) -> fieldValue.toString()));
        
        String refreshKey = RedisLoginConstants.getRefreshTokenKey(refreshToken);
        stringRedisTemplate.opsForHash().putAll(refreshKey, userMap);
        stringRedisTemplate.expire(refreshKey, RedisLoginConstants.LOGIN_REFRESH_TTL.toMinutes(), TimeUnit.MINUTES);

        Map<String, String> tokens = new HashMap<>();
        tokens.put("accessToken", accessToken);
        tokens.put("refreshToken", refreshToken);

        return Result.ok(tokens);
    }

    @Override
    public Result logout(HttpServletRequest request) {
        // Block Access Token
        String authHeader = request.getHeader("Authorization");
        if (cn.hutool.core.util.StrUtil.isNotBlank(authHeader) && authHeader.startsWith("Bearer ")) {
            String accessToken = authHeader.substring(7);
            try {
                JWT jwt = JWTUtil.parseToken(accessToken);
                Object expObj = jwt.getPayload("exp");
                if (expObj != null) {
                    long exp = Long.parseLong(expObj.toString());
                    long now = System.currentTimeMillis() / 1000;
                    long ttl = exp - now;
                    if (ttl > 0) {
                        stringRedisTemplate.opsForValue().set(
                                RedisLoginConstants.getBlacklistKey(accessToken),
                                "1",
                                ttl,
                                TimeUnit.SECONDS
                        );
                    }
                }
            } catch (Exception e) {
                log.warn("Logout: Failed to parse access token", e);
            }
        }

        // Delete Refresh Token
        String refreshToken = request.getHeader("X-Refresh-Token");
        if (refreshToken != null) {
            String refreshKey = RedisLoginConstants.getRefreshTokenKey(refreshToken);
            stringRedisTemplate.delete(refreshKey);
        }
        UserHolder.removeUser();
        return Result.ok();
    }

    @Override
    public Result sign() {
        Long userId = UserHolder.getUser().getId();
        LocalDateTime now = LocalDateTime.now();
        String keySuffix = now.format(DateTimeFormatter.ofPattern(":yyyyMM"));
        String key = RedisUserConstants.getUserSignKey(userId, keySuffix);
        int dayOfMonth = now.getDayOfMonth();
        stringRedisTemplate.opsForValue().setBit(key, dayOfMonth - 1, true);
        return Result.ok();
    }

    @Override
    public Result signCount() {
        Long userId = UserHolder.getUser().getId();
        LocalDateTime now = LocalDateTime.now();
        String keySuffix = now.format(DateTimeFormatter.ofPattern(":yyyyMM"));
        String key = RedisUserConstants.getUserSignKey(userId, keySuffix);
        int dayOfMonth = now.getDayOfMonth();
        List<Long> result = stringRedisTemplate.opsForValue().bitField(
                key,
                BitFieldSubCommands.create()
                        .get(BitFieldSubCommands.BitFieldType.unsigned(dayOfMonth)).valueAt(0)
        );
        if (result == null || result.isEmpty()) {
            return Result.ok(0);
        }
        Long num = result.get(0);
        if (num == null || num == 0) {
            return Result.ok(0);
        }
        int count = 0;
        while (true) {
            if ((num & 1) == 0) {
                break;
            }else {
                count++;
            }
            num >>>= 1;
        }
        return Result.ok(count);
    }

    private User createUserWithPhone(String phone) {
        User user = new User();
        user.setId(YitIdHelper.nextId());
        user.setPhone(phone);
        user.setNickName(USER_NICK_NAME_PREFIX + RandomUtil.randomString(10));
        boolean success = save(user);
        if (!success) {
            throw new RuntimeException("系统繁忙，请稍后重试");
        }
        return user;
    }
}
