package io.github.jhipster.web.util;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.Optional;

public final class ResponseUtil {

    private ResponseUtil() {
    }

    public static <X> ResponseEntity<X> wrapOrNotFound(Optional<X> maybeResponse) {
        return wrapOrNotFound(maybeResponse, null);
    }

    public static <X> ResponseEntity<X> wrapOrNotFound(Optional<X> maybeResponse, HttpHeaders headers) {
        return maybeResponse
                .map(body -> ResponseEntity.ok().headers(headers == null ? new HttpHeaders() : headers).body(body))
                .orElse(new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }
}
