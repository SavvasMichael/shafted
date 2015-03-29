package org.savvas.shafted.controller;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.savvas.shafted.Application;
import org.savvas.shafted.controller.error.ErrorResponse;
import org.savvas.shafted.controller.request.RegistrationRequest;
import org.savvas.shafted.domain.ShaftUser;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.IntegrationTest;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.boot.test.TestRestTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;

import java.net.URI;

import static org.junit.Assert.*;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = Application.class)
@WebAppConfiguration
@IntegrationTest("server.port=0")
public class RegistrationControllerIntegrationTest {

    @Value("${local.server.port}")
    private int port;

    private final TestRestTemplate rest = new TestRestTemplate();

    @Test
    public void registeringANewUserReturnsACreatedResponseCode() {
        // Given
        String baseUrl = "http://localhost:" + port;
        String registrationUrl = baseUrl + "/registration";
        RegistrationRequest request = new RegistrationRequest("michaelsavvas@ymail.com", "savvas", "password");
        // When
        ResponseEntity<String> registrationResponse = rest.postForEntity(URI.create(registrationUrl), request, String.class);
        String userPath = registrationResponse.getHeaders().getFirst("Location");
        ResponseEntity<ShaftUser> userResponse = rest.getForEntity(URI.create(baseUrl + userPath), ShaftUser.class);
        // Then
        assertEquals("Unexpected response code.", 201, registrationResponse.getStatusCode().value());
        assertEquals("Unexpected Location Header.", "/user/1", userPath);
        ShaftUser shaftUser = userResponse.getBody();
        assertEquals("michaelsavvas@ymail.com", shaftUser.getEmail());
        assertEquals("savvas", shaftUser.getName());
        assertEquals("password", shaftUser.getPassword());
    }

    @Test
    public void invalidEmailReturnsBadRequest() {
        // Given
        String registrationUrl = "http://localhost:" + port + "/registration";
        RegistrationRequest request = new RegistrationRequest("michaelsavvas", "savvas", "password");
        // When
        ResponseEntity<ErrorResponse> response = rest.postForEntity(URI.create(registrationUrl), request, ErrorResponse.class);

        // Then
        assertEquals("Unexpected response code.", 400, response.getStatusCode().value());
        assertEquals("Unexpected Error Message", "email", response.getBody().getErrors().get(0));
    }

    @Test
    public void emptyEmailReturnsBadRequest() {
        //given
        String registrationUrl = "http://localhost:" + port + "/registration";
        RegistrationRequest request = new RegistrationRequest("", "savvas", "password");
        //when
        ResponseEntity<ErrorResponse> response = rest.postForEntity(URI.create(registrationUrl), request, ErrorResponse.class);
        //then
        assertEquals("Unexpected response code.", 400, response.getStatusCode().value());
        assertEquals("Unexpected Error Message", "email", response.getBody().getErrors().get(0));

    }

    @Test
    public void missingEmailReturnsBadRequest() {
        //given
        String registrationUrl = "http://localhost:" + port + "/registration";
        RegistrationRequest request = new RegistrationRequest(null, "savvas", "password");
        //when
        ResponseEntity<ErrorResponse> response = rest.postForEntity(URI.create(registrationUrl), request, ErrorResponse.class);
        //then
        assertEquals("Unexpected response code.", 400, response.getStatusCode().value());
        assertEquals("Unexpected Error Message", "email", response.getBody().getErrors().get(0));
    }

    @Test
    public void missingNameReturnsBadRequest() {
        //given
        String registrationUrl = "http://localhost:" + port + "/registration";
        RegistrationRequest request = new RegistrationRequest("michaelsavvas@ymail.com", null, "password");
        //when
        ResponseEntity<ErrorResponse> response = rest.postForEntity(URI.create(registrationUrl), request, ErrorResponse.class);
        //then
        assertEquals("Unexpected response code.", 400, response.getStatusCode().value());
        assertEquals("Unexpected Error Message", "name", response.getBody().getErrors().get(0));
    }

    @Test
    public void emptyNameReturnsBadRequest() {
        //given
        String registrationUrl = "http://localhost:" + port + "/registration";
        RegistrationRequest request = new RegistrationRequest("michaelsavvas@ymail.com", "", "password");
        //when
        ResponseEntity<ErrorResponse> response = rest.postForEntity(URI.create(registrationUrl), request, ErrorResponse.class);
        //then
        assertEquals("Unexpected response code.", 400, response.getStatusCode().value());
        assertEquals("Unexpected Error Message", "name", response.getBody().getErrors().get(0));
    }

