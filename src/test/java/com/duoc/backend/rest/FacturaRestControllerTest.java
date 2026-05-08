package com.duoc.backend.rest;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

import com.duoc.backend.dto.FacturaGenerarRequest;
import com.duoc.backend.dto.LineaFacturaDto;
import com.duoc.backend.dto.LoginRequest;
import com.duoc.backend.dto.LoginResponse;

/**
 * Integración de {@code /api/facturas}: autenticación JWT, validaciones y ramas de {@link com.duoc.backend.services.FacturaService}.
 */
class FacturaRestControllerTest extends RestApiTestSupport {

	private String tokenAdmin;

	@BeforeEach
	void loginAdmin() {
		tokenAdmin = bearerToken("admin", "admin123");
	}

	private HttpEntity<FacturaGenerarRequest> generarEntity(FacturaGenerarRequest body) {
		return new HttpEntity<>(body, jsonHeaders(tokenAdmin));
	}

	@Test
	void generarSinTokenRetorna403() {
		String url = baseUrl() + "/api/facturas/generar";
		FacturaGenerarRequest body = new FacturaGenerarRequest(
				1L,
				null,
				null,
				null,
				null,
				List.of(new LineaFacturaDto("Consulta", new BigDecimal("15000.00"))));
		ResponseEntity<String> response = restTemplate.postForEntity(url, new HttpEntity<>(body, jsonHeaders(null)), String.class);

		assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
	}

	@Test
	void generarAuthorizationSinPrefijoBearerRetorna403() {
		String url = baseUrl() + "/api/facturas/generar";
		FacturaGenerarRequest body = new FacturaGenerarRequest(
				1L,
				null,
				null,
				null,
				null,
				List.of(new LineaFacturaDto("Consulta", new BigDecimal("1.00"))));
		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON);
		headers.set(HttpHeaders.AUTHORIZATION, tokenAdmin.replace("Bearer ", ""));
		ResponseEntity<String> response = restTemplate.postForEntity(url, new HttpEntity<>(body, headers), String.class);

		assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
	}

	@Test
	void generarJwtInvalidoRetorna403() {
		String url = baseUrl() + "/api/facturas/generar";
		FacturaGenerarRequest body = new FacturaGenerarRequest(
				1L,
				null,
				null,
				null,
				null,
				List.of(new LineaFacturaDto("Consulta", new BigDecimal("1.00"))));
		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON);
		headers.setBearerAuth("no-es-un-jwt");
		ResponseEntity<String> response = restTemplate.postForEntity(url, new HttpEntity<>(body, headers), String.class);

		assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
	}

	@Test
	void generarLineasVaciasRetorna400Validacion() throws Exception {
		String url = baseUrl() + "/api/facturas/generar";
		String json = """
				{"visitaId": 1, "lineas": []}
				""";
		ResponseEntity<String> response = restTemplate.postForEntity(url, new HttpEntity<>(json, jsonHeaders(tokenAdmin)), String.class);

		assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
		assertEquals("lineas: must not be empty", readErrorField(response.getBody()));
	}

	@Test
	void generarLineaConceptoVacioRetorna400() throws Exception {
		String url = baseUrl() + "/api/facturas/generar";
		String json = """
				{"visitaId": 1, "lineas": [{"concepto": "   ", "monto": 100}]}
				""";
		ResponseEntity<String> response = restTemplate.postForEntity(url, new HttpEntity<>(json, jsonHeaders(tokenAdmin)), String.class);

		assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
		assertEquals("lineas[0].concepto: must not be blank", readErrorField(response.getBody()));
	}

	@Test
	void generarLineaMontoNuloRetorna400() throws Exception {
		String url = baseUrl() + "/api/facturas/generar";
		String json = """
				{"visitaId": 1, "lineas": [{"concepto": "Item"}]}
				""";
		ResponseEntity<String> response = restTemplate.postForEntity(url, new HttpEntity<>(json, jsonHeaders(tokenAdmin)), String.class);

		assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
		assertEquals("lineas[0].monto: must not be null", readErrorField(response.getBody()));
	}

	@Test
	void generarVisitaInexistenteRetorna400() throws Exception {
		String url = baseUrl() + "/api/facturas/generar";
		FacturaGenerarRequest body = new FacturaGenerarRequest(
				999999L,
				null,
				null,
				null,
				null,
				List.of(new LineaFacturaDto("Servicio", BigDecimal.ONE)));
		ResponseEntity<String> response = restTemplate.postForEntity(url, new HttpEntity<>(body, jsonHeaders(tokenAdmin)), String.class);

		assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
		assertEquals("Visita no encontrada: 999999", readErrorField(response.getBody()));
	}

	@Test
	void generarNuevaVisitaSinCamposObligatoriosRetorna400() throws Exception {
		String url = baseUrl() + "/api/facturas/generar";
		FacturaGenerarRequest body = new FacturaGenerarRequest(
				null,
				null,
				null,
				1L,
				2L,
				List.of(new LineaFacturaDto("Item", BigDecimal.ONE)));
		ResponseEntity<String> response = restTemplate.postForEntity(url, new HttpEntity<>(body, jsonHeaders(tokenAdmin)), String.class);

		assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
		assertEquals(
				"Para nueva visita se requiere fecha, motivo, mascotaId y veterinarioId",
				readErrorField(response.getBody()));
	}

	@Test
	void generarNuevaVisitaMascotaInexistenteRetorna400() throws Exception {
		String url = baseUrl() + "/api/facturas/generar";
		FacturaGenerarRequest body = new FacturaGenerarRequest(
				null,
				LocalDateTime.now(),
				"Motivo",
				999999L,
				2L,
				List.of(new LineaFacturaDto("Item", BigDecimal.ONE)));
		ResponseEntity<String> response = restTemplate.postForEntity(url, new HttpEntity<>(body, jsonHeaders(tokenAdmin)), String.class);

		assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
		assertEquals("Mascota no encontrada", readErrorField(response.getBody()));
	}

	@Test
	void generarNuevaVisitaVeterinarioInexistenteRetorna400() throws Exception {
		String url = baseUrl() + "/api/facturas/generar";
		FacturaGenerarRequest body = new FacturaGenerarRequest(
				null,
				LocalDateTime.now(),
				"Motivo",
				1L,
				999999L,
				List.of(new LineaFacturaDto("Item", BigDecimal.ONE)));
		ResponseEntity<String> response = restTemplate.postForEntity(url, new HttpEntity<>(body, jsonHeaders(tokenAdmin)), String.class);

		assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
		assertEquals("Veterinario no encontrado", readErrorField(response.getBody()));
	}

	@Test
	void generarNuevaVisitaUsuarioNoVeterinarioRetorna400() throws Exception {
		String url = baseUrl() + "/api/facturas/generar";
		FacturaGenerarRequest body = new FacturaGenerarRequest(
				null,
				LocalDateTime.now(),
				"Motivo",
				1L,
				3L,
				List.of(new LineaFacturaDto("Item", BigDecimal.ONE)));
		ResponseEntity<String> response = restTemplate.postForEntity(url, new HttpEntity<>(body, jsonHeaders(tokenAdmin)), String.class);

		assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
		assertEquals("El usuario no es un veterinario válido", readErrorField(response.getBody()));
	}

	@Test
	void generarConVisitaExistenteRetorna201YLuegoDuplicado400() throws Exception {
		String url = baseUrl() + "/api/facturas/generar";
		FacturaGenerarRequest body = new FacturaGenerarRequest(
				1L,
				null,
				null,
				null,
				null,
				List.of(new LineaFacturaDto("Consulta", new BigDecimal("25000.50"))));

		ResponseEntity<Map<String, Object>> first = restTemplate.exchange(
				url,
				HttpMethod.POST,
				generarEntity(body),
				new ParameterizedTypeReference<Map<String, Object>>() {
				});
		assertEquals(HttpStatus.CREATED, first.getStatusCode());
		assertEquals(0, new BigDecimal("25000.50").compareTo(new BigDecimal(first.getBody().get("total").toString())));

		ResponseEntity<String> duplicate = restTemplate.postForEntity(url, generarEntity(body), String.class);
		assertEquals(HttpStatus.BAD_REQUEST, duplicate.getStatusCode());
		assertEquals("Ya existe factura para esta visita", readErrorField(duplicate.getBody()));
	}

	@Test
	void generarVisita2DuplicadaEnSegundaLlamada() throws Exception {
		String url = baseUrl() + "/api/facturas/generar";
		FacturaGenerarRequest body = new FacturaGenerarRequest(
				2L,
				null,
				null,
				null,
				null,
				List.of(new LineaFacturaDto("Control", new BigDecimal("10000.00"))));

		ResponseEntity<Map<String, Object>> ok = restTemplate.exchange(
				url,
				HttpMethod.POST,
				generarEntity(body),
				new ParameterizedTypeReference<Map<String, Object>>() {
				});
		assertEquals(HttpStatus.CREATED, ok.getStatusCode());

		ResponseEntity<String> dup = restTemplate.postForEntity(url, generarEntity(body), String.class);
		assertEquals(HttpStatus.BAD_REQUEST, dup.getStatusCode());
		assertEquals("Ya existe factura para esta visita", readErrorField(dup.getBody()));
	}

	@Test
	void generarConNuevaVisitaRetorna201() {
		String url = baseUrl() + "/api/facturas/generar";
		FacturaGenerarRequest body = new FacturaGenerarRequest(
				null,
				LocalDateTime.now(),
				"Chequeo integración",
				3L,
				2L,
				List.of(new LineaFacturaDto("Radiografía", new BigDecimal("45000.00"))));

		ResponseEntity<Map<String, Object>> response = restTemplate.exchange(
				url,
				HttpMethod.POST,
				generarEntity(body),
				new ParameterizedTypeReference<Map<String, Object>>() {
				});

		assertEquals(HttpStatus.CREATED, response.getStatusCode());
		assertNotNull(response.getBody().get("id"));
	}

	@Test
	void imprimirSinTokenRetorna403() {
		String url = baseUrl() + "/api/facturas/1/imprimir";
		ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.GET, new HttpEntity<>(jsonHeaders(null)), String.class);

		assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
	}

	@Test
	void imprimirFacturaInexistenteRetorna400() throws Exception {
		String url = baseUrl() + "/api/facturas/999999/imprimir";
		ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.GET, new HttpEntity<>(jsonHeaders(tokenAdmin)), String.class);

		assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
		assertEquals("Factura no encontrada: 999999", readErrorField(response.getBody()));
	}

	@Test
	void imprimirFacturaExistenteRetornaPdf() {
		String generarUrl = baseUrl() + "/api/facturas/generar";
		FacturaGenerarRequest body = new FacturaGenerarRequest(
				null,
				LocalDateTime.now(),
				"Visita para PDF",
				1L,
				2L,
				List.of(new LineaFacturaDto("Item", new BigDecimal("5000.00"))));
		ResponseEntity<Map<String, Object>> created = restTemplate.exchange(
				generarUrl,
				HttpMethod.POST,
				generarEntity(body),
				new ParameterizedTypeReference<Map<String, Object>>() {
				});
		assertEquals(HttpStatus.CREATED, created.getStatusCode());
		Long facturaId = ((Number) created.getBody().get("id")).longValue();

		String imprimirUrl = baseUrl() + "/api/facturas/" + facturaId + "/imprimir";
		ResponseEntity<byte[]> response = restTemplate.exchange(
				imprimirUrl,
				HttpMethod.GET,
				new HttpEntity<>(jsonHeaders(tokenAdmin)),
				byte[].class);

		assertEquals(HttpStatus.OK, response.getStatusCode());
		assertNotNull(response.getBody());
		assertTrue(response.getBody().length > 0);
		assertEquals(MediaType.APPLICATION_PDF, response.getHeaders().getContentType());
		String cd = response.getHeaders().getFirst(HttpHeaders.CONTENT_DISPOSITION);
		assertNotNull(cd);
		assertTrue(cd.contains("factura-" + facturaId + ".pdf"));
	}

	@Test
	void enviarSinTokenRetorna403() {
		String url = baseUrl() + "/api/facturas/1/enviar";
		ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.POST, new HttpEntity<>(jsonHeaders(null)), String.class);

		assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
	}

	@Test
	void enviarFacturaInexistenteRetorna400() throws Exception {
		String url = baseUrl() + "/api/facturas/999999/enviar";
		ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.POST, new HttpEntity<>(jsonHeaders(tokenAdmin)), String.class);

		assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
		assertEquals("Factura no encontrada: 999999", readErrorField(response.getBody()));
	}

	@Test
	void enviarFacturaExistenteRetorna200YMensaje() {
		String generarUrl = baseUrl() + "/api/facturas/generar";
		FacturaGenerarRequest body = new FacturaGenerarRequest(
				null,
				LocalDateTime.now().minusHours(1),
				"Visita para envío correo",
				2L,
				2L,
				List.of(new LineaFacturaDto("Otro", new BigDecimal("3000.00"))));
		ResponseEntity<Map<String, Object>> created = restTemplate.exchange(
				generarUrl,
				HttpMethod.POST,
				generarEntity(body),
				new ParameterizedTypeReference<Map<String, Object>>() {
				});
		assertEquals(HttpStatus.CREATED, created.getStatusCode());
		Long facturaId = ((Number) created.getBody().get("id")).longValue();

		String enviarUrl = baseUrl() + "/api/facturas/" + facturaId + "/enviar";
		ResponseEntity<Map<String, String>> response = restTemplate.exchange(
				enviarUrl,
				HttpMethod.POST,
				new HttpEntity<>(jsonHeaders(tokenAdmin)),
				new ParameterizedTypeReference<Map<String, String>>() {
				});

		assertEquals(HttpStatus.OK, response.getStatusCode());
		assertEquals(
				"Solicitud de envío procesada (revisa logs si SMTP no está disponible)",
				response.getBody().get("mensaje"));
	}

	@Test
	void facturasRequiereJwtValidoUsuarioAutenticado() {
		String loginUrl = baseUrl() + "/api/auth/login";
		HttpHeaders h = new HttpHeaders();
		h.setContentType(MediaType.APPLICATION_JSON);
		ResponseEntity<LoginResponse> login = restTemplate.postForEntity(
				loginUrl,
				new HttpEntity<>(new LoginRequest("vet", "vet123"), h),
				LoginResponse.class);
		assertEquals(HttpStatus.OK, login.getStatusCode());
		String vetToken = "Bearer " + login.getBody().token();

		String url = baseUrl() + "/api/facturas/generar";
		FacturaGenerarRequest body = new FacturaGenerarRequest(
				null,
				LocalDateTime.now(),
				"Con token vet",
				2L,
				2L,
				List.of(new LineaFacturaDto("Solo vet", new BigDecimal("100.00"))));
		ResponseEntity<Map<String, Object>> response = restTemplate.exchange(
				url,
				HttpMethod.POST,
				new HttpEntity<>(body, jsonHeaders(vetToken)),
				new ParameterizedTypeReference<Map<String, Object>>() {
				});
		assertEquals(HttpStatus.CREATED, response.getStatusCode());
	}
}
