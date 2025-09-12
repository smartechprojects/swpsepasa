package com.eurest.supplier.security;

import java.io.IOException;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationFailureHandler;
import org.springframework.web.filter.OncePerRequestFilter;


public class CaptchaCaptureFilter extends OncePerRequestFilter {

	private String failureUrl = "/login?error=true";

	@Override
	public void doFilterInternal(HttpServletRequest req, HttpServletResponse res, FilterChain chain)
			throws IOException, ServletException {

		if (req.getParameter("g-recaptcha-response") != null) {
			boolean verify = VerifyRecaptcha.verify(req.getParameter("g-recaptcha-response"));
			if(!verify) {
				SimpleUrlAuthenticationFailureHandler failureHandler = new SimpleUrlAuthenticationFailureHandler();
				failureHandler.setDefaultFailureUrl(failureUrl);
			    failureHandler.onAuthenticationFailure(req, res, new BadCredentialsException("La selección del Captcha es incorrecto!"));
			}else {
				chain.doFilter(req, res);
			}
		}else {
			chain.doFilter(req, res);
		}
		
	}


}
