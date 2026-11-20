package com.you.springsecurity.config;


import com.you.springsecurity.model.Roles;
import com.you.springsecurity.model.Users;
import com.you.springsecurity.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Component
@Profile("dev")
public class DevelopmentUserSeeder implements CommandLineRunner {

    private final UserRepository userRepository ;
    private final PasswordEncoder passwordEncoder ;

    @Override
    public void run(String... args) throws Exception {
        createUserIfNotExists("Admin"  , "Admin@gmail.com" , "admin@123" , Roles.ADMIN);
        createUserIfNotExists("user" , "user@gmail.com" ,"user@123", Roles.USERS);

    }

    private  void createUserIfNotExists(
            String username  , String email , String passwrod, Roles roles
    ){
        if (userRepository.existsByUsername(username)){
            return;
        }
        String hashPassword = passwordEncoder.encode(passwrod);
        Users users = new Users(username, email, hashPassword, roles);
        userRepository.save(users);
    }
}
