package pl.kmolski.hydrohomie.account.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import pl.kmolski.hydrohomie.account.service.UserService;
import pl.kmolski.hydrohomie.webmvc.util.PaginationUtil;
import reactor.core.publisher.Mono;

@Controller
@RequestMapping("/admin")
@RequiredArgsConstructor
public class AdminUserManagementController {

    private final UserService userService;

    @GetMapping
    public Mono<String> homepage(@RequestParam(value = "page", defaultValue = "0") int page, Model model) {
        return userService.getAllUserAccounts(PaginationUtil.fromPage(page))
                .map(accounts -> {
                    model.addAttribute("userAccounts", accounts);
                    return "admin_home";
                });
    }

    @PostMapping("/deleteUser/{id}")
    public Mono<String> deleteUserAction(@PathVariable("id") String username, Model model) {
        model.addAttribute("redirect", "/admin");
        return userService.deleteUserAccount(username)
                .map(account -> {
                    var message = "Successfully deleted user '" + account.getUsername() + "'.";
                    model.addAttribute("message", message);
                    return "admin_success";
                })
                .onErrorReturn("admin_error");
    }
}
