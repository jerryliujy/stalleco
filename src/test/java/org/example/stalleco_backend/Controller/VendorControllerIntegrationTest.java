package org.example.stalleco_backend.Controller;

import org.example.stalleco_backend.model.Vendor;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.*;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class VendorControllerIntegrationTest {

    @Autowired
    private TestRestTemplate restTemplate;

    @Test
    public void testRegisterLoginAndSearch() {
        // 1. 测试注册
        Vendor newVendor = new Vendor();
        newVendor.setUsername("testvendor");
        newVendor.setPassword("123456");
        newVendor.setStallName("美食摊位");
        newVendor.setDescription("主营烧烤");
        newVendor.setLongitude(116.397128);
        newVendor.setLatitude(39.916527);

        ResponseEntity<Vendor> registerResponse = restTemplate.postForEntity(
                "/vendors/register", newVendor, Vendor.class);
        assertThat(registerResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        Vendor registeredVendor = registerResponse.getBody();
        assertThat(registeredVendor).isNotNull();
        assertThat(registeredVendor.getId()).isNotNull();

        // 2. 测试登录
        // 构造登录所需的请求体
        String loginJson = "{\"username\":\"testvendor\", \"password\":\"123456\"}";
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<String> loginRequest = new HttpEntity<>(loginJson, headers);

        ResponseEntity<Vendor> loginResponse = restTemplate.postForEntity(
                "/vendors/login", loginRequest, Vendor.class);
        assertThat(loginResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        Vendor loginVendor = loginResponse.getBody();
        assertThat(loginVendor).isNotNull();
        assertThat(loginVendor.getUsername()).isEqualTo("testvendor");

        // 3. 测试检索
        ResponseEntity<Vendor[]> searchResponse = restTemplate.getForEntity(
                "/vendors?q=美食", Vendor[].class);
        assertThat(searchResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        Vendor[] vendors = searchResponse.getBody();
        assertThat(vendors).isNotEmpty();
    }
}
