package pl.kmolski.hydrohomie.account.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * <p>Configuration properties for the application admin account.
 * <p>
 * This class defines the following properties:
 * <ul>
 *     <li>admin.username - admin username</li>
 *     <li>admin.password - admin plaintext password</li>
 * </ul>
 */
@ConfigurationProperties("admin")
public record AdminAccountSettings(String username, String password) {}
