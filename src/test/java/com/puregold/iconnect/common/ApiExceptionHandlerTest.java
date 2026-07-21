// src/test/java/com/puregold/iconnect/common/ApiExceptionHandlerTest.java
package com.puregold.iconnect.common;

import org.junit.jupiter.api.Test;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import java.util.Map;
import static org.assertj.core.api.Assertions.assertThat;

class ApiExceptionHandlerTest {
    @Test
    void notFoundCarriesMessage() {
        NotFoundException ex = new NotFoundException("Company 99 not found");
        assertThat(ex.getMessage()).isEqualTo("Company 99 not found");
    }

    @Test
    void dataIntegrityViolationMapsTo400() {
        ApiExceptionHandler handler = new ApiExceptionHandler();
        ResponseEntity<Map<String, Object>> response =
            handler.handleDataIntegrityViolation(new DataIntegrityViolationException("unique constraint"));
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).containsKey("message");
    }

    @Test
    void notFoundHandlerIsNullMessageSafe() {
        ApiExceptionHandler handler = new ApiExceptionHandler();
        // NotFoundException constructed without a message — getMessage() is null.
        ResponseEntity<Map<String, Object>> response = handler.handleNotFound(new NotFoundException(null));
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(response.getBody().get("message")).isNotNull();
    }
}
