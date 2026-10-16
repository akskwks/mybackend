package com.app.mybackend;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
*
* 클래스명 : TestController
* 역할 : 백엔드 동작 확인용 테스트 API를 제공하는 컨트롤러
* 생성일 : 2026-09-17
*
**/
@RestController
@RequestMapping("/api")
public class TestController {

    @GetMapping("/test")
    public String test() {
        return "Hello World";
    }
}
