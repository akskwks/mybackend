package com.app.mybackend.user.service;

import com.app.mybackend.user.entity.User;
import com.app.mybackend.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public User register(String userId, String password, String passwordConfirm, String userName) {
        if (blank(userId) || blank(password) || blank(userName)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "아이디, 비밀번호, 사용자명을 입력해 주세요.");
        }

        if (!password.equals(passwordConfirm)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "비밀번호가 일치하지 않습니다.");
        }

        String normalizedId = userId.trim();
        if (userRepository.existsByUserId(normalizedId)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "이미 사용 중인 아이디입니다.");
        }

        User user = new User();
        user.setUserId(normalizedId);
        user.setPassword(passwordEncoder.encode(password));
        user.setUserName(userName);
        user.setRole("USER");
        user.setUseYn("Y");

        try {
            return userRepository.saveAndFlush(user);
        } catch (DataIntegrityViolationException e) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "이미 사용 중인 아이디입니다.", e);
        }

    }

    @Transactional(readOnly = true)
    public User authenicate(String userId, String password) {
        if (blank(userId) || blank(password)) {
            throw invalidCredentials();
        }

        User user = userRepository.findByUserId(userId.trim())
                .orElseThrow(this::invalidCredentials);
        if (!passwordEncoder.matches(password, user.getPassword())) {
            throw invalidCredentials();
        }

        if (!"Y".equals(user.getUseYn())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                    "사용할 수 없는 계정입니다.");
        }

        return user;
    }

    @Transactional(readOnly = true)
    public User findById(long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.UNAUTHORIZED, "로그인이 필요합니다."));
        if (!"Y".equals(user.getUseYn())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                    "사용할 수 없는 계정입니다.");
        }
        return user;
    }

    private ResponseStatusException invalidCredentials() {
        return new ResponseStatusException(HttpStatus.UNAUTHORIZED,
                "아이디 또는 비밀번호가 올바르지 않습니다.");
    }

    private boolean blank(String value) {
        return value == null || value.isBlank();
    }
}
