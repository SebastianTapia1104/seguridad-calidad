package com.duoc.backend.rest;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.resttestclient.TestRestTemplate;
import org.springframework.boot.resttestclient.autoconfigure.AutoConfigureTestRestTemplate;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;

import com.duoc.backend.BackendApplication;
import com.duoc.backend.dto.LoginRequest;
import com.duoc.backend.dto.LoginResponse;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

@SpringBootTest(classes = BackendApplication.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureTestRestTemplate
@ActiveProfiles("test")
public abstract class RestApiTestSupport {

	private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

	@LocalServerPort
	protected int port;

	@Autowired
	protected TestRestTemplate restTemplate;

	protected String baseUrl() {
		return "http://localhost:" + port;
	}

	protected String loginUrl() {
		return baseUrl() + "/api/auth/login";
	}

	/** Obtiene un JWT válido vía POST /api/auth/login. */
	protected String bearerToken(String username, String password) {
		LoginRequest loginRequest = new LoginRequest(username, password);
		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON);
		HttpEntity<LoginRequest> loginEntity = new HttpEntity<>(loginRequest, headers);
		ResponseEntity<LoginResponse> loginResponse = restTemplate.postForEntity(loginUrl(), loginEntity, LoginResponse.class);
		assertEquals(HttpStatus.OK, loginResponse.getStatusCode());
		assertNotNull(loginResponse.getBody());
		return "Bearer " + loginResponse.getBody().token();
	}

	protected HttpHeaders jsonHeaders(String bearerAuthorizationValue) {
		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON);
		if (bearerAuthorizationValue != null) {
			headers.set(HttpHeaders.AUTHORIZATION, bearerAuthorizationValue);
		}
		return headers;
	}

	protected String readErrorField(String jsonBody) throws Exception {
		JsonNode root = OBJECT_MAPPER.readTree(jsonBody);
		JsonNode err = root.path("error");
		return err.isMissingNode() ? "" : err.asText();
	}

}
