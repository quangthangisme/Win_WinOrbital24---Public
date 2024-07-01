package com.winwin.orbital.user;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import com.winwin.orbital.manager.Manager;
import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

@ToString
@Getter
@Setter
@EqualsAndHashCode
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "\"user\"")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    private long id;

    @NotBlank
    @Column(nullable = false, unique = true)
    private String username;

    @Email
    @Column(nullable = false, unique = true)
    @EqualsAndHashCode.Exclude
    private String email;

    @NotBlank
    @Column(nullable = false)
    @EqualsAndHashCode.Exclude
    private String password;

    @NotBlank
    @Column(nullable = false)
    @EqualsAndHashCode.Exclude
    private String role;

    @OneToOne(mappedBy = "user", cascade = CascadeType.ALL)
    @JoinColumn(name = "manager_id")
    @EqualsAndHashCode.Exclude
    @JsonManagedReference
    private Manager manager;
}
