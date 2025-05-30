package org.kivislime.weather.controller;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.kivislime.weather.dto.LoginFormDto;
import org.kivislime.weather.dto.SignUpFormDto;
import org.kivislime.weather.exception.UserAlreadyExistsException;
import org.kivislime.weather.security.CookieFactory;
import org.kivislime.weather.security.CookieProperties;
import org.kivislime.weather.dto.SessionDto;
import org.kivislime.weather.exception.InvalidCredentialsException;
import org.kivislime.weather.service.SessionService;
import org.kivislime.weather.dto.UserDto;
import org.kivislime.weather.service.UserService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

import java.util.Arrays;

@Controller
@RequiredArgsConstructor
public class SessionController {
    private final SessionService sessionService;
    private final UserService userService;
    private final CookieFactory cookieFactory;
    private final CookieProperties cookieProperties;

    @GetMapping({"/", "/sign-in"})
    public String showLoginForm(Model model) {
        model.addAttribute("loginForm", new LoginFormDto());
        return "sign-in";
    }

    //TODO: защита от XSS-атак?
    @PostMapping("/sign-in")
    public String login(@Valid @ModelAttribute("loginForm") LoginFormDto form,
                        BindingResult bindingResult,
                        Model model,
                        HttpServletResponse response
    ) {
        if (bindingResult.hasErrors()) {
            return "sign-in";
        }

        try {
            SessionDto sessionDto = sessionService.createSession(form.getLogin(), form.getPassword());
            Cookie cookie = cookieFactory.createCookie(sessionDto.getId().toString());
            response.addCookie(cookie);
            return "redirect:/locations";
        } catch (InvalidCredentialsException ex) {
            model.addAttribute("loginError", "Invalid username or password");
            return "sign-in";
        }
    }

    @GetMapping("/sign-up")
    public String showSignUpForm(Model model) {
        model.addAttribute("signUpForm", new SignUpFormDto());
        return "sign-up";
    }

    @PostMapping("/sign-up")
    public String registration(
            @Valid @ModelAttribute("signUpForm") SignUpFormDto form,
            BindingResult bindingResult
    ) {
        if (bindingResult.hasErrors()) {
            return "sign-up";
        }

        if (!form.getPassword().equals(form.getConfirmPassword())) {
            bindingResult.rejectValue(
                    "confirmPassword",
                    "password.mismatch",
                    "Passwords do not match"
            );
            return "sign-up";
        }

        try {
            userService.registrationUser(form.getLogin(), form.getPassword());
        } catch (UserAlreadyExistsException ex) {
            bindingResult.rejectValue(
                    "login",
                    "login.exists",
                    "Username is already taken"
            );
            return "sign-up";
        }

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

        sessionService.deleteSession(uuidCookie);
        Cookie deleteCookie = cookieFactory.createCookie(uuidCookie);
        response.addCookie(deleteCookie);

        return "redirect:/sign-in";
    }
}
