package lt.creditco.cupa.web.rest.util;

import java.util.Optional;
import lt.creditco.cupa.domain.MerchantOwnedEntity;
import lt.creditco.cupa.web.context.CupaApiContext;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;

/**
 * Utility class for handling access control in REST controllers.
 */
public class AccessControlHelper {

    /**
     * Check access to an entity and return the entity if access is granted.
     *
     * @param entity the entity to check access for
     * @param context the CUPA API context
     * @return ResponseEntity with the entity or appropriate error response
     */
    public static <T extends MerchantOwnedEntity> ResponseEntity<T> checkAccessAndReturn(
        Optional<T> entity,
        CupaApiContext.CupaApiContextData context
    ) {
        if (entity.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        try {
            T entityValue = entity.orElseThrow();
            context.checkAccessToEntity(entityValue);
            return ResponseEntity.ok(entityValue);
        } catch (AccessDeniedException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
    }

    /**
     * Check access to an entity and return the converted entity if access is granted.
     *
     * @param entity the entity to check access for
     * @param context the CUPA API context
     * @param converter function to convert the entity to the desired type
     * @return ResponseEntity with the converted entity or appropriate error response
     */
    public static <T extends MerchantOwnedEntity, R> ResponseEntity<R> checkAccessAndReturn(
        Optional<T> entity,
        CupaApiContext.CupaApiContextData context,
        java.util.function.Function<T, R> converter
    ) {
        if (entity.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        try {
            T entityValue = entity.orElseThrow();
            context.checkAccessToEntity(entityValue);
            return ResponseEntity.ok(converter.apply(entityValue));
        } catch (AccessDeniedException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
    }
}
