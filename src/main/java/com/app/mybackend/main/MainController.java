package com.app.mybackend.main;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.servlet.view.RedirectView;

/**
*
* 클래스명 : MainController
* 역할 : 백엔드 루트 요청을 프론트엔드 메인 화면으로 연결하는 컨트롤러
* 생성일 : 2026-09-17
*
**/
@Controller
public class MainController {

    private final String frontendBaseUrl;

    public MainController(
            @Value("${myapp.frontend-base-url:http://localhost:5173}") String frontendBaseUrl
    ) {
        this.frontendBaseUrl = frontendBaseUrl.replaceAll("/+$", "");
    }

    @GetMapping({"/", "/main", "/main/"})
    public RedirectView main() {
        return new RedirectView(frontendBaseUrl + "/#/");
    }
}
