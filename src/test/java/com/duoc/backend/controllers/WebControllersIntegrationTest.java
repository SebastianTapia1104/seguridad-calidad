package com.duoc.backend.controllers;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.net.CookieManager;
import java.net.CookiePolicy;
import java.net.URI;
import java.net.http.HttpClient;
import java.time.LocalDateTime;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.resttestclient.TestRestTemplate;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import org.springframework.boot.restclient.RestTemplateBuilder;

import com.duoc.backend.BackendApplication;
import com.duoc.backend.entities.Mascota;
import com.duoc.backend.entities.Usuario;
import com.duoc.backend.entities.Visita;
import com.duoc.backend.repositories.MascotaRepository;
import com.duoc.backend.repositories.UsuarioRepository;
import com.duoc.backend.repositories.VisitaRepository;

/**
 * Integración end-to-end de controladores Thymeleaf: sesión por cookies, CSRF de formularios,
 * redirecciones de seguridad (302 → login) y ramas de negocio en controladores MVC.
 */
@SpringBootTest(classes = BackendApplication.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@TestMethodOrder(OrderAnnotation.class)
class WebControllersIntegrationTest {

	private static final Pattern CSRF_INPUT = Pattern.compile(
			"name=\"_csrf\"\\s+value=\"([^\"]+)\"",
			Pattern.CASE_INSENSITIVE | Pattern.DOTALL);

	private static final Pattern ELIMINAR_ACTION = Pattern.compile("/admin/mascotas/(\\d+)/eliminar");

	@LocalServerPort
	private int port;

	@Autowired
	private VisitaRepository visitaRepository;

	@Autowired
	private MascotaRepository mascotaRepository;

	@Autowired
	private UsuarioRepository usuarioRepository;

	private String baseUrl() {
		return "http://localhost:" + port;
	}

	private String url(String path) {
		return path.startsWith("/") ? baseUrl() + path : baseUrl() + "/" + path;
	}

	private TestRestTemplate sessionClient(boolean followRedirects) {
		CookieManager cookieManager = new CookieManager();
		cookieManager.setCookiePolicy(CookiePolicy.ACCEPT_ALL);
		HttpClient.Builder httpClientBuilder = HttpClient.newBuilder().cookieHandler(cookieManager);
		if (!followRedirects) {
			httpClientBuilder.followRedirects(HttpClient.Redirect.NEVER);
		}
		HttpClient jdkHttpClient = httpClientBuilder.build();
		JdkClientHttpRequestFactory requestFactory = new JdkClientHttpRequestFactory(jdkHttpClient);
		RestTemplateBuilder builder = new RestTemplateBuilder().requestFactory(() -> requestFactory);
		return new TestRestTemplate(builder);
	}

	private String extractCsrf(String html) {
		Matcher m = CSRF_INPUT.matcher(html);
		assertTrue(m.find(), "No se encontró token CSRF (_csrf) en el HTML");
		return m.group(1);
	}

	private void login(TestRestTemplate client, String username, String password) {
		ResponseEntity<String> loginPage = client.getForEntity(url("/login"), String.class);
		assertEquals(HttpStatus.OK, loginPage.getStatusCode());
		String csrfLogin = extractCsrf(loginPage.getBody());

		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
		MultiValueMap<String, String> form = new LinkedMultiValueMap<>();
		form.add("username", username);
		form.add("password", password);
		form.add("_csrf", csrfLogin);
		ResponseEntity<String> posted = client.postForEntity(url("/login"), new HttpEntity<>(form, headers), String.class);
		assertTrue(posted.getStatusCode().is3xxRedirection(), "Login debe redirigir tras éxito");
		assertNotNull(posted.getHeaders().getLocation());
	}

	private Long crearVisitaSinFactura() {
		Usuario vet = usuarioRepository.findByUsername("vet").orElseThrow();
		Mascota mascota = mascotaRepository.findAll().stream()
				.filter(m -> "Rocky".equals(m.getNombre()))
				.findFirst()
				.orElseThrow();
		Visita v = new Visita(LocalDateTime.now(), "Visita extra integración", mascota, vet);
		return visitaRepository.save(v).getId();
	}

	@Test
	@Order(1)
	void loginPageRetornaVistaLogin() {
		TestRestTemplate client = sessionClient(true);
		ResponseEntity<String> r = client.getForEntity(url("/login"), String.class);
		assertEquals(HttpStatus.OK, r.getStatusCode());
		assertTrue(r.getBody().contains("Ingreso administrador"));
	}

	@Test
	@Order(2)
	void indexPublicoRetornaOk() {
		TestRestTemplate client = sessionClient(true);
		ResponseEntity<String> r = client.getForEntity(url("/"), String.class);
		assertEquals(HttpStatus.OK, r.getStatusCode());
		assertTrue(r.getBody().contains("Patitas") || r.getBody().toLowerCase().contains("mascota"));
	}

	@Test
	@Order(3)
	void buscarSinGeneroInvalidoRetornaOk() {
		TestRestTemplate client = sessionClient(true);
		ResponseEntity<String> r = client.getForEntity(url("/buscar?especie=Perro"), String.class);
		assertEquals(HttpStatus.OK, r.getStatusCode());
	}

	@Test
	@Order(4)
	void buscarConGeneroEnumValidoRetornaOk() {
		TestRestTemplate client = sessionClient(true);
		ResponseEntity<String> r = client.getForEntity(url("/buscar?genero=MACHO"), String.class);
		assertEquals(HttpStatus.OK, r.getStatusCode());
	}

	@Test
	@Order(5)
	void buscarConGeneroInvalidoEjecutaRamaCatch() {
		TestRestTemplate client = sessionClient(true);
		ResponseEntity<String> r = client.getForEntity(url("/buscar?genero=NO_EXISTE"), String.class);
		assertEquals(HttpStatus.OK, r.getStatusCode());
	}

	@Test
	@Order(6)
	void adminMascotasSinSesionRedirigeAlLoginCon302() {
		TestRestTemplate client = sessionClient(false);
		ResponseEntity<String> r = client.getForEntity(url("/admin/mascotas"), String.class);
		assertEquals(HttpStatus.FOUND, r.getStatusCode());
		assertNotNull(r.getHeaders().getLocation());
		assertTrue(r.getHeaders().getLocation().toString().contains("/login"));
	}

	@Test
	@Order(7)
	void adminFacturacionSinSesionRedirigeAlLoginCon302() {
		TestRestTemplate client = sessionClient(false);
		ResponseEntity<String> r = client.getForEntity(url("/admin/facturacion"), String.class);
		assertEquals(HttpStatus.FOUND, r.getStatusCode());
		assertNotNull(r.getHeaders().getLocation());
		assertTrue(r.getHeaders().getLocation().toString().contains("/login"));
	}

	@Test
	@Order(8)
	void adminMascotasConUsuarioNoAdminRetorna403() {
		TestRestTemplate client = sessionClient(true);
		login(client, "vet", "vet123");
		ResponseEntity<String> r = client.getForEntity(url("/admin/mascotas"), String.class);
		assertEquals(HttpStatus.FORBIDDEN, r.getStatusCode());
	}

	@Test
	@Order(9)
	void adminFacturacionConUsuarioNoAdminRetorna403() {
		TestRestTemplate client = sessionClient(true);
		login(client, "recep", "recep123");
		ResponseEntity<String> r = client.getForEntity(url("/admin/facturacion"), String.class);
		assertEquals(HttpStatus.FORBIDDEN, r.getStatusCode());
	}

	@Test
	@Order(10)
	void adminMascotasConAdminMuestraGestion() {
		TestRestTemplate client = sessionClient(true);
		login(client, "admin", "admin123");
		ResponseEntity<String> r = client.getForEntity(url("/admin/mascotas"), String.class);
		assertEquals(HttpStatus.OK, r.getStatusCode());
		assertTrue(r.getBody().contains("Gestión de mascotas"));
	}

	@Test
	@Order(11)
	void adminMascotasConEditIdValidoMuestraFormularioEdicion() {
		TestRestTemplate client = sessionClient(true);
		login(client, "admin", "admin123");
		ResponseEntity<String> r = client.getForEntity(url("/admin/mascotas?edit=1"), String.class);
		assertEquals(HttpStatus.OK, r.getStatusCode());
		assertTrue(r.getBody().contains("Editar mascota"));
	}

	@Test
	@Order(12)
	void adminMascotasConEditIdInexistenteProvocaErrorServidor() {
		TestRestTemplate client = sessionClient(true);
		login(client, "admin", "admin123");
		ResponseEntity<String> r = client.getForEntity(url("/admin/mascotas?edit=999999"), String.class);
		assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, r.getStatusCode());
	}

	@Test
	@Order(20)
	void generarFacturaDuplicadaMismaVisitaRetornaFlashError() {
		TestRestTemplate client = sessionClient(false);
		login(client, "admin", "admin123");

		ResponseEntity<String> page = client.getForEntity(url("/admin/facturacion"), String.class);
		String csrf = extractCsrf(page.getBody());
		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

		MultiValueMap<String, String> first = new LinkedMultiValueMap<>();
		first.add("_csrf", csrf);
		first.add("visitaId", "1");
		first.add("conceptos", "Primera");
		first.add("montos", "100");
		client.postForEntity(url("/admin/facturacion/generar"), new HttpEntity<>(first, headers), String.class);

		ResponseEntity<String> page2 = client.getForEntity(url("/admin/facturacion"), String.class);
		String csrf2 = extractCsrf(page2.getBody());
		MultiValueMap<String, String> dup = new LinkedMultiValueMap<>();
		dup.add("_csrf", csrf2);
		dup.add("visitaId", "1");
		dup.add("conceptos", "Duplicado");
		dup.add("montos", "200");
		ResponseEntity<String> posted = client.postForEntity(url("/admin/facturacion/generar"), new HttpEntity<>(dup, headers), String.class);
		assertEquals(HttpStatus.FOUND, posted.getStatusCode());

		ResponseEntity<String> after = client.getForEntity(url("/admin/facturacion"), String.class);
		assertTrue(after.getBody().contains("Ya existe una factura para esta visita"));
	}

	@Test
	@Order(21)
	void generarFacturaSinEmailRetornaRedirectYFlashCorrecto() {
		TestRestTemplate client = sessionClient(false);
		login(client, "admin", "admin123");

		ResponseEntity<String> page = client.getForEntity(url("/admin/facturacion"), String.class);
		String csrf = extractCsrf(page.getBody());

		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
		MultiValueMap<String, String> form = new LinkedMultiValueMap<>();
		form.add("_csrf", csrf);
		form.add("visitaId", "2");
		form.add("conceptos", "Consulta MVC");
		form.add("montos", "15000.00");
		ResponseEntity<String> posted = client.postForEntity(url("/admin/facturacion/generar"), new HttpEntity<>(form, headers), String.class);
		assertEquals(HttpStatus.FOUND, posted.getStatusCode());

		ResponseEntity<String> after = client.getForEntity(url("/admin/facturacion"), String.class);
		assertTrue(after.getBody().contains("Factura generada correctamente"));
		assertTrue(!after.getBody().contains("Factura generada y enviada por email"));
	}

	@Test
	@Order(22)
	void generarFacturaConEmailRetornaRedirectYFlashEnviado() {
		TestRestTemplate client = sessionClient(false);
		login(client, "admin", "admin123");

		Long visitaId = crearVisitaSinFactura();

		ResponseEntity<String> page = client.getForEntity(url("/admin/facturacion"), String.class);
		String csrf = extractCsrf(page.getBody());

		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
		MultiValueMap<String, String> form = new LinkedMultiValueMap<>();
		form.add("_csrf", csrf);
		form.add("visitaId", visitaId.toString());
		form.add("conceptos", "Control MVC");
		form.add("montos", "8000.00");
		form.add("enviarEmail", "true");
		ResponseEntity<String> posted = client.postForEntity(url("/admin/facturacion/generar"), new HttpEntity<>(form, headers), String.class);
		assertEquals(HttpStatus.FOUND, posted.getStatusCode());

		ResponseEntity<String> after = client.getForEntity(url("/admin/facturacion"), String.class);
		assertTrue(after.getBody().contains("Factura generada y enviada por email correctamente"));
	}

	@Test
	@Order(23)
	void generarFacturaVisitaInexistenteRetornaFlashError() {
		TestRestTemplate client = sessionClient(false);
		login(client, "admin", "admin123");

		ResponseEntity<String> page = client.getForEntity(url("/admin/facturacion"), String.class);
		String csrf = extractCsrf(page.getBody());

		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
		MultiValueMap<String, String> form = new LinkedMultiValueMap<>();
		form.add("_csrf", csrf);
		form.add("visitaId", "999999");
		form.add("conceptos", "X");
		form.add("montos", "1");
		ResponseEntity<String> posted = client.postForEntity(url("/admin/facturacion/generar"), new HttpEntity<>(form, headers), String.class);
		assertEquals(HttpStatus.FOUND, posted.getStatusCode());

		ResponseEntity<String> after = client.getForEntity(url("/admin/facturacion"), String.class);
		assertTrue(after.getBody().contains("Error al generar factura"));
		assertTrue(after.getBody().contains("Visita no encontrada"));
	}

	@Test
	@Order(24)
	void generarFacturaConceptoEnBlancoSaltaDetalle() {
		TestRestTemplate client = sessionClient(false);
		login(client, "admin", "admin123");

		Long visitaId = crearVisitaSinFactura();

		ResponseEntity<String> page = client.getForEntity(url("/admin/facturacion"), String.class);
		String csrf = extractCsrf(page.getBody());

		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
		MultiValueMap<String, String> form = new LinkedMultiValueMap<>();
		form.add("_csrf", csrf);
		form.add("visitaId", visitaId.toString());
		form.add("conceptos", "   ");
		form.add("montos", "5000");
		ResponseEntity<String> posted = client.postForEntity(url("/admin/facturacion/generar"), new HttpEntity<>(form, headers), String.class);
		assertEquals(HttpStatus.FOUND, posted.getStatusCode());
	}

	@Test
	@Order(30)
	void facturacionAdminListaVisitasYFacturas() {
		TestRestTemplate client = sessionClient(true);
		login(client, "admin", "admin123");
		ResponseEntity<String> r = client.getForEntity(url("/admin/facturacion"), String.class);
		assertEquals(HttpStatus.OK, r.getStatusCode());
		assertTrue(r.getBody().contains("Facturación"));
	}

	@Test
	@Order(40)
	void guardarNuevaMascotaRedirigeConFlashCreada() {
		TestRestTemplate client = sessionClient(false);
		login(client, "admin", "admin123");

		ResponseEntity<String> page = client.getForEntity(url("/admin/mascotas"), String.class);
		assertEquals(HttpStatus.OK, page.getStatusCode());
		String csrf = extractCsrf(page.getBody());

		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
		MultiValueMap<String, String> form = new LinkedMultiValueMap<>();
		form.add("_csrf", csrf);
		form.add("nombre", "MascotaWebTest");
		form.add("especie", "Perro");
		form.add("raza", "Mestizo");
		form.add("edad", "4");
		form.add("genero", "HEMBRA");
		form.add("ubicacion", "Concepción");
		form.add("estadoAdopcion", "DISPONIBLE");
		form.add("fotoUrl", "");
		ResponseEntity<String> posted = client.postForEntity(url("/admin/mascotas/guardar"), new HttpEntity<>(form, headers), String.class);
		assertEquals(HttpStatus.FOUND, posted.getStatusCode());
		URI loc = posted.getHeaders().getLocation();
		assertNotNull(loc);
		assertEquals("/admin/mascotas", loc.getPath());

		ResponseEntity<String> after = client.getForEntity(url("/admin/mascotas"), String.class);
		assertEquals(HttpStatus.OK, after.getStatusCode());
		assertTrue(after.getBody().contains("Mascota creada"));
	}

	@Test
	@Order(41)
	void guardarActualizarMascotaRedirigeConFlashActualizada() {
		TestRestTemplate client = sessionClient(false);
		login(client, "admin", "admin123");

		ResponseEntity<String> editPage = client.getForEntity(url("/admin/mascotas?edit=1"), String.class);
		assertEquals(HttpStatus.OK, editPage.getStatusCode());
		String body = editPage.getBody();
		String csrf = extractCsrf(body);

		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
		MultiValueMap<String, String> form = new LinkedMultiValueMap<>();
		form.add("_csrf", csrf);
		form.add("id", "1");
		form.add("nombre", "LunaRenombrada");
		form.add("especie", "Perro");
		form.add("raza", "Mestizo");
		form.add("edad", "3");
		form.add("genero", "HEMBRA");
		form.add("ubicacion", "Santiago");
		form.add("estadoAdopcion", "DISPONIBLE");
		form.add("fotoUrl", "https://placehold.co/400x300?text=Luna");
		ResponseEntity<String> posted = client.postForEntity(url("/admin/mascotas/guardar"), new HttpEntity<>(form, headers), String.class);
		assertEquals(HttpStatus.FOUND, posted.getStatusCode());
		ResponseEntity<String> list = client.getForEntity(url("/admin/mascotas"), String.class);
		assertTrue(list.getBody().contains("Mascota actualizada"));
	}

	@Test
	@Order(50)
	void eliminarMascotaRedirigeConFlashEliminada() {
		TestRestTemplate client = sessionClient(false);
		login(client, "admin", "admin123");

		ResponseEntity<String> page = client.getForEntity(url("/admin/mascotas"), String.class);
		String html = page.getBody();
		String csrf = extractCsrf(html);

		long maxId = -1;
		Matcher em = ELIMINAR_ACTION.matcher(html);
		while (em.find()) {
			maxId = Math.max(maxId, Long.parseLong(em.group(1)));
		}
		assertTrue(maxId > 0, "Debe existir al menos un formulario de eliminar");

		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
		MultiValueMap<String, String> form = new LinkedMultiValueMap<>();
		form.add("_csrf", csrf);
		ResponseEntity<String> posted = client.postForEntity(
				url("/admin/mascotas/" + maxId + "/eliminar"),
				new HttpEntity<>(form, headers),
				String.class);
		assertEquals(HttpStatus.FOUND, posted.getStatusCode());

		ResponseEntity<String> after = client.getForEntity(url("/admin/mascotas"), String.class);
		assertTrue(after.getBody().contains("Mascota eliminada"));
	}

	@Test
	@Order(60)
	void postAdminSinCsrfRetorna403() {
		TestRestTemplate client = sessionClient(true);
		login(client, "admin", "admin123");

		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
		MultiValueMap<String, String> form = new LinkedMultiValueMap<>();
		form.add("nombre", "X");
		form.add("especie", "Y");
		form.add("edad", "1");
		form.add("genero", "MACHO");
		form.add("ubicacion", "Z");
		form.add("estadoAdopcion", "DISPONIBLE");
		ResponseEntity<String> r = client.postForEntity(url("/admin/mascotas/guardar"), new HttpEntity<>(form, headers), String.class);
		assertEquals(HttpStatus.FORBIDDEN, r.getStatusCode());
	}
}
