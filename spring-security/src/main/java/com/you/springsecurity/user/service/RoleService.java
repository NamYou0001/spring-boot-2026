package com.you.springsecurity.user.service;


import com.you.springsecurity.common.exception.NotFoundException;
import com.you.springsecurity.user.dto.request.SaveRoleRequest;
import com.you.springsecurity.user.dto.response.RoleResponse;
import com.you.springsecurity.user.model.Permission;
import com.you.springsecurity.user.model.Role;

import com.you.springsecurity.user.repository.PermissionRepository;
import com.you.springsecurity.user.repository.RoleRepository;
import com.you.springsecurity.user.repository.UserRepository;


import org.springframework.stereotype.Service;


import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class RoleService {

    private final RoleRepository roleRepository;

    private final PermissionRepository permissionRepository;

    private final UserRepository userRepository;

    public RoleService(RoleRepository roleRepository, PermissionRepository permissionRepository, UserRepository userRepository) {
        this.roleRepository = roleRepository;
        this.permissionRepository = permissionRepository;
        this.userRepository = userRepository;
    }



    public  List<RoleResponse> findAll(){
        return  roleRepository.findAll().stream().map(this::toResponse).toList();
    }

    public  RoleResponse createRole(SaveRoleRequest req){
        Role role  = toEntity(req);
        Role saveRole = roleRepository.save(role);
        return  toResponse(saveRole);
    }

    public  void deleteRole (Long id){
        Role findId = findById(id);
        roleRepository.delete(findId);
    }


    public   RoleResponse getById(Long id){
        return  toResponse(findById(id));
    }

    public  Role findById(Long id ){
        return  roleRepository.findById(id).orElseThrow(()-> new NotFoundException("Role" , id));
    }


    public  RoleResponse updateRole (Long id , SaveRoleRequest req)
    {
        Role findRole = findById(id);
        findRole.setName(req.name());
        findRole.setDescription(req.description());
        return  toResponse(roleRepository.save(findRole));
    }


    public Role toEntity(SaveRoleRequest req){
        Role role = new Role();
        role.setName(req.name());
        role.setDescription(req.description());
        return  role ;

    }


    public  RoleResponse toResponse (Role role)
    {
        Set<String> permission = role.getPermissions().stream().map(Permission::getName).collect(Collectors.toSet());
        return  new RoleResponse(
                role.getId(),
                role.getName(),
                role.getDescription(),
                permission
        );
    }


}