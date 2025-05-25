package org.kivislime.weather;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Arrays;

@Slf4j
@Controller
@RequiredArgsConstructor
public class SessionController {
    private final SessionService sessionService;
    private final UserService userService;
    private final CookieFactory cookieFactory;
    private final CookieProperties cookieProperties;

    @GetMapping("/")
    public String showLoginForm() {
        return "sign-in";
    }

    @GetMapping("/sign-in")
    public String showLoginFormAlias() {
        return "sign-in";
    }

    //TODO: защита от XSS-атак?
    @PostMapping("/sign-in")
    public String login(@RequestParam("login") String login,
                        @RequestParam("password") String password,
                        HttpServletResponse response) {
        SessionDto sessionDto = sessionService.createSession(login, password);

        Cookie cookie = cookieFactory.createCookie(sessionDto.getId().toString());
        response.addCookie(cookie);

        return "redirect:/locations";
    }

    //TODO: совместить get & post типа if (login & password == null) return "sign-up"; И остальные тоже наверное то?
    @GetMapping("/sign-up")
    public String showSignUpForm() {
        return "sign-up";
    }

    @PostMapping("/sign-up")
    public String registration(@RequestParam("login") String login,
                               @RequestParam("password") String password,
                               @RequestParam("confirmPassword") String confirmPassword) {
//TODO: доделат
//        if (!password.equals(confirmPassword)) {
//            // Если хотите показать сообщение об ошибке, можно:
//            // model.addAttribute("error", "Пароли не совпадают");
//            // return "sign-up";
//            return "redirect:/sign-up?error=passwordMismatch";
//        }

        UserDto userDto = userService.registrationUser(login, password);

        return "redirect:/sign-in";
    }

    @PostMapping("/logout")
    public String logout(HttpServletRequest request,
                         HttpServletResponse response) {
        //TODO: null -> exception?
        String uuidCookie = Arrays.stream(request.getCookies())
                .filter((x) -> x.getName().equals(cookieProperties.getCookieName()))
                .map(Cookie::getValue)
                .findFirst()
                .orElse(null);

        boolean isDeleted = sessionService.deleteSession(uuidCookie);
        Cookie deleteCookie = cookieFactory.createCookie(uuidCookie);
        response.addCookie(deleteCookie);

        return "redirect:/sign-in";
    }
}
