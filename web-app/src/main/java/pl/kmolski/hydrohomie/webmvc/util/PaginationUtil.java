package pl.kmolski.hydrohomie.webmvc.util;

import org.springframework.data.domain.Pageable;

/**
 * Utilities related to pagination in the web UI.
 */
public class PaginationUtil {

    /**
     * The default page size.
     */
    public static final int DEFAULT_SIZE = 30;

    private PaginationUtil () {}

    /**
     * Create a {@link Pageable} object with the default page size and given page number.
     * @param page the page number
     * @return the {@link Pageable} object
     */
    public static Pageable fromPage(int page) {
        return Pageable.ofSize(DEFAULT_SIZE).withPage(page);
    }
}
