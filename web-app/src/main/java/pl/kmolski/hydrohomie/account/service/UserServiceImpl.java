package pl.kmolski.hydrohomie.account.service;

import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pl.kmolski.hydrohomie.account.dto.PlaintextPassword;
import pl.kmolski.hydrohomie.account.model.UserAccount;
import pl.kmolski.hydrohomie.account.repo.UserRepository;
import pl.kmolski.hydrohomie.webmvc.exception.EntityNotFoundException;
import reactor.core.publisher.Mono;

/**
 * Implementation of {@link UserService}
 */
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private static final Logger LOGGER = LoggerFactory.getLogger(UserServiceImpl.class);

    private final PasswordEncoder passwordEncoder;
    private final UserRepository userRepository;
    private final AdminAccount adminAccount;

    private Mono<UserAccount> findUserOrFail(String username) {
        return userRepository.findById(username)
                .switchIfEmpty(Mono.error(new EntityNotFoundException("User not found")));
    }

    @Override
    @Transactional(readOnly = true)
    public Mono<UserDetails> findByUsername(String username) {
        LOGGER.debug("Fetching UserDetails for username={}", username);
        if (adminAccount.getUsername().equals(username)) {
            LOGGER.debug("Found admin account with username={}", username);
            return Mono.just(adminAccount);
        } else {
            LOGGER.debug("Fetching user account with username={}", username);
            return userRepository.findByUsername(username)
                    .doOnNext(account -> LOGGER.debug("Found user account with username={}", account.getUsername()));
        }
    }

    @Override
    @Transactional
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public Mono<UserAccount> createAccount(String username, PlaintextPassword password, boolean enabled) {
        LOGGER.info("Creating account for user '{}' with enabled={}", username, enabled);
        var encodedPassword = passwordEncoder.encode(password.plaintext());
        return userRepository.create(username, encodedPassword, enabled)
                .doOnNext(account -> LOGGER.info("Successfully created user '{}'", account.getUsername()));
    }

    @Override
    @Transactional(readOnly = true)
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public Mono<Page<UserAccount>> getAllAccounts(Pageable pageable) {
        LOGGER.debug("Fetching user accounts for page {}", pageable);
        return userRepository.findAllBy(pageable).collectList()
                .zipWith(userRepository.count())
                .doOnNext(page -> LOGGER.debug("Successfully fetched page {}", page))
                .map(t -> new PageImpl<>(t.getT1(), pageable, t.getT2()));
    }

    @Override
    @Transactional
    @PreAuthorize("hasRole('ROLE_ADMIN') or (hasRole('ROLE_USER') and principal.username == username)")
    public Mono<UserAccount> updatePassword(String username, PlaintextPassword password) {
        LOGGER.info("Updating password for user '{}'", username);
        var encodedPassword = passwordEncoder.encode(password.plaintext());
        return findUserOrFail(username)
                .map(entity -> entity.setPassword(encodedPassword))
                .flatMap(userRepository::save)
                .doOnNext(account -> LOGGER.info("Successfully updated password for user '{}'", account.getUsername()));
    }

    @Override
    @Transactional
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public Mono<UserAccount> setAccountEnabled(String username, boolean enabled) {
        LOGGER.info("Setting user '{}' to enabled={}", username, enabled);
        return findUserOrFail(username)
                .map(entity -> entity.setEnabled(enabled))
                .flatMap(userRepository::save)
                .doOnNext(account -> LOGGER.info("Successfully set user '{}' to enabled={}",
                                account.getUsername(), account.isEnabled()));
    }

    @Override
    @Transactional
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public Mono<UserAccount> deleteAccount(String username) {
        LOGGER.info("Deleting account for user '{}'", username);
        return findUserOrFail(username)
                .flatMap(entity -> userRepository.delete(entity).thenReturn(entity))
                .doOnNext(account -> LOGGER.info("Successfully deleted user '{}'", account.getUsername()));
    }
}
