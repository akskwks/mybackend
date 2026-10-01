package com.app.mybackend.main;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.servlet.view.RedirectView;

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
