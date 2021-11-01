package pl.kmolski.hydrohomie.service;

import org.springframework.security.core.userdetails.ReactiveUserDetailsService;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import pl.kmolski.hydrohomie.model.Account;
import pl.kmolski.hydrohomie.model.UserPrincipal;
import pl.kmolski.hydrohomie.repo.UserRepository;
import reactor.core.publisher.Mono;

@Component
public class UserService implements ReactiveUserDetailsService {

    private final UserRepository userRepository;
    private final AdminAccount adminAccount;

    UserService(UserRepository userRepository, AdminAccount adminAccount) {
        this.userRepository = userRepository;
        this.adminAccount = adminAccount;
    }

    private Mono<? extends Account> getUserEntity(String username) {
        if (adminAccount.getUsername().equals(username)) {
            return Mono.just(adminAccount);
        } else {
            return userRepository.findById(username);
        }
    }

    @Override
    public Mono<UserDetails> findByUsername(String username) {
        return getUserEntity(username).map(UserPrincipal::new);
    }
}
