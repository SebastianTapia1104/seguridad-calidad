package com.duoc.backend.rest;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

import com.duoc.backend.dto.LoginRequest;
import com.duoc.backend.dto.LoginResponse;

/**
 * Pruebas HTTP reales (puerto aleatorio) del login REST: validación {@code @Valid},
 * credenciales erróneas y usuario inexistente.
 */
public class AuthRestControllerTest extends RestApiTestSupport {

	@Test
	void loginExitosoRetorna200YToken() {
		LoginRequest loginRequest = new LoginRequest("admin", "admin123");
		HttpEntity<LoginRequest> requestEntity = new HttpEntity<>(loginRequest, jsonHeaders(null));
		ResponseEntity<LoginResponse> response = restTemplate.postForEntity(loginUrl(), requestEntity, LoginResponse.class);

		assertEquals(HttpStatus.OK, response.getStatusCode());
		assertNotNull(response.getBody());
		assertNotNull(response.getBody().token());
		assertEquals("Bearer", response.getBody().tokenType());
		assertEquals("admin", response.getBody().username());
		assertEquals("ADMIN", response.getBody().rol());
	}

	@Test
	void loginCredencialesInvalidasRetorna401YMensaje() throws Exception {
		LoginRequest loginRequest = new LoginRequest("admin", "wrongpassword");
		HttpEntity<LoginRequest> requestEntity = new HttpEntity<>(loginRequest, jsonHeaders(null));
		ResponseEntity<String> response = restTemplate.postForEntity(loginUrl(), requestEntity, String.class);

		assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
		assertEquals("Usuario o contraseña inválidos", readErrorField(response.getBody()));
	}

	@Test
	void loginUsuarioInexistenteRetorna401YMensaje() throws Exception {
		LoginRequest loginRequest = new LoginRequest("nonexistent", "password");
		HttpEntity<LoginRequest> requestEntity = new HttpEntity<>(loginRequest, jsonHeaders(null));
		ResponseEntity<String> response = restTemplate.postForEntity(loginUrl(), requestEntity, String.class);

		assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
		assertEquals("Usuario no encontrado: nonexistent", readErrorField(response.getBody()));
	}

	@Test
	void loginUsernameVacioRetorna400YDetalleValidacion() throws Exception {
		LoginRequest loginRequest = new LoginRequest("", "any");
		HttpEntity<LoginRequest> requestEntity = new HttpEntity<>(loginRequest, jsonHeaders(null));
		ResponseEntity<String> response = restTemplate.postForEntity(loginUrl(), requestEntity, String.class);

		assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
		assertEquals("username: must not be blank", readErrorField(response.getBody()));
	}

	@Test
	void loginPasswordVacioRetorna400YDetalleValidacion() throws Exception {
		LoginRequest loginRequest = new LoginRequest("admin", "");
		HttpEntity<LoginRequest> requestEntity = new HttpEntity<>(loginRequest, jsonHeaders(null));
		ResponseEntity<String> response = restTemplate.postForEntity(loginUrl(), requestEntity, String.class);

		assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
		assertEquals("password: must not be blank", readErrorField(response.getBody()));
	}

	@Test
	void loginCuerpoSinCamposRetorna400PrimerErrorDeValidacion() throws Exception {
		String url = loginUrl();
		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON);
		HttpEntity<String> requestEntity = new HttpEntity<>("{}", headers);
		ResponseEntity<String> response = restTemplate.postForEntity(url, requestEntity, String.class);

		assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
		assertEquals("username: must not be blank", readErrorField(response.getBody()));
	}
}
