package pl.kmolski.hydrohomie.coaster.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import pl.kmolski.hydrohomie.coaster.model.Measurement;
import pl.kmolski.hydrohomie.coaster.repo.MeasurementRepository;
import pl.kmolski.hydrohomie.coaster.service.CoasterService;
import pl.kmolski.hydrohomie.webmvc.util.PaginationUtil;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Clock;
import java.time.Instant;
import java.util.Optional;

@Controller
@RequestMapping("/user")
@RequiredArgsConstructor
public class UserCoasterController {

    private final CoasterService coasterService;
    private final MeasurementRepository measurementRepository;
    private final Clock clock;

    @GetMapping
    public Mono<String> homepage(@RequestParam(value = "page", defaultValue = "0") int page,
                                 Authentication authentication, Model model) {
        return coasterService.getUserAssignedCoasters(authentication.getName(), PaginationUtil.fromPage(page))
                .map(coasters -> {
                    model.addAttribute("coasters", coasters);
                    return "user_home";
                });
    }

    @GetMapping("/coaster/{name}/measurements")
    public Flux<Measurement> getMeasurements(@PathVariable("name") String deviceName,
                                             @RequestParam("start") Long startTime,
                                             @RequestParam(value = "end", required = false) Optional<Long> endTime) {
        var start = Instant.ofEpochSecond(startTime);
        var end = endTime.map(Instant::ofEpochSecond).orElseGet(() -> Instant.now(clock));
        return measurementRepository.findByDeviceNameAndTimestampBetween(deviceName, start, end);
    }
}
