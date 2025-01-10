package com.auth.service;

import com.auth.exception.AccountLockedException;
import com.auth.exception.BadCredentialsException;
import com.auth.model.UserInfo;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import com.auth.repository.UserInfoRepository;

@Component
public class CustomAuthProvider implements AuthenticationProvider{

	@Autowired
	private UserInfoRepository userRepository;

	private final UserInfoService userInfoService;

    @Autowired
    public CustomAuthProvider(@Lazy UserInfoService userInfoService) {
        this.userInfoService = userInfoService;
    }

	@Autowired
	private PasswordEncoder passwordEncoder;

	@Override
	public Authentication authenticate(Authentication authentication) throws AuthenticationException {
		String username = authentication.getName();
		String password = authentication.getCredentials().toString();

		// Check the user's details and password
		Optional<UserInfo> userFound = userRepository.findByEmail(username);
		UserInfo user = userFound.get();

		if (userFound.isEmpty()) {
			throw new BadCredentialsException("Invalid username or password");
		}
		if (Boolean.TRUE.equals(user.getLocked())) {
			//throw new AccountLockedException("Account is locked. Please contact support.");
			throw  new AccountLockedException("Account is Locked");
		}
		//int failedAttempts = Optional.ofNullable(user.getFailedLoginAttempts()).orElse(0);

		int failedAttempts = (user.getFailedLoginAttempts() != null) ? user.getFailedLoginAttempts() : 0;

		if (!passwordEncoder.matches(password, user.getPassword())) {
			System.out.println(failedAttempts);
            failedAttempts++;
            user.setFailedLoginAttempts(failedAttempts);
            
            System.out.println(failedAttempts);
            if (failedAttempts >= 5) {
                user.setLocked(true); // Lock the account after 3 failed attempts
            }

            userRepository.save(user);
            throw new BadCredentialsException("Invalid username or password");
        }

        // Reset failed login attempts on successful authentication
        user.setFailedLoginAttempts(0);
        user.setLocked(false);
        userRepository.save(user);

		// Load user details and return authentication token
		UserDetails userDetails = userInfoService.loadUserByUsername(username);
		return new UsernamePasswordAuthenticationToken(userDetails, password, userDetails.getAuthorities());
	}

	@Override
	public boolean supports(Class<?> authentication) {
		return UsernamePasswordAuthenticationToken.class.isAssignableFrom(authentication);
	}

}
