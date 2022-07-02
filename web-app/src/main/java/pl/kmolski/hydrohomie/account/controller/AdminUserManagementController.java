package pl.kmolski.hydrohomie.account.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import pl.kmolski.hydrohomie.account.service.UserService;
import pl.kmolski.hydrohomie.webmvc.util.PaginationUtil;
import reactor.core.publisher.Mono;

@Controller
@RequestMapping("/admin")
public class AdminUserManagementController {

    private final UserService userService;

    AdminUserManagementController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping
    public Mono<String> homepage(@RequestParam(value = "page", defaultValue = "0") int page, Model model) {
        return userService.getAllUserAccounts(PaginationUtil.fromPage(page))
                .map(accounts -> {
                    model.addAttribute("userAccounts", accounts);
                    return "admin_home";
                });
    }
}
