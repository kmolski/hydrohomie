package pl.kmolski.hydrohomie.webmvc.controller;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import static pl.kmolski.hydrohomie.account.service.AdminAccount.ADMIN_ROLE;

@Controller
@RequestMapping("/")
public class RootController {

    @GetMapping
    public String homepage(Authentication authentication) {
        if (authentication.getAuthorities().contains(new SimpleGrantedAuthority(ADMIN_ROLE))) {
            return "redirect:/admin";
        } else {
            return "redirect:/user";
        }
    }

    @GetMapping("/login")
    public String login() {
        return "login";
    }
}
