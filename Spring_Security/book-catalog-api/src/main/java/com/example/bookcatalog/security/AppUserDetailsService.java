package com.example.bookcatalog.security;

import com.example.bookcatalog.model.AppUser;
import com.example.bookcatalog.repository.AppUserRepository;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class AppUserDetailsService implements UserDetailsService {
        private final AppUserRepository users;

        public AppUserDetailsService (AppUserRepository users)
        {
            this.users = users;
        }

        @Override
        public UserDetails loadUserByUsername (String username)
        {
            AppUser user = users.findByUsername(username).orElseThrow(() -> new UsernameNotFoundException("Invalid credentials"));

            return User.withUsername(user.getUsername())
                        .password(user.getPasswordHash())
                        .roles(user.getRole())
                        .build();
        }
}