    @Test
    public void emptyPasswordReturnsBadRequest() {
        //given
        String registrationUrl = "http://localhost:" + port + "/registration";
        RegistrationRequest request = new RegistrationRequest("michaelsavvas@ymail.com", "Savvas", null);
        //when
        ResponseEntity<ErrorResponse> response = rest.postForEntity(URI.create(registrationUrl), request, ErrorResponse.class);
        //then
        assertEquals("Unexpected response code.", 400, response.getStatusCode().value());
        assertEquals("Unexpected Error Message", "password", response.getBody().getErrors().get(0));
    }

    @Test
    public void missingPasswordReturnsBadRequest() {
        //given
        String registrationUrl = "http://localhost:" + port + "/registration";
        RegistrationRequest request = new RegistrationRequest("michaelsavvas@ymail.com", "Savvas", "");
        //when
        ResponseEntity<ErrorResponse> response = rest.postForEntity(URI.create(registrationUrl), request, ErrorResponse.class);
        //then
        assertEquals("Unexpected response code.", 400, response.getStatusCode().value());
        assertEquals("Unexpected Error Message", "password", response.getBody().getErrors().get(0));
    }

    @Test
    public void registeringUserWithDuplicateEmailReturnsBadRequest() {
        //given
        String registrationUrl = "http://localhost:" + port + "/registration";
        RegistrationRequest request = new RegistrationRequest("michaelsavvas@ymail.com", "Savvas", "pass");
        RegistrationRequest request2 = new RegistrationRequest("michaelsavvas@ymail.com", "Savvas", "pass");
        //when
        ResponseEntity<ErrorResponse> response = rest.postForEntity(URI.create(registrationUrl), request, ErrorResponse.class);
        ResponseEntity<ErrorResponse> response2 = rest.postForEntity(URI.create(registrationUrl), request2, ErrorResponse.class);

        //then
        assertEquals(201, response.getStatusCode().value());
        assertEquals("This Email has already been used", 400, response2.getStatusCode().value());
        assertEquals("Unexpected error message", "This email has already been registered", response2.getBody().getErrors().get(0));
    }

    @Test
    public void registeringUserCreatesUniqueUuid() {
        //given
        String baseUrl = "http://localhost:" + port;
        String registrationUrl = "http://localhost:" + port + "/registration";
        RegistrationRequest request = new RegistrationRequest("michaelsavvas@ymail.com", "Savvas", "pass");
        RegistrationRequest request2 = new RegistrationRequest("savvas.a.michael@gmail.com", "Savvas", "pass");
        //when
        ResponseEntity<String> registrationResponse = rest.postForEntity(URI.create(registrationUrl), request, String.class);
        ResponseEntity<String> registrationResponse2 = rest.postForEntity(URI.create(registrationUrl), request2, String.class);
        String userPath = registrationResponse.getHeaders().getFirst("Location");
        String userPath2 = registrationResponse2.getHeaders().getFirst("Location");
        ResponseEntity<ShaftUser> userResponse = rest.getForEntity(URI.create(baseUrl + userPath), ShaftUser.class);
        ResponseEntity<ShaftUser> userResponse2 = rest.getForEntity(URI.create(baseUrl + userPath2), ShaftUser.class);
        //then
        assertTrue(!userResponse.getBody().getUuid().isEmpty());
        assertTrue(!userResponse2.getBody().getUuid().isEmpty());
        assertNotEquals(userResponse.getBody().getUuid(), userResponse2.getBody().getUuid());
    }

    @Test
    public void setsActivateTrueWhenValidUuid() {
        //given
        String baseUrl = "http://localhost:" + port;
        String registrationUrl = baseUrl + "/registration";
        String activationUrl = baseUrl + "/activation/";
        RegistrationRequest request = new RegistrationRequest("michaelsavvas@ymail.com", "savvas", "password");
        //when
        ResponseEntity<String> registrationResponse = rest.postForEntity(URI.create(registrationUrl), request, String.class);
        String userPath = registrationResponse.getHeaders().getFirst("Location");
        ResponseEntity<ShaftUser> userResponse = rest.getForEntity(URI.create(baseUrl + userPath), ShaftUser.class);
        String uuid = userResponse.getBody().getUuid();
        ResponseEntity<ShaftUser> activationResponse = rest.postForEntity(URI.create(activationUrl + uuid), null, ShaftUser.class);
        ResponseEntity<ShaftUser> userResponse2 = rest.getForEntity(URI.create(baseUrl + userPath), ShaftUser.class);
        //then
        assertEquals(true, userResponse2.getBody().isActivated());
    }

}