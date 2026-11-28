package com.you.springsecurity.user.model;

import jakarta.persistence.*;

@Entity
@Table(
        name = "tbl_permissions",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_permission_code",
                        columnNames = "code"
                )
        }
)
public class Permission {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 100)
    private String code;
    private  String name ;
    @Column(length = 255)
    private String description;

    public Permission() {
    }

    public Permission(String code, String name ,  String description) {
        this.code = code;
        this.name = name ;
        this.description = description;
    }

//    public Permission(String code, String name ,  String description) {
//        this.name = name ;
//        this.code = code;
//        this.description = description;
//    }

    public Long getId() {
        return id;
    }


    public String getName (){
        return  name;
    }
    public  void setName(String name){
        this.name = name ;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}