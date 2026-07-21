// src/test/java/com/puregold/iconnect/common/ApiExceptionHandlerTest.java
package com.puregold.iconnect.common;

import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;

class ApiExceptionHandlerTest {
    @Test
    void notFoundCarriesMessage() {
        NotFoundException ex = new NotFoundException("Company 99 not found");
        assertThat(ex.getMessage()).isEqualTo("Company 99 not found");
    }
}
