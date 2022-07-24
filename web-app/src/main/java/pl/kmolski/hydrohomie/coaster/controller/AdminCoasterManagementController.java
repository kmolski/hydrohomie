package pl.kmolski.hydrohomie.coaster.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import pl.kmolski.hydrohomie.account.service.UserService;
import pl.kmolski.hydrohomie.coaster.service.CoasterService;
import pl.kmolski.hydrohomie.webmvc.util.PaginationUtil;
import reactor.core.publisher.Mono;

@Controller
@RequestMapping("/admin/coasters")
@RequiredArgsConstructor
public class AdminCoasterManagementController {

    private final CoasterService coasterService;
    private final UserService userService;

    @GetMapping
    public Mono<String> homepage(@RequestParam(value = "page", defaultValue = "0") int page, Model model) {
        return coasterService.getUnassignedCoasters(PaginationUtil.fromPage(page))
                .map(coasters -> {
                    model.addAttribute("coasters", coasters);
                    return "admin_coasters";
                });
    }

    @GetMapping("/assignCoaster/{id}")
    public Mono<String> assignCoasterForm(@PathVariable("id") String deviceName, Model model,
                                          @RequestParam(value = "page", defaultValue = "0") int page) {
        return userService.getAllAccounts(PaginationUtil.fromPage(page))
                .map(userAccounts -> {
                    model.addAttribute("userAccounts", userAccounts);
                    model.addAttribute("deviceName", deviceName);
                    return "admin_coaster_assign";
                });
    }

    @PostMapping("/assignCoaster/{id}")
    public Mono<String> assignCoasterAction(@PathVariable("id") String deviceName, Model model,
                                            @RequestParam("userId") String username) {
        model.addAttribute("redirect", "/admin/coasters");
        return coasterService.assignCoasterToUser(deviceName, username)
                .map(coaster -> {
                    var message = "Successfully assigned coaster '" + deviceName + "'.";
                    model.addAttribute("message", message);
                    return "admin_success";
                });
    }
}
