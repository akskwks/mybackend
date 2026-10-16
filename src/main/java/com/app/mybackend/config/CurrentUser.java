package com.app.mybackend.config;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;

/**
*
* 클래스명 : CurrentUser
* 역할 : 현재 인증된 사용자의 식별 번호를 조회하는 구성 요소
* 생성일 : 2026-10-15
*
**/
@Component
public class CurrentUser {
    public Long number() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !(authentication.getPrincipal() instanceof Long userNum)) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "로그인이 필요합니다.");
        }
        return userNum;
    }
}
