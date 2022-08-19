package pl.kmolski.hydrohomie.coaster.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;
import pl.kmolski.hydrohomie.coaster.dto.UpdateCoasterDetailsDto;
import pl.kmolski.hydrohomie.coaster.model.Coaster;
import reactor.core.publisher.Mono;

/**
 * Coaster entity management service. Operations like coaster creation, removal,
 * assignment, disassociation & coaster detail update are handled here.
 */
public interface CoasterManagementService {

    /**
     * Fetch the specified page of coaster entities that are not assigned to any user.
     *
     * @param pageable the page descriptor
     * @return the requested page of {@link Coaster} entities
     */
    @Transactional(readOnly = true)
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    Mono<Page<Coaster>> getUnassignedCoasters(Pageable pageable);

    /**
     * Assign the coaster entity to the specified user.
     *
     * @param deviceName the device ID
     * @param username   the new coaster owner's username
     * @return the updated {@link Coaster} entity
     */
    @Transactional
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    Mono<Coaster> assignCoasterToUser(String deviceName, String username);

    /**
     * Fetch the specified page of coaster entities that are assigned to the specified user.
     *
     * @param username the coaster owner's username
     * @param pageable the page descriptor
     * @return the requested page of {@link Coaster} entities
     */
    @Transactional(readOnly = true)
    @PreAuthorize("hasRole('ROLE_USER') and principal.username == username")
    Mono<Page<Coaster>> getUserAssignedCoasters(String username, Pageable pageable);

    /**
     * Fetch the specified coaster entity.
     *
     * @param deviceName the device ID
     * @param username   the coaster owner's username
     * @return the {@link Coaster} entity
     */
    @Transactional(readOnly = true)
    @PreAuthorize("hasRole('ROLE_USER') and principal.username == username")
    Mono<Coaster> getCoasterDetails(String deviceName, String username);

    /**
     * Update the coaster entity with the specified details.
     *
     * @param deviceName the device ID
     * @param username   the owner's username
     * @param detailsDto the new coaster details
     * @return the updated {@link Coaster} entity
     */
    @Transactional
    @PreAuthorize("hasRole('ROLE_USER') and principal.username == username")
    Mono<Coaster> updateCoasterDetails(String deviceName, String username, UpdateCoasterDetailsDto detailsDto);

    /**
     * Disassociate the coaster entity from the specified user.
     *
     * @param deviceName the device ID
     * @param username   the new coaster owner's username
     * @return the updated {@link Coaster} entity
     */
    @Transactional
    @PreAuthorize("hasRole('ROLE_USER') and principal.username == username")
    Mono<Coaster> removeCoasterFromUser(String deviceName, String username);
}
