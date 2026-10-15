// src/test/java/com/puregold/iconnect/product/ProductApiTest.java
package com.puregold.iconnect.product;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
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
    @Autowired ObjectMapper mapper;

    @Test
    void createComputesMargin() throws Exception {
        String body = """
            {"description":"Test Milk 1L","standardCost":58.20,"baseRetail":72.00,"status":"Active"}
            """;
        mvc.perform(post("/backend/products").contentType("application/json").content(body))
           .andExpect(status().isCreated())
           .andExpect(jsonPath("$.marginPct").value(19.2));
    }

    @Test
    void rejectsBlankDescription() throws Exception {
        mvc.perform(post("/backend/products").contentType("application/json")
               .content("{\"description\":\"\"}"))
           .andExpect(status().isBadRequest())
           .andExpect(jsonPath("$.errors.description").exists());
    }

    @Test
    void createAndUpdateWithAttributes() throws Exception {
        String body = """
            {"description":"Attr Test Product","standardCost":10.00,"baseRetail":15.00,"status":"Active",
             "attributes":[{"name":"Net Content","value":"100 g"},{"name":"Packaging","value":"Sachet"}]}
            """;

        String createResponse = mvc.perform(post("/backend/products").contentType("application/json").content(body))
           .andExpect(status().isCreated())
           .andExpect(jsonPath("$.attributes.length()").value(2))
           .andExpect(jsonPath("$.attributes[0].name").value("Net Content"))
           .andExpect(jsonPath("$.attributes[0].value").value("100 g"))
           .andReturn().getResponse().getContentAsString();
        Long id = mapper.readTree(createResponse).get("id").asLong();

        mvc.perform(get("/backend/products/" + id))
           .andExpect(status().isOk())
           .andExpect(jsonPath("$.attributes.length()").value(2));

        String updateBody = """
            {"description":"Attr Test Product","standardCost":10.00,"baseRetail":15.00,"status":"Active",
             "attributes":[{"name":"Flavor","value":"Classic"}]}
            """;
        mvc.perform(put("/backend/products/" + id).contentType("application/json").content(updateBody))
           .andExpect(status().isOk())
           .andExpect(jsonPath("$.attributes.length()").value(1))
           .andExpect(jsonPath("$.attributes[0].name").value("Flavor"))
           .andExpect(jsonPath("$.attributes[0].value").value("Classic"));
    }
}
