package pl.kmolski.hydrohomie.account.controller;

import lombok.RequiredArgsConstructor;
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

/**
 * User-accessible controller providing account self-management.
 */
@Controller
@RequestMapping("/user")
@RequiredArgsConstructor
public class UserChangePasswordController {

    private final UserService userService;

    /**
     * Populate and show the user's change own password form.
     *
     * @param model the template model
     * @param changeUserPasswordDto new DTO for the password change
     * @return the change password form
     */
    @GetMapping("/changePassword")
    public String changePasswordForm(Model model, ChangeUserPasswordDto changeUserPasswordDto) {
        return "user_change_password";
    }

    /**
     * Change the user's own password to the one specified in the change password form.
     *
     * @param model the template model
     * @param changePasswordDto DTO for the password change
     * @param result the result of DTO binding (includes validation errors)
     * @param authentication the current user's authentication object
     * @return the success page or form with errors
     */
    @PostMapping("/changePassword")
    public Mono<String> changePasswordAction(Model model, @Valid ChangeUserPasswordDto changePasswordDto,
                                             BindingResult result, Authentication authentication) {
        if (result.hasErrors()) {
            return Mono.just("user_change_password");
        }
        model.addAttribute("redirect", "/user");
        return userService.updatePassword(authentication.getName(), changePasswordDto.getPassword())
                .map(account -> {
                    var message = "Successfully changed password.";
                    model.addAttribute("message", message);
                    return "user_success";
                });
    }
}
