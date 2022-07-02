package pl.kmolski.hydrohomie.account.service;

import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.userdetails.ReactiveUserDetailsService;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pl.kmolski.hydrohomie.account.model.UserAccount;
import pl.kmolski.hydrohomie.account.repo.UserRepository;
import reactor.core.publisher.Mono;

@Service
@Transactional
@RequiredArgsConstructor
public class UserService implements ReactiveUserDetailsService {

    private static final Logger LOGGER = LoggerFactory.getLogger(UserService.class);

    private final UserRepository userRepository;
    private final AdminAccount adminAccount;

    @Override
    public Mono<UserDetails> findByUsername(String username) {
        if (adminAccount.getUsername().equals(username)) {
            return Mono.just(adminAccount);
        } else {
            return userRepository.findByUsername(username);
        }
    }

    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public Mono<Page<UserAccount>> getAllUserAccounts(Pageable pageable) {
        return userRepository.findAllBy(pageable).collectList()
                .zipWith(userRepository.count())
                .map(t -> new PageImpl<>(t.getT1(), pageable, t.getT2()));
    }
}
