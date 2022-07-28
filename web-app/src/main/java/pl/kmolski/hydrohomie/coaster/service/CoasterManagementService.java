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
import pl.kmolski.hydrohomie.coaster.model.Coaster;
import pl.kmolski.hydrohomie.coaster.repo.CoasterRepository;
import pl.kmolski.hydrohomie.webmvc.exception.EntityNotFoundException;
import reactor.core.publisher.Mono;

@Service
@Transactional
@RequiredArgsConstructor
public class CoasterManagementService {

    private static final Logger LOGGER = LoggerFactory.getLogger(CoasterManagementService.class);

    private final CoasterRepository coasterRepository;

    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public Mono<Page<Coaster>> getUnassignedCoasters(Pageable pageable) {
        LOGGER.debug("Fetching unassigned coasters for page {}", pageable);
        return coasterRepository.findAllByOwnerIsNull(pageable).collectList()
                .zipWith(coasterRepository.countByOwnerIsNull())
                .doOnNext(page -> LOGGER.debug("Successfully fetched page {}", page))
                .map(t -> new PageImpl<>(t.getT1(), pageable, t.getT2()));
    }

    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public Mono<Coaster> assignCoasterToUser(String deviceName, String username) {
        return coasterRepository.findById(deviceName)
                .map(coaster -> coaster.setOwner(username))
                .flatMap(coasterRepository::save)
                .doOnNext(coaster -> LOGGER.info("Successfully assigned coaster '{}' to user={}",
                        coaster.getDeviceName(), username))
                .switchIfEmpty(Mono.error(new EntityNotFoundException("Coaster not found")))
                .onErrorResume(
                        DataIntegrityViolationException.class,
                        exc -> Mono.error(new EntityNotFoundException("User not found")));
    }

    @PreAuthorize("hasRole('ROLE_USER') and principal.username == username")
    public Mono<Page<Coaster>> getUserAssignedCoasters(String username, Pageable pageable) {
        LOGGER.debug("Fetching assigned coasters for user '{}', page {}", username, pageable);
        return coasterRepository.findAllByOwner(username, pageable).collectList()
                .zipWith(coasterRepository.countByOwner(username))
                .doOnNext(page -> LOGGER.debug("Successfully fetched page {}", page))
                .map(t -> new PageImpl<>(t.getT1(), pageable, t.getT2()));
    }
}
