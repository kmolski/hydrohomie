package pl.kmolski.hydrohomie.account.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import pl.kmolski.hydrohomie.account.dto.PlaintextPassword;
import pl.kmolski.hydrohomie.account.model.UserAccount;
import pl.kmolski.hydrohomie.account.repo.UserRepository;
import pl.kmolski.hydrohomie.webmvc.util.PaginationUtil;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;


@ExtendWith(MockitoExtension.class)
@SuppressWarnings("ReactiveStreamsUnusedPublisher")
class UserServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private AdminAccount adminAccount;

    @InjectMocks
    private UserServiceImpl userService;

    @Test
    void findUserAccountByUsernameSucceeds() {
        var username = "james_t_kirk";
        var account = new UserAccount(username, null, true);

        when(adminAccount.getUsername()).thenReturn("admin");
        when(userRepository.findByUsername(username)).thenReturn(Mono.just(account));
        var result = userService.findByUsername(username).block();

        assertEquals(account, result, "User account not returned");
    }

    @Test
    void findNonexistentAccountByUsernameFails() {
        var username = "james_t_kirk";

        when(adminAccount.getUsername()).thenReturn("admin");
        when(userRepository.findByUsername(username)).thenReturn(Mono.empty());
        var result = userService.findByUsername(username).block();

        assertNull(result, "Nonexistent account was returned");
    }

    @Test
    void findAdminAccountByUsernameSucceeds() {
        var adminUsername = "admin";

        when(adminAccount.getUsername()).thenReturn(adminUsername);
        var account = userService.findByUsername(adminUsername).block();

        assertSame(adminAccount, account, "Admin account not returned");
    }

    @Test
    void createAccountReturnsTheNewAccount() {
        var username = "james_t_kirk";
        var password = new PlaintextPassword("enterprise");
        var encodedPassword = password.plaintext() + "encoded";

        var account = new UserAccount(username, encodedPassword, true);

        when(passwordEncoder.encode(any())).then(invocation -> invocation.getArgument(0, String.class) + "encoded");
        when(userRepository.create(username, encodedPassword, true)).thenReturn(Mono.just(account));

        var result = userService.createAccount(username, password, true).block();

        assertNotNull(result, "Returned null user");
        assertSame(username, result.getUsername(), "Username is not the same");
        assertNotEquals(password.plaintext(), result.getPassword(), "Password is not encoded");
        assertTrue(result.isEnabled(), "User is not enabled");
    }

    @Test
    void getAllAccountsReturnsTheAccounts() {
        var usernames = Set.of("spock", "jimmy", "scotty");
        var users = usernames.stream().map(name -> new UserAccount(name, "", true));

        var pageable = PaginationUtil.fromPage(0);
        when(userRepository.findAllBy(pageable)).thenReturn(Flux.fromStream(users));
        when(userRepository.count()).thenReturn(Mono.just((long) usernames.size()));

        var userPage = userService.getAllAccounts(pageable).block();

        assertNotNull(userPage, "Returned null page of users");
        assertEquals(usernames.size(), userPage.getTotalElements(), "Page size and user count are not equal");
        userPage.forEach(account -> assertTrue(usernames.contains(account.getUsername()), "Returned unknown username"));
    }

    @Test
    void updatePasswordReturnsTheUpdatedAccount() {
        var username = "james_t_kirk";
        var oldEncodedPassword = "enterprise";

        var account = new UserAccount(username, oldEncodedPassword, true);
        when(userRepository.findById(username)).thenReturn(Mono.just(account));

        var newPassword = new PlaintextPassword("troubling tribbles");
        var newEncodedPassword = newPassword.plaintext() + "encoded";
        when(passwordEncoder.encode(newPassword.plaintext())).thenReturn(newEncodedPassword);
        when(userRepository.save(any())).then(invocation -> Mono.just(invocation.getArgument(0, UserAccount.class)));

        var result = userService.updatePassword(username, newPassword).block();

        assertNotNull(result, "Returned account is null");
        assertEquals(username, result.getUsername(), "Username has changed");
        assertNotEquals(oldEncodedPassword, result.getPassword(), "Password was not changed");
        assertEquals(newEncodedPassword, result.getPassword(), "Password was not set to new encoded password");
    }

    @Test
    void setAccountEnabledReturnsTheUpdatedAccount() {
        var username = "james_t_kirk";

        var account = new UserAccount(username, null, false);
        when(userRepository.findById(username)).thenReturn(Mono.just(account));
        when(userRepository.save(any())).then(invocation -> Mono.just(invocation.getArgument(0, UserAccount.class)));

        var result = userService.setAccountEnabled(username, true).block();

        assertNotNull(result, "Returned account is null");
        assertTrue(result.isEnabled(), "User was not enabled");
    }

    @Test
    void deleteAccountReturnsTheAccount() {
        var username = "james_t_kirk";

        var account = new UserAccount(username, null, false);
        when(userRepository.findById(username)).thenReturn(Mono.just(account));
        when(userRepository.delete(any())).thenReturn(Mono.just(0).then());

        var result = userService.deleteAccount(username).block();

        assertNotNull(result, "Returned account is null");
        assertEquals(account, result, "Deleted user was not returned");
    }
}
