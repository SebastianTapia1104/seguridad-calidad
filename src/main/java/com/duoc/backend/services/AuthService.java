package com.duoc.backend.services;

import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.duoc.backend.dto.LoginRequest;
import com.duoc.backend.dto.LoginResponse;
import com.duoc.backend.entities.Usuario;
import com.duoc.backend.security.JwtTokenProvider;

@Service
public class AuthService {

	private final UserDetailsService userDetailsService;
	private final PasswordEncoder passwordEncoder;
	private final JwtTokenProvider jwtTokenProvider;

	public AuthService(UserDetailsService userDetailsService, PasswordEncoder passwordEncoder,
			JwtTokenProvider jwtTokenProvider) {
		this.userDetailsService = userDetailsService;
		this.passwordEncoder = passwordEncoder;
		this.jwtTokenProvider = jwtTokenProvider;
	}

	public LoginResponse login(LoginRequest request) {
		UserDetails userDetails = userDetailsService.loadUserByUsername(request.username());
		if (!passwordEncoder.matches(request.password(), userDetails.getPassword())) {
			throw new BadCredentialsException("Usuario o contraseña inválidos");
		}
		Usuario usuario = (Usuario) userDetails;
		String token = jwtTokenProvider.createToken(usuario);
		return new LoginResponse(token, "Bearer", usuario.getUsername(), usuario.getRol().name());
	}
}
