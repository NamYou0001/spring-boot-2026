package com.you.springsecurity.model;


import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Entity()
@Table(name ="tbl_users")
public class Users {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private  Long id ;
    private  String username ;
    private  String password ;
    private  String email;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private  Roles roles ;
    private  boolean enable = true;
    private  boolean accountNonlocked = true ;
    private  boolean expiresAt ;
    private Instant createAt ;

    public  Users(String username ,String email ,String password,  Roles roles){
        this.username = username ;
        this.password = password;
        this.email = email ;
        this.roles = roles;
    }

}