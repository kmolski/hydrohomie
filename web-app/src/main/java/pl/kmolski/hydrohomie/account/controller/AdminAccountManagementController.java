package pl.kmolski.hydrohomie.account.controller;

import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.annotation.*;
import pl.kmolski.hydrohomie.account.dto.ChangeUserPasswordDto;
import pl.kmolski.hydrohomie.account.dto.NewUserDto;
import pl.kmolski.hydrohomie.account.service.UserService;
import pl.kmolski.hydrohomie.webmvc.util.PaginationUtil;
import reactor.core.publisher.Mono;

import javax.validation.Valid;

@Controller
@RequestMapping("/admin")
@RequiredArgsConstructor
public class AdminAccountManagementController {

    private static final Logger LOGGER = LoggerFactory.getLogger(AdminAccountManagementController.class);

    private final UserService userService;

    @GetMapping("/createUser")
    public String createUserForm(NewUserDto newUserDto) {
        return "admin_create_user";
    }

    @PostMapping("/createUser")
    public Mono<String> createUserAction(@Valid NewUserDto newUserDto, BindingResult result, Model model) {
        if (result.hasErrors()) {
            return Mono.just("admin_create_user");
        }
        model.addAttribute("redirect", "/admin");
        return userService.createAccount(newUserDto.getUsername(), newUserDto.getPassword(), newUserDto.isEnabled())
                .map(account -> {
                    var message = "Successfully created user '" + account.getUsername() + "'.";
                    model.addAttribute("message", message);
                    return "admin_success";
                })
                .onErrorResume(DataIntegrityViolationException.class, exc -> {
                    result.addError(new ObjectError("username", "User with this username already exists"));
                    return Mono.just("admin_create_user");
                });
    }

    @GetMapping
    public Mono<String> homepage(@RequestParam(value = "page", defaultValue = "0") int page, Model model) {
        return userService.getAllAccounts(PaginationUtil.fromPage(page))
                .map(accounts -> {
                    model.addAttribute("userAccounts", accounts);
                    return "admin_home";
                });
    }

    @GetMapping("/changePassword/{id}")
    public String changePasswordForm(@PathVariable("id") String username, Model model,
                                     ChangeUserPasswordDto changeUserPasswordDto) {
        model.addAttribute("username", username);
        return "admin_change_password";
    }

    @PostMapping("/changePassword/{id}")
    public Mono<String> changePasswordAction(@PathVariable("id") String username, Model model,
                                             @Valid ChangeUserPasswordDto changePasswordDto, BindingResult result) {
        if (result.hasErrors()) {
            return Mono.just("admin_change_password");
        }
        model.addAttribute("redirect", "/admin");
        return userService.updatePassword(username, changePasswordDto.getPassword())
                .map(account -> {
                    var message = "Successfully changed password for user '" + username + "'.";
                    model.addAttribute("message", message);
                    return "admin_success";
                });
    }

    @PostMapping("/setEnabledUser/{id}")
    public Mono<String> setEnabledUserAction(@PathVariable("id") String username,
                                             @RequestParam("enabled") boolean enabled,
                                             Model model) {
        model.addAttribute("redirect", "/admin");
        return userService.setAccountEnabled(username, enabled)
                .map(account -> {
                    var message = "Successfully updated user '" + account.getUsername() + "'.";
                    model.addAttribute("message", message);
                    return "admin_success";
                });
    }

    @PostMapping("/deleteUser/{id}")
    public Mono<String> deleteUserAction(@PathVariable("id") String username, Model model) {
        model.addAttribute("redirect", "/admin");
        return userService.deleteAccount(username)
                .map(account -> {
                    var message = "Successfully deleted user '" + account.getUsername() + "'.";
                    model.addAttribute("message", message);
                    return "admin_success";
                });
    }
}
