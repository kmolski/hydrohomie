package pl.kmolski.hydrohomie.account.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import pl.kmolski.hydrohomie.account.service.UserService;
import reactor.core.publisher.Mono;

@Controller
@RequestMapping("/admin")
public class AdminUserManagementController {

    private final UserService userService;

    AdminUserManagementController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping
    public Mono<String> homepage(Model model) {
        return userService.getAllUserAccounts().collectList()
                .map(accounts -> {
                    model.addAttribute("userAccounts", accounts);
                    return "admin_home";
                });
    }
}
