package pl.kmolski.hydrohomie.coaster.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import pl.kmolski.hydrohomie.account.service.UserService;
import pl.kmolski.hydrohomie.coaster.service.CoasterManagementService;
import pl.kmolski.hydrohomie.webmvc.util.PaginationUtil;
import reactor.core.publisher.Mono;

/**
 * Admin-accessible controller providing the coaster assignment functionality.
 */
@Controller
@RequestMapping("/admin/coasters")
@RequiredArgsConstructor
public class AdminCoasterManagementController {

    private final CoasterManagementService coasterService;
    private final UserService userService;

    /**
     * Populate and show the unassigned coaster list view.
     *
     * @param page the page of unassigned coasters to show
     * @param model the template model
     * @return the unassigned coaster list view
     */
    @GetMapping
    public Mono<String> homepage(@RequestParam(value = "page", defaultValue = "0") int page, Model model) {
        return coasterService.getUnassignedCoasters(PaginationUtil.fromPage(page))
                .map(coasters -> {
                    model.addAttribute("coasters", coasters);
                    return "admin_coasters";
                });
    }

    /**
     * Populate and show the assign coaster form.
     *
     * @param deviceName the device ID of the coaster to assign
     * @param model the template model
     * @param page the page of available users to show
     * @return the assign coaster form
     */
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

    /**
     * Assign the coaster to the user specified in the assign coaster form.
     *
     * @param deviceName the device ID of the coaster to assign
     * @param model the template model
     * @param username the name of the user to assign the coaster to
     * @return the success page
     */
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
