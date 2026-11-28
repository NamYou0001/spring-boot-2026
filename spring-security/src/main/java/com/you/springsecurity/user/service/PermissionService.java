package com.you.springsecurity.user.service;

import com.you.springsecurity.common.exception.NotFoundException;
import com.you.springsecurity.user.dto.request.SavePermissionRequest;
import com.you.springsecurity.user.dto.response.PermissionResponse;
import com.you.springsecurity.user.model.Permission;
import com.you.springsecurity.user.repository.PermissionRepository;
import com.you.springsecurity.user.repository.RoleRepository;

import jakarta.validation.Valid;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class PermissionService {

    private final PermissionRepository permissionRepository;

    private final RoleRepository roleRepository;

    public PermissionService(PermissionRepository permissionRepository, RoleRepository roleRepository) {
        this.permissionRepository = permissionRepository;
        this.roleRepository = roleRepository;
    }

    @Transactional(readOnly = true)
    @Cacheable(value = "permissions", key = "'all'")
    public  List<PermissionResponse> findAll(){
        return  permissionRepository.findAll().stream().map(this::toResponse).toList();
    }


    @Transactional(readOnly = true)
    @Cacheable(value = "permissions", key = "'all'")
    public List<PermissionResponse> getAll() {

        return permissionRepository.findAll().stream().map(this::toResponse).toList();
    }

    @Transactional(readOnly = true)
    @Cacheable(value = "permissions", key = "#id")
    public PermissionResponse getById(Long id) {

        Permission permission = findPermission(id);

        return toResponse(permission);
    }

    @CacheEvict(value = "permissions", key = "'all'")
    public  PermissionResponse createPermission(SavePermissionRequest req){
        Permission permission  = toEntity(req);
        return  toResponse(permissionRepository.save(permission));
    }


    public PermissionResponse updatePermission (Long id , SavePermissionRequest req ){
        Permission permission =findPermission(id);
        permission.setCode(req.code());
        permission.setName(req.name());
        permission.setDescription(req.description());

        return toResponse(permissionRepository.save(permission));
    }


    @Caching(evict = {
            @CacheEvict(value = "permissions", key = "#id"),
            @CacheEvict(value = "permissions", key = "'all'"),
            @CacheEvict(value = "roles", allEntries = true)
    })
    public  Permission toEntity (@Valid  SavePermissionRequest req){
        Permission permission  = new Permission();
        permission.setCode(req.code());
        permission.setName(req.name());
        permission.setDescription(req.description());
        return  permission ;

    }


    @Transactional
    @Caching(evict = {
            @CacheEvict(value = "permissions", key = "#id"),
            @CacheEvict(value = "permissions", key = "'all'"),
            @CacheEvict(value = "roles", allEntries = true)
    })
    public void delete(Long id) {
        Permission permission = findPermission(id);
        permissionRepository.delete(permission);
    }

    private Permission findPermission(Long id) {

        return permissionRepository.findById(id).orElseThrow(() -> new NotFoundException("Permission not found: " + id, id));
    }


    private PermissionResponse toResponse(Permission permission) {

        return new PermissionResponse(permission.getId(),permission.getName() ,  permission.getCode(), permission.getDescription());
    }
}