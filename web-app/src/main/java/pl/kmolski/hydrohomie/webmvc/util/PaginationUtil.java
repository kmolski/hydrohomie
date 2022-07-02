package pl.kmolski.hydrohomie.webmvc.util;

import org.springframework.data.domain.Pageable;

public class PaginationUtil {

    public static final int DEFAULT_SIZE = 30;

    private PaginationUtil () {}

    public static Pageable fromPage(int page) {
        return Pageable.ofSize(DEFAULT_SIZE).withPage(page);
    }
}
