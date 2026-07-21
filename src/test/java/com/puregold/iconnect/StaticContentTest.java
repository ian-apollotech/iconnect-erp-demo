package com.puregold.iconnect;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class StaticContentTest {
    @Autowired TestRestTemplate rest;

    @Test
    void servesMockupAtRoot() {
        String body = rest.getForObject("/", String.class);
        assertThat(body).contains("<title>iConnect ERP");
    }
}
