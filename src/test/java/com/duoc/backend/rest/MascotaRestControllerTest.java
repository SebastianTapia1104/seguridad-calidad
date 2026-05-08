package com.duoc.backend.rest;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
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

import com.duoc.backend.dto.MascotaDto;
import com.duoc.backend.dto.MascotaFormDto;
import com.duoc.backend.entities.EstadoAdopcion;
import com.duoc.backend.entities.GeneroMascota;

/**
 * Integración REST de {@code /api/mascotas}: rutas públicas, JWT, validación y errores de negocio.
 */
class MascotaRestControllerTest extends RestApiTestSupport {

	private String tokenAdmin;

	@BeforeEach
	void obtenerJwtAdmin() {
		tokenAdmin = bearerToken("admin", "admin123");
	}

	private MascotaFormDto dtoValido(String nombre) {
		return new MascotaFormDto(
				null,
				nombre,
				"Perro",
				"Calle",
				1,
				GeneroMascota.MACHO,
				"Santiago",
				EstadoAdopcion.DISPONIBLE,
				null);
	}

	@Test
	void catalogoSinAuthorizationRetorna200() {
		String url = baseUrl() + "/api/mascotas/catalogo";
		ResponseEntity<List<MascotaDto>> response = restTemplate.exchange(
				url,
				HttpMethod.GET,
				new HttpEntity<>(new HttpHeaders()),
				new ParameterizedTypeReference<List<MascotaDto>>() {
				});

		assertEquals(HttpStatus.OK, response.getStatusCode());
		assertNotNull(response.getBody());
		assertFalse(response.getBody().isEmpty());
	}

	@Test
	void catalogoConJwtMalFormadoRetorna403() {
		String url = baseUrl() + "/api/mascotas/catalogo";
		HttpHeaders headers = new HttpHeaders();
		headers.set(HttpHeaders.AUTHORIZATION, "Bearer token-invalido-no-jwt");
		ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.GET, new HttpEntity<>(headers), String.class);

		assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
	}

	@Test
	void buscarPorEspecieRetorna200() {
		String url = baseUrl() + "/api/mascotas/buscar?especie=Perro";
		ResponseEntity<List<MascotaDto>> response = restTemplate.exchange(
				url,
				HttpMethod.GET,
				new HttpEntity<>(new HttpHeaders()),
				new ParameterizedTypeReference<List<MascotaDto>>() {
				});

		assertEquals(HttpStatus.OK, response.getStatusCode());
		assertNotNull(response.getBody());
	}

	@Test
	void buscarPorGeneroEnumRetorna200() {
		String url = baseUrl() + "/api/mascotas/buscar?genero=HEMBRA";
		ResponseEntity<List<MascotaDto>> response = restTemplate.exchange(
				url,
				HttpMethod.GET,
				new HttpEntity<>(new HttpHeaders()),
				new ParameterizedTypeReference<List<MascotaDto>>() {
				});

		assertEquals(HttpStatus.OK, response.getStatusCode());
		assertNotNull(response.getBody());
	}

	@Test
	void buscarPorEdadYUbicacionRetorna200() {
		String url = baseUrl() + "/api/mascotas/buscar?edad=2&ubicacion=Valpara%C3%ADso";
		ResponseEntity<List<MascotaDto>> response = restTemplate.exchange(
				url,
				HttpMethod.GET,
				new HttpEntity<>(new HttpHeaders()),
				new ParameterizedTypeReference<List<MascotaDto>>() {
				});

		assertEquals(HttpStatus.OK, response.getStatusCode());
		assertNotNull(response.getBody());
	}

	@Test
	void crearSinTokenRetorna403() {
		String url = baseUrl() + "/api/mascotas";
		HttpEntity<MascotaFormDto> requestEntity = new HttpEntity<>(dtoValido("SinAuth"), jsonHeaders(null));
		ResponseEntity<String> response = restTemplate.postForEntity(url, requestEntity, String.class);

		assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
	}

	@Test
	void crearAuthorizationSinPrefijoBearerRetorna403() {
		String url = baseUrl() + "/api/mascotas";
		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON);
		headers.set(HttpHeaders.AUTHORIZATION, tokenAdmin.replace("Bearer ", ""));
		HttpEntity<MascotaFormDto> requestEntity = new HttpEntity<>(dtoValido("MalPrefijo"), headers);
		ResponseEntity<String> response = restTemplate.postForEntity(url, requestEntity, String.class);

		assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
	}

	@Test
	void crearConJwtInvalidoRetorna403() {
		String url = baseUrl() + "/api/mascotas";
		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON);
		headers.setBearerAuth("no-es-un-jwt");
		HttpEntity<MascotaFormDto> requestEntity = new HttpEntity<>(dtoValido("JwtMal"), headers);
		ResponseEntity<String> response = restTemplate.postForEntity(url, requestEntity, String.class);

		assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
	}

	@Test
	void crearNombreVacioRetorna400() throws Exception {
		String url = baseUrl() + "/api/mascotas";
		MascotaFormDto dto = new MascotaFormDto(
				null,
				"   ",
				"Perro",
				"X",
				1,
				GeneroMascota.MACHO,
				"Santiago",
				EstadoAdopcion.DISPONIBLE,
				null);
		ResponseEntity<String> response = restTemplate.postForEntity(url, new HttpEntity<>(dto, jsonHeaders(tokenAdmin)), String.class);

		assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
		assertEquals("nombre: must not be blank", readErrorField(response.getBody()));
	}

	@Test
	void crearEdadNegativaRetorna400() throws Exception {
		String url = baseUrl() + "/api/mascotas";
		MascotaFormDto dto = new MascotaFormDto(
				null,
				"X",
				"Perro",
				"X",
				-1,
				GeneroMascota.MACHO,
				"Santiago",
				EstadoAdopcion.DISPONIBLE,
				null);
		ResponseEntity<String> response = restTemplate.postForEntity(url, new HttpEntity<>(dto, jsonHeaders(tokenAdmin)), String.class);

		assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
		assertEquals("edad: must be greater than or equal to 0", readErrorField(response.getBody()));
	}

	@Test
	void crearGeneroNuloRetorna400() throws Exception {
		String url = baseUrl() + "/api/mascotas";
		String json = """
				{
				  "nombre": "X",
				  "especie": "Perro",
				  "raza": "X",
				  "edad": 1,
				  "ubicacion": "Santiago",
				  "estadoAdopcion": "DISPONIBLE"
				}
				""";
		HttpHeaders headers = jsonHeaders(tokenAdmin);
		HttpEntity<String> requestEntity = new HttpEntity<>(json, headers);
		ResponseEntity<String> response = restTemplate.postForEntity(url, requestEntity, String.class);

		assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
		assertEquals("genero: must not be null", readErrorField(response.getBody()));
	}

	@Test
	void crearConRolAdminRetorna201() {
		String url = baseUrl() + "/api/mascotas";
		ResponseEntity<MascotaDto> response = restTemplate.postForEntity(
				url,
				new HttpEntity<>(dtoValido("MascotaAdmin"), jsonHeaders(tokenAdmin)),
				MascotaDto.class);

		assertEquals(HttpStatus.CREATED, response.getStatusCode());
		assertNotNull(response.getBody());
		assertEquals("MascotaAdmin", response.getBody().nombre());
	}

	@Test
	void crearConRolVeterinarioRetorna201() {
		String url = baseUrl() + "/api/mascotas";
		String tokenVet = bearerToken("vet", "vet123");
		ResponseEntity<MascotaDto> response = restTemplate.postForEntity(
				url,
				new HttpEntity<>(dtoValido("MascotaVet"), jsonHeaders(tokenVet)),
				MascotaDto.class);

		assertEquals(HttpStatus.CREATED, response.getStatusCode());
		assertNotNull(response.getBody());
	}

	@Test
	void crearConRolRecepcionistaRetorna201() {
		String url = baseUrl() + "/api/mascotas";
		String tokenRecep = bearerToken("recep", "recep123");
		ResponseEntity<MascotaDto> response = restTemplate.postForEntity(
				url,
				new HttpEntity<>(dtoValido("MascotaRecep"), jsonHeaders(tokenRecep)),
				MascotaDto.class);

		assertEquals(HttpStatus.CREATED, response.getStatusCode());
		assertNotNull(response.getBody());
	}

	@Test
	void actualizarMascotaExistenteRetorna200() {
		String urlGet = baseUrl() + "/api/mascotas/catalogo";
		ResponseEntity<List<MascotaDto>> catalogo = restTemplate.exchange(
				urlGet,
				HttpMethod.GET,
				new HttpEntity<>(new HttpHeaders()),
				new ParameterizedTypeReference<List<MascotaDto>>() {
				});
		Long id = catalogo.getBody().get(0).id();

		String urlPut = baseUrl() + "/api/mascotas/" + id;
		MascotaFormDto dto = new MascotaFormDto(
				id,
				"NombreActualizado",
				"Perro",
				"X",
				2,
				GeneroMascota.MACHO,
				"Santiago",
				EstadoAdopcion.DISPONIBLE,
				null);

		ResponseEntity<MascotaDto> response = restTemplate.exchange(
				urlPut,
				HttpMethod.PUT,
				new HttpEntity<>(dto, jsonHeaders(tokenAdmin)),
				MascotaDto.class);

		assertEquals(HttpStatus.OK, response.getStatusCode());
		assertEquals("NombreActualizado", response.getBody().nombre());
	}

	@Test
	void actualizarNombreInvalidoRetorna400() throws Exception {
		String url = baseUrl() + "/api/mascotas/1";
		MascotaFormDto dto = new MascotaFormDto(
				1L,
				"",
				"Perro",
				"X",
				1,
				GeneroMascota.MACHO,
				"Santiago",
				EstadoAdopcion.DISPONIBLE,
				null);
		ResponseEntity<String> response = restTemplate.exchange(
				url,
				HttpMethod.PUT,
				new HttpEntity<>(dto, jsonHeaders(tokenAdmin)),
				String.class);

		assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
		assertEquals("nombre: must not be blank", readErrorField(response.getBody()));
	}

	@Test
	void actualizarMascotaInexistenteRetorna400() throws Exception {
		String url = baseUrl() + "/api/mascotas/999999";
		MascotaFormDto dto = new MascotaFormDto(
				999999L,
				"X",
				"Perro",
				"X",
				1,
				GeneroMascota.MACHO,
				"Santiago",
				EstadoAdopcion.DISPONIBLE,
				null);
		ResponseEntity<Map<String, String>> response = restTemplate.exchange(
				url,
				HttpMethod.PUT,
				new HttpEntity<>(dto, jsonHeaders(tokenAdmin)),
				new ParameterizedTypeReference<Map<String, String>>() {
				});

		assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
		assertEquals("Mascota no encontrada: 999999", response.getBody().get("error"));
	}

	@Test
	void eliminarMascotaCreadaRetorna200YMensaje() {
		String urlPost = baseUrl() + "/api/mascotas";
		ResponseEntity<MascotaDto> created = restTemplate.postForEntity(
				urlPost,
				new HttpEntity<>(dtoValido("ParaBorrar"), jsonHeaders(tokenAdmin)),
				MascotaDto.class);
		assertEquals(HttpStatus.CREATED, created.getStatusCode());
		Long id = created.getBody().id();

		String urlDelete = baseUrl() + "/api/mascotas/" + id;
		ResponseEntity<Map<String, String>> response = restTemplate.exchange(
				urlDelete,
				HttpMethod.DELETE,
				new HttpEntity<>(jsonHeaders(tokenAdmin)),
				new ParameterizedTypeReference<Map<String, String>>() {
				});

		assertEquals(HttpStatus.OK, response.getStatusCode());
		assertEquals("Mascota eliminada", response.getBody().get("mensaje"));
	}

	@Test
	void eliminarMascotaInexistenteRetorna400() throws Exception {
		String url = baseUrl() + "/api/mascotas/999999";
		ResponseEntity<Map<String, String>> response = restTemplate.exchange(
				url,
				HttpMethod.DELETE,
				new HttpEntity<>(jsonHeaders(tokenAdmin)),
				new ParameterizedTypeReference<Map<String, String>>() {
				});

		assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
		assertEquals("Mascota no encontrada: 999999", response.getBody().get("error"));
	}

	@Test
	void eliminarSinTokenRetorna403() {
		String url = baseUrl() + "/api/mascotas/1";
		ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.DELETE, new HttpEntity<>(jsonHeaders(null)), String.class);

		assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
	}
}
