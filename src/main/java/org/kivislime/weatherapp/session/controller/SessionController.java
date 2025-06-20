package org.kivislime.weatherapp.session.controller;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.kivislime.weatherapp.session.service.SessionService;
import org.kivislime.weatherapp.session.dto.SessionDto;
import org.kivislime.weatherapp.session.exception.InvalidCredentialsException;
import org.kivislime.weatherapp.session.dto.LoginFormDto;
import org.kivislime.weatherapp.session.dto.SignUpFormDto;
import org.kivislime.weatherapp.user.exception.UserAlreadyExistsException;
import org.kivislime.weatherapp.security.CookieFactory;
import org.kivislime.weatherapp.security.CookieProperties;
import org.kivislime.weatherapp.user.service.UserService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

import java.util.Arrays;

@Slf4j
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
            log.error("Invalid username or password", ex);
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
            userService.registerUser(form.getLogin(), form.getPassword());
        } catch (UserAlreadyExistsException ex) {
            log.error("User already exists", ex);
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
        if (request.getSession().getId() != null) {
            Arrays.stream(request.getCookies())
                    .filter((x) -> x.getName().equals(cookieProperties.getCookieName()))
                    .findFirst()
                    .ifPresent(cookie -> {
                        String cookieToDelete = cookie.getValue();
                        sessionService.deleteSession(cookieToDelete);

                        Cookie expired = cookieFactory.deleteCookie(cookieToDelete);
                        response.addCookie(expired);
                    });
        }
        return "redirect:/sign-in";
    }
}
