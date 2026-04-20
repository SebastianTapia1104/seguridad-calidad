package com.duoc.backend.security;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

	public static final String HEADER_AUTHORIZATION = "Authorization";
	public static final String TOKEN_PREFIX = "Bearer ";

	private final JwtTokenProvider jwtTokenProvider;

	public JwtAuthenticationFilter(JwtTokenProvider jwtTokenProvider) {
		this.jwtTokenProvider = jwtTokenProvider;
	}

	@Override
	protected void doFilterInternal(
			@NonNull HttpServletRequest request,
			@NonNull HttpServletResponse response,
			@NonNull FilterChain filterChain) throws ServletException, IOException {
		String header = request.getHeader(HEADER_AUTHORIZATION);
		// Si no hay header, continuar sin limpiar (permitir sesiones de Thymeleaf)
		if (header == null) {
			filterChain.doFilter(request, response);
			return;
		}
		// Si hay header pero no comienza con "Bearer ", limpiar (JWT inválido)
		if (!header.startsWith(TOKEN_PREFIX)) {
			SecurityContextHolder.clearContext();
			filterChain.doFilter(request, response);
			return;
}
		String jwt = header.substring(TOKEN_PREFIX.length()).trim();
		try {
			Claims claims = jwtTokenProvider.parseClaims(jwt);
			@SuppressWarnings("unchecked")
			List<String> authorities = (List<String>) claims.get("authorities");
			if (authorities == null || authorities.isEmpty()) {
				SecurityContextHolder.clearContext();
			} else {
				var granted = authorities.stream().map(SimpleGrantedAuthority::new).collect(Collectors.toList());
				var auth = new UsernamePasswordAuthenticationToken(claims.getSubject(), null, granted);
				SecurityContextHolder.getContext().setAuthentication(auth);
			}
		} catch (JwtException e) {
			SecurityContextHolder.clearContext();
			response.setStatus(HttpServletResponse.SC_FORBIDDEN);
			response.sendError(HttpServletResponse.SC_FORBIDDEN, e.getMessage());
			return;
		}
		filterChain.doFilter(request, response);
	}

	@Override
	protected boolean shouldNotFilter(HttpServletRequest request) {
		return !request.getRequestURI().startsWith("/api/");
	}
}
