package pl.kmolski.hydrohomie.account.controller;

import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import pl.kmolski.hydrohomie.account.dto.ChangeUserPasswordDto;
import pl.kmolski.hydrohomie.account.service.UserService;
import reactor.core.publisher.Mono;

import javax.validation.Valid;

@Controller
@RequestMapping("/user")
@RequiredArgsConstructor
public class UserChangePasswordController {

    private static final Logger LOGGER = LoggerFactory.getLogger(UserChangePasswordController.class);

    private final UserService userService;

    @GetMapping("/changePassword")
    public String changePasswordForm(Model model, ChangeUserPasswordDto changeUserPasswordDto) {
        return "user_change_password";
    }

    @PostMapping("/changePassword")
    public Mono<String> changePasswordAction(Model model, @Valid ChangeUserPasswordDto changePasswordDto,
                                             BindingResult result, Authentication authentication) {
        if (result.hasErrors()) {
            return Mono.just("user_change_password");
        }
        model.addAttribute("redirect", "/user");
        return userService.updateUserPassword(authentication.getName(), changePasswordDto.getPassword())
                .map(account -> {
                    var message = "Successfully changed password.";
                    model.addAttribute("message", message);
                    return "user_success";
                });
    }
}
