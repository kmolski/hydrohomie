package pl.kmolski.hydrohomie.coaster.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.test.context.support.WithMockUser;
import pl.kmolski.hydrohomie.coaster.dto.UpdateCoasterDetailsDto;
import pl.kmolski.hydrohomie.coaster.model.Coaster;
import pl.kmolski.hydrohomie.coaster.repo.CoasterRepository;
import pl.kmolski.hydrohomie.webmvc.util.PaginationUtil;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.ZoneId;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@SuppressWarnings("ReactiveStreamsUnusedPublisher")
class CoasterManagementServiceImplTest {

    @Mock
    private CoasterRepository coasterRepository;

    @InjectMocks
    private CoasterManagementServiceImpl coasterManagementService;

    @Test
    @WithMockUser(roles = {"ADMIN"})
    void getUnassignedCoastersReturnsTheCoasters() {
        var coasterNames = Set.of("esp001", "esp002", "esp003");
        long coasterCount = coasterNames.size();
        var coasters = coasterNames.stream()
                .map(name -> new Coaster(name).setTimezone(ZoneId.of("Etc/UTC")))
                .toList();

        when(coasterRepository.findAllByOwnerIsNull(any())).thenReturn(Flux.fromIterable(coasters));
        when(coasterRepository.countByOwnerIsNull()).thenReturn(Mono.just(coasterCount));
        var coasterPage = coasterManagementService.getUnassignedCoasters(PaginationUtil.fromPage(0)).block();

        assertNotNull(coasterPage, "Returned null page of coasters");
        assertEquals(coasterCount, coasterPage.getTotalElements(), "Page size and coaster count are not equal");
        coasterPage.forEach(coaster -> assertTrue(coasterNames.contains(coaster.getDeviceName()), "Returned unknown coaster"));
    }

    @Test
    void assignCoasterToUserReturnsTheUpdatedCoaster() {
        var coasterName = "esp001";

        var coaster = new Coaster(coasterName);
        when(coasterRepository.findByDeviceNameAndOwner(eq(coasterName), any())).thenReturn(Mono.just(coaster));
        when(coasterRepository.save(any())).then(invocation -> Mono.just(invocation.getArgument(0, Coaster.class)));

        var result = coasterManagementService.assignCoasterToUser(coasterName, "user").block();

        assertNotNull(result, "Returned coaster is null");
        assertEquals("user", result.getOwner(), "Coaster owner was not set");
    }

    @Test
    void getUserAssignedCoastersReturnsTheCoasters() {
        var coasterNames = Set.of("esp001", "esp002", "esp003");
        long coasterCount = coasterNames.size();
        var coasters = coasterNames.stream()
                .map(name -> new Coaster(name).setOwner("user").setTimezone(ZoneId.of("Etc/UTC")))
                .toList();

        when(coasterRepository.findAllByOwner(eq("user"), any())).thenReturn(Flux.fromIterable(coasters));
        when(coasterRepository.countByOwner(eq("user"))).thenReturn(Mono.just(coasterCount));
        var coasterPage = coasterManagementService.getUserAssignedCoasters("user", PaginationUtil.fromPage(0)).block();

        assertNotNull(coasterPage, "Returned null page of coasters");
        assertEquals(coasterCount, coasterPage.getTotalElements(), "Page size and coaster count are not equal");
        coasterPage.forEach(coaster -> assertTrue(coasterNames.contains(coaster.getDeviceName()), "Returned unknown coaster"));
    }

    @Test
    void getCoasterDetailsReturnsTheCoaster() {
        var coasterName = "esp001";

        var coaster = new Coaster(coasterName).setOwner("user");
        when(coasterRepository.findByDeviceNameAndOwner(eq(coasterName), eq("user"))).thenReturn(Mono.just(coaster));

        var result = coasterManagementService.getCoasterDetails(coasterName, "user").block();

        assertNotNull(result, "Returned coaster is null");
        assertEquals(coasterName, result.getDeviceName(), "Unexpected coaster device name");
        assertEquals("user", result.getOwner(), "Unexpected coaster owner");
    }

    @Test
    void updateCoasterDetailsReturnsTheUpdatedCoaster() {
        var coasterName = "esp001";

        var coaster = new Coaster(coasterName).setTimezone(ZoneId.of("Etc/UTC"));
        when(coasterRepository.findByDeviceNameAndOwner(eq(coasterName), eq("user"))).thenReturn(Mono.just(coaster));
        when(coasterRepository.save(any())).then(invocation -> Mono.just(invocation.getArgument(0, Coaster.class)));

        var newDisplayName = "ESP32 coaster number 001";
        var newDescription = "Description";
        var newTimezone = ZoneId.of("Europe/Warsaw");
        var newPlace = "Mock land";
        var updateDto = new UpdateCoasterDetailsDto().setDisplayName(newDisplayName)
                .setDescription(newDescription)
                .setTimezone(newTimezone)
                .setPlace(newPlace);
        var result = coasterManagementService.updateCoasterDetails(coasterName, "user", updateDto).block();

        assertNotNull(result, "Returned coaster is null");
        assertEquals(newDisplayName, result.getDisplayName(), "Display name was not set");
        assertEquals(newDescription, result.getDescription(), "Description was not set");
        assertEquals(newTimezone, result.getTimezone(), "Timezone was not set");
        assertEquals(newPlace, result.getPlace(), "Place was not set");
    }

    @Test
    void removeCoasterFromUserReturnsTheUpdatedCoaster() {
        var coasterName = "esp001";

        var coaster = new Coaster(coasterName).setOwner("user").setTimezone(ZoneId.of("Etc/UTC"));
        when(coasterRepository.findByDeviceNameAndOwner(eq(coasterName), eq("user"))).thenReturn(Mono.just(coaster));
        when(coasterRepository.save(any())).then(invocation -> Mono.just(invocation.getArgument(0, Coaster.class)));

        var result = coasterManagementService.removeCoasterFromUser(coasterName, "user").block();

        assertNotNull(result, "Returned coaster is null");
        assertNull(result.getOwner(), "Coaster owner was not set to null");
    }
}
