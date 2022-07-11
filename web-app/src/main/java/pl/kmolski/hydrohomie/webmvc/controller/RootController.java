package pl.kmolski.hydrohomie.webmvc.controller;

import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import static pl.kmolski.hydrohomie.account.model.AccountRole.ROLE_ADMIN;

/**
 * Common root controller for authenticated & unauthenticated users.
 */
@Controller
@RequestMapping("/")
public class RootController {

    /**
     * Redirect the user to their appropriate homepage.
     * @param authentication the authentication object
     * @return the redirect to the admin or user homepage
     */
    @GetMapping
    public String homepage(Authentication authentication) {
        if (authentication.getAuthorities().contains(ROLE_ADMIN.authority())) {
            return "redirect:/admin";
        } else {
            return "redirect:/user";
        }
    }

    /**
     * Show the login form.
     * @return the login form template
     */
    @GetMapping("/login")
    public String login() {
        return "login";
    }
}
