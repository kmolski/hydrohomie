package pl.kmolski.hydrohomie.account.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * <p>Configuration properties for the application admin account.</p>
 * <p>
 *     This class defines the following properties:
 *     <ul>
 *         <li>admin.username - admin username</li>
 *         <li>admin.password - admin plaintext password</li>
 *     </ul>
 * </p>
 */
@Component
@ConfigurationProperties("admin")
public class AdminAccountSettings {

    private String username;
    private String password;

    /**
     * Return the username of the application admin account
     * @return the non-null admin username
     */
    public String getUsername() {
        return username;
    }

    /**
     * Set the username of the application admin account
     * @param username the admin username
     */
    public void setUsername(String username) {
        this.username = username;
    }

    /**
     * Return the password of the application admin account
     * @return the plaintext password
     */
    public String getPassword() {
        return password;
    }

    /**
     * Set the password of the application admin account
     * @param password the plaintext password
     */
    public void setPassword(String password) {
        this.password = password;
    }
}
