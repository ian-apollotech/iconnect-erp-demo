// src/test/java/com/puregold/iconnect/product/ProductAttributeApiTest.java
package com.puregold.iconnect.product;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class ProductAttributeApiTest {
    @Autowired MockMvc mvc;

    @Test
    void createThenList() throws Exception {
        String body = """
            {"name":"Test Attr","code":"ATT-TEST","allowedValues":"A, B, C"}
            """;

        mvc.perform(post("/backend/product-attributes").contentType("application/json").content(body))
           .andExpect(status().isCreated())
           .andExpect(jsonPath("$.allowedValues").value("A, B, C"));

        mvc.perform(get("/backend/product-attributes"))
           .andExpect(status().isOk())
           .andExpect(jsonPath("$[?(@.code=='ATT-TEST')]").exists());
    }

    @Test
    void rejectsBlankName() throws Exception {
        String body = """
            {"code":"ATT-X","allowedValues":"A"}
            """;
        mvc.perform(post("/backend/product-attributes").contentType("application/json").content(body))
           .andExpect(status().isBadRequest());
    }
}
