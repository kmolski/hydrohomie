package pl.kmolski.hydrohomie.coaster.service;

import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pl.kmolski.hydrohomie.coaster.dto.UpdateCoasterDetailsDto;
import pl.kmolski.hydrohomie.coaster.model.Coaster;
import pl.kmolski.hydrohomie.coaster.repo.CoasterRepository;
import pl.kmolski.hydrohomie.webmvc.exception.EntityNotFoundException;
import reactor.core.publisher.Mono;

/**
 * Coaster entity management service. Operations like coaster creation, removal,
 * assignment, disassociation & coaster detail update are handled here.
 */
@Service
@RequiredArgsConstructor
public class CoasterManagementService {

    private static final Logger LOGGER = LoggerFactory.getLogger(CoasterManagementService.class);

    private final CoasterRepository coasterRepository;

    private Mono<Coaster> findCoasterOrFail(String deviceName, String owner) {
        LOGGER.debug("Fetching coaster '{}' for user {}", deviceName, owner);
        return coasterRepository.findByDeviceNameAndOwner(deviceName, owner)
                .switchIfEmpty(Mono.error(new EntityNotFoundException("Coaster not found")));
    }

    private Mono<Coaster> updateCoasterOwner(String deviceName, String prevOwner, String newOwner) {
        return findCoasterOrFail(deviceName, prevOwner)
                .map(coaster -> coaster.setOwner(newOwner))
                .flatMap(coasterRepository::save);
    }

    /**
     * Fetch the specified page of coaster entities that are not assigned to any user.
     *
     * @param pageable the page descriptor
     * @return the requested page of {@link Coaster} entities
     */
    @Transactional(readOnly = true)
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public Mono<Page<Coaster>> getUnassignedCoasters(Pageable pageable) {
        LOGGER.debug("Fetching unassigned coasters for page {}", pageable);
        return coasterRepository.findAllByOwnerIsNull(pageable).collectList()
                .zipWith(coasterRepository.countByOwnerIsNull())
                .doOnNext(page -> LOGGER.debug("Successfully fetched page {}", page))
                .map(t -> new PageImpl<>(t.getT1(), pageable, t.getT2()));
    }

    /**
     * Assign the coaster entity to the specified user.
     *
     * @param deviceName the device ID
     * @param username the new coaster owner's username
     * @return the updated {@link Coaster} entity
     */
    @Transactional
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public Mono<Coaster> assignCoasterToUser(String deviceName, String username) {
        LOGGER.info("Assigning coaster '{}' to user {}", deviceName, username);
        return updateCoasterOwner(deviceName, null, username)
                .doOnNext(coaster -> LOGGER.info("Successfully assigned coaster '{}' to user {}",
                        coaster.getDeviceName(), username))
                .onErrorResume(
                        DataIntegrityViolationException.class,
                        exc -> Mono.error(new EntityNotFoundException("User not found")));
    }

    /**
     * Fetch the specified page of coaster entities that are assigned to the specified user.
     *
     * @param username the coaster owner's username
     * @param pageable the page descriptor
     * @return the requested page of {@link Coaster} entities
     */
    @Transactional(readOnly = true)
    @PreAuthorize("hasRole('ROLE_USER') and principal.username == username")
    public Mono<Page<Coaster>> getUserAssignedCoasters(String username, Pageable pageable) {
        LOGGER.debug("Fetching assigned coasters for user '{}', page {}", username, pageable);
        return coasterRepository.findAllByOwner(username, pageable).collectList()
                .zipWith(coasterRepository.countByOwner(username))
                .doOnNext(page -> LOGGER.debug("Successfully fetched page {}", page))
                .map(t -> new PageImpl<>(t.getT1(), pageable, t.getT2()));
    }

    /**
     * Fetch the specified coaster entity.
     *
     * @param deviceName the device ID
     * @param username the coaster owner's username
     * @return the {@link Coaster} entity
     */
    @Transactional(readOnly = true)
    @PreAuthorize("hasRole('ROLE_USER') and principal.username == username")
    public Mono<Coaster> getCoasterDetails(String deviceName, String username) {
        return findCoasterOrFail(deviceName, username)
                .doOnNext(coaster -> LOGGER.info("Found coaster '{}' of user {}",
                        coaster.getDeviceName(), username));
    }

    /**
     * Update the coaster entity with the specified details.
     *
     * @param deviceName the device ID
     * @param username the owner's username
     * @param detailsDto the new coaster details
     * @return the updated {@link Coaster} entity
     */
    @Transactional
    @PreAuthorize("hasRole('ROLE_USER') and principal.username == username")
    public Mono<Coaster> updateCoasterDetails(String deviceName, String username, UpdateCoasterDetailsDto detailsDto) {
        LOGGER.info("Updating coaster '{}' as user {}", deviceName, username);
        return findCoasterOrFail(deviceName, username)
                .map(coaster -> coaster.setDisplayName(detailsDto.getDisplayName())
                                       .setDescription(detailsDto.getDescription())
                                       .setTimezone(detailsDto.getTimezone())
                                       .setPlace(detailsDto.getPlace()))
                .flatMap(coasterRepository::save)
                .doOnNext(coaster -> LOGGER.info("Successfully updated coaster '{}' as user {}",
                        coaster.getDeviceName(), username));
    }

    /**
     * Disassociate the coaster entity from the specified user.
     *
     * @param deviceName the device ID
     * @param username the new coaster owner's username
     * @return the updated {@link Coaster} entity
     */
    @Transactional
    @PreAuthorize("hasRole('ROLE_USER') and principal.username == username")
    public Mono<Coaster> removeCoasterFromUser(String deviceName, String username) {
        LOGGER.info("Removing coaster '{}' from user {}", deviceName, username);
        return updateCoasterOwner(deviceName, username, null)
                .doOnNext(coaster -> LOGGER.info("Successfully removed coaster '{}' from user {}",
                        coaster.getDeviceName(), username));
    }
}
