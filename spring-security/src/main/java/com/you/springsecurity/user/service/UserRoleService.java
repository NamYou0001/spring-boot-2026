package com.you.springsecurity.user.service;


import com.you.springsecurity.user.dto.request.UserReq;
import com.you.springsecurity.user.dto.response.UserResponse;
import com.you.springsecurity.common.exception.NotFoundException;
import com.you.springsecurity.user.model.Role;
import com.you.springsecurity.user.model.Users;

import com.you.springsecurity.user.repository.RoleRepository;
import com.you.springsecurity.user.repository.UserRepository;

import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class UserRoleService {

    private final UserRepository userRepository;

    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    public UserRoleService(UserRepository userRepository, RoleRepository roleRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional(readOnly = true)
    @Cacheable(value = "users", key = "'all'")
    public  List<UserResponse> findAll (){
        return  userRepository.findAll().stream().map(this::Response).toList();
    }


    @Transactional
    @CacheEvict(value = "users", key = "'all'")
    public   UserResponse saveUser (UserReq userReq){
        Users  users = toEntity(userReq);
        Users saveUsers = userRepository.save(users);
        return Response(saveUsers);
    }


    public UserResponse Response (Users users ){
        return  UserResponse.builder()
                .userId(users.getId())
                .username(users.getUsername())
                .email(users.getEmail())
                .roleId(users.getRole().getId())
                .roleName(users.getRole().getName())
                .build();
    }


    public  Users toEntity (UserReq userReq){
        Users users = new Users();
        users.setUsername(userReq.username());
        users.setEmail(userReq.email());
        users.setPassword(passwordEncoder.encode(userReq.password()));
        Role role = roleRepository.findById(userReq.roleId())
                .orElseThrow(()->new NotFoundException("Role Not Found", userReq.roleId()));
        users.setRole(role);
        return  users;
    }
//
//    public  Role getRoleById(Long id ){
//        return  roleRepository.findById(id).orElseThrow(new NotFoundException("Role Not Found "));
//    }

//    @Transactional(readOnly = true)
//    public List<String> getUserRoles(Long userId) {
//
//        Users user = findUser(userId);
//
//        return user.getRole().stream().map(Role::getName).sorted().toList();
//    }

//    @Transactional
//    public List<String> assignRoles(Long userId, Set<Long> roleIds) {
//
//        Users user = findUser(userId);
//
//        List<Role> roles = roleRepository.findAllById(roleIds);
//
//        if (roles.size() != roleIds.size()) {
//            throw new IllegalArgumentException("One or more role IDs do not exist");
//        }
//
//        user.(new HashSet<>(roles));
//
//        Users savedUser = userRepository.save(user);
//
//        return savedUser.getRoles().stream().map(Role::getName).sorted().toList();
//    }
//
//    @Transactional
//    public void removeRole(Long userId, Long roleId) {
//
//        Users user = findUser(userId);
//
//        Role role = roleRepository.findById(roleId).orElseThrow(() -> new IllegalArgumentException("Role not found: " + roleId));
//
//        if (!user.getRoles().remove(role)) {
//            throw new IllegalArgumentException("User does not have this role");
//        }
//
//        userRepository.save(user);
//    }
//
//    private Users findUser(Long userId) {
//
//        return userRepository.findWithRolesById(userId).orElseThrow(() -> new IllegalArgumentException("User not found: " + userId));
//    }
}