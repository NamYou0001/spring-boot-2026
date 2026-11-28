package com.you.springsecurity.security;

import com.you.springsecurity.user.model.Users;
import com.you.springsecurity.user.repository.UserRepository;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    public CustomUserDetailsService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String username) {

        Users user = (Users) userRepository.findByUsernameWithRoles(username).orElseThrow(() -> new UsernameNotFoundException("User not found: " + username));

        return new CustomUserDetail(user);
    }
}