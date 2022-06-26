package pl.kmolski.hydrohomie.account.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.userdetails.ReactiveUserDetailsService;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import pl.kmolski.hydrohomie.account.model.UserAccount;
import pl.kmolski.hydrohomie.account.repo.UserRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Component
@Transactional
public class UserService implements ReactiveUserDetailsService {

    private static final Logger LOGGER = LoggerFactory.getLogger(UserService.class);

    private final UserRepository userRepository;
    private final AdminAccount adminAccount;

    UserService(UserRepository userRepository, AdminAccount adminAccount) {
        this.userRepository = userRepository;
        this.adminAccount = adminAccount;
    }

    @Override
    public Mono<UserDetails> findByUsername(String username) {
        if (adminAccount.getUsername().equals(username)) {
            return Mono.just(adminAccount);
        } else {
            return userRepository.findByUsername(username);
        }
    }

    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public Flux<UserAccount> getAllUserAccounts() {
        return userRepository.findAll();
    }
}
