// src/test/java/com/puregold/iconnect/vendor/VendorApiTest.java
package com.puregold.iconnect.vendor;

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
class VendorApiTest {
    @Autowired MockMvc mvc;
    @Autowired ObjectMapper mapper;

    @Test
    void createThenList() throws Exception {
        String body = """
            {"name":"Test Vendor Co","tin":"123-456-789","deliveryModel":"Direct-to-Store (DTS)",
             "paymentTerms":"30 Days","leadTime":"3 Days","minOrder":100.00,"rtvAllowed":"Yes",
             "registeredAddress":"1 Vendor St","contactPerson":"Jane Doe","phone":"09171234567",
             "email":"jane@vendor.com","status":"Active"}
            """;

        mvc.perform(post("/backend/vendors").contentType("application/json").content(body))
           .andExpect(status().isCreated())
           .andExpect(jsonPath("$.name").value("Test Vendor Co"))
           .andExpect(jsonPath("$.id").isNumber())
           .andExpect(jsonPath("$.code").value(org.hamcrest.Matchers.startsWith("V-")));

        mvc.perform(get("/backend/vendors"))
           .andExpect(status().isOk())
           .andExpect(jsonPath("$[?(@.name=='Test Vendor Co')]").exists());
    }

    @Test
    void rejectsBlankName() throws Exception {
        String body = """
            {"name":"","rtvAllowed":"Yes"}
            """;
        mvc.perform(post("/backend/vendors").contentType("application/json").content(body))
           .andExpect(status().isBadRequest())
           .andExpect(jsonPath("$.errors.name").exists());
    }

    private Long createVendor(String name, String vendorType) throws Exception {
        String body = """
            {"name":"%s","rtvAllowed":"Yes","vendorType":"%s"}
            """.formatted(name, vendorType);
        String response = mvc.perform(post("/backend/vendors").contentType("application/json").content(body))
           .andExpect(status().isCreated())
           .andReturn().getResponse().getContentAsString();
        JsonNode node = mapper.readTree(response);
        return node.get("id").asLong();
    }

    @Test
    void distributorTypeVendorCannotBeBrandManufacturer() throws Exception {
        Long distributorId = createVendor("Distributor Only Co " + System.nanoTime(), "Distributor");

        String brandBody = """
            {"name":"Bad Brand Type","code":"BR-BADTYPE-%d","manufacturerId":%d}
            """.formatted(System.nanoTime(), distributorId);

        mvc.perform(post("/backend/brands").contentType("application/json").content(brandBody))
           .andExpect(status().isBadRequest());
    }

    @Test
    void manufacturerTypeVendorCanBeBrandManufacturer() throws Exception {
        Long manufacturerId = createVendor("Manufacturer Only Co " + System.nanoTime(), "Manufacturer");

        String brandBody = """
            {"name":"Good Brand Type","code":"BR-GOODTYPE-%d","manufacturerId":%d}
            """.formatted(System.nanoTime(), manufacturerId);

        mvc.perform(post("/backend/brands").contentType("application/json").content(brandBody))
           .andExpect(status().isCreated());
    }
}
