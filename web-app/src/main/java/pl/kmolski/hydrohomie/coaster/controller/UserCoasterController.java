package pl.kmolski.hydrohomie.coaster.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import pl.kmolski.hydrohomie.coaster.dto.UpdateCoasterDetailsDto;
import pl.kmolski.hydrohomie.coaster.model.Measurement;
import pl.kmolski.hydrohomie.coaster.repo.MeasurementRepository;
import pl.kmolski.hydrohomie.coaster.service.CoasterManagementService;
import pl.kmolski.hydrohomie.webmvc.util.PaginationUtil;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import javax.validation.Valid;
import java.time.Clock;
import java.time.Instant;
import java.util.Optional;

/**
 * User-accessible controller providing coaster presentation and management.
 */
@Controller
@RequestMapping("/user")
@RequiredArgsConstructor
public class UserCoasterController {

    private final CoasterManagementService coasterService;
    private final MeasurementRepository measurementRepository;
    private final Clock clock;

    /**
     * Populate and show the assigned coaster list view.
     *
     * @param page the page of assigned coasters to show
     * @param authentication the user's authentication object
     * @param model the template model
     * @return the assigned coaster list view
     */
    @GetMapping
    public Mono<String> homepage(@RequestParam(value = "page", defaultValue = "0") int page,
                                 Authentication authentication, Model model) {
        return coasterService.getUserAssignedCoasters(authentication.getName(), PaginationUtil.fromPage(page))
                .map(coasters -> {
                    model.addAttribute("coasters", coasters);
                    return "user_home";
                });
    }

    /**
     * Populate and show the coaster details view.
     *
     * @param deviceName the name of the coaster to show
     * @param auth the user's authentication object
     * @param model the template model
     * @return the show coaster details form
     */
    @GetMapping("/coaster/{id}")
    public Mono<String> showCoasterDetails(@PathVariable("id") String deviceName, Authentication auth, Model model) {
        return coasterService.getCoasterDetails(deviceName, auth.getName())
                .map(coaster -> {
                    model.addAttribute("coaster", coaster);
                    return "user_show_coaster";
                });
    }

    /**
     * Populate and show the edit coaster details form.
     *
     * @param deviceName the name of the coaster to update
     * @param auth the user's authentication object
     * @param model the template model
     * @param updateCoasterDetailsDto new DTO for the coaster details
     * @return the edit coaster details form
     */
    @GetMapping("/editCoaster/{id}")
    public Mono<String> editCoasterForm(@PathVariable("id") String deviceName, Authentication auth,
                                        Model model, UpdateCoasterDetailsDto updateCoasterDetailsDto) {
        return coasterService.getCoasterDetails(deviceName, auth.getName())
                .map(coaster -> {
                    updateCoasterDetailsDto.setDisplayName(coaster.getDisplayName()).setDescription(coaster.getDescription());
                    updateCoasterDetailsDto.setTimezone(coaster.getTimezone()).setPlace(coaster.getPlace());
                    model.addAttribute("deviceName", deviceName);
                    return "user_edit_coaster";
                });
    }

    /**
     * Update the coaster details to the ones specified in the edit coaster form.
     *
     * @param deviceName the name of the coaster to update
     * @param auth the user's authentication object
     * @param model the template model
     * @param updateCoasterDto DTO for the password change
     * @param result the result of DTO binding (includes validation errors)
     * @return the success page or form with errors
     */
    @PostMapping("/editCoaster/{id}")
    public Mono<String> editCoasterAction(@PathVariable("id") String deviceName, Authentication auth, Model model,
                                          @Valid UpdateCoasterDetailsDto updateCoasterDto, BindingResult result) {
        if (result.hasErrors()) {
            return Mono.just("user_edit_coaster");
        }
        model.addAttribute("redirect", "/user");
        return coasterService.updateCoasterDetails(deviceName, auth.getName(), updateCoasterDto)
                .map(coaster -> {
                    var message = "Successfully updated coaster '" + deviceName + "'.";
                    model.addAttribute("message", message);
                    return "user_success";
                });
    }

    /**
     * Show the coaster remove confirmation view.
     *
     * @param deviceName the name of the coaster to remove
     * @param model the template model
     * @return the confirmation page
     */
    @GetMapping("/removeCoaster/{id}")
    public String removeCoasterConfirm(@PathVariable("id") String deviceName, Model model) {
        model.addAttribute("deviceName", deviceName);
        return "user_confirm_remove";
    }

    /**
     * Remove the coaster specified in the path variable 'id'.
     *
     * @param deviceName the name of the coaster to remove
     * @param authentication the user's authentication object
     * @param model the template model
     * @return the success page
     */
    @PostMapping("/removeCoaster/{id}")
    public Mono<String> removeCoasterAction(@PathVariable("id") String deviceName,
                                            Authentication authentication, Model model) {
        model.addAttribute("redirect", "/user");
        return coasterService.removeCoasterFromUser(deviceName, authentication.getName())
                .map(coaster -> {
                    var message = "Successfully removed coaster '" + coaster.getDeviceName() + "' from your account.";
                    model.addAttribute("message", message);
                    return "user_success";
                });
    }

    @GetMapping("/coasterMeasurements/{id}")
    public Flux<Measurement> getMeasurements(@PathVariable("id") String deviceName,
                                             @RequestParam("start") Long startTime,
                                             @RequestParam(value = "end", required = false) Optional<Long> endTime) {
        var start = Instant.ofEpochSecond(startTime);
        var end = endTime.map(Instant::ofEpochSecond).orElseGet(() -> Instant.now(clock));
        return measurementRepository.findByDeviceNameAndTimestampBetween(deviceName, start, end);
    }
}
