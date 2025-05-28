package org.kivislime.weather.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "users")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    //TODO: размер в конфиг
    @Column(name = "login", unique = true, nullable = false, length = 100)
    private String login;
    //TODO: шифрование BCrypt
    @Column(name = "password", unique = true, nullable = false, length = 100)
    private String password;
    //TODO: cascade = CascadeType.ALL, orphanRemoval = true?
    @OneToMany(mappedBy = "user")
    List<Location> users;
}
