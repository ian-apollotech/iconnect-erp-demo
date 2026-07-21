// src/test/java/com/puregold/iconnect/product/ProductApiTest.java
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
class ProductApiTest {
    @Autowired MockMvc mvc;

    @Test
    void createComputesMargin() throws Exception {
        String body = """
            {"description":"Test Milk 1L","standardCost":58.20,"baseRetail":72.00,"status":"Active"}
            """;
        mvc.perform(post("/api/products").contentType("application/json").content(body))
           .andExpect(status().isCreated())
           .andExpect(jsonPath("$.marginPct").value(19.2));
    }

    @Test
    void rejectsBlankDescription() throws Exception {
        mvc.perform(post("/api/products").contentType("application/json")
               .content("{\"description\":\"\"}"))
           .andExpect(status().isBadRequest())
           .andExpect(jsonPath("$.errors.description").exists());
    }
}
