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
@Table(
        name = "users", uniqueConstraints = @UniqueConstraint(
        columnNames = {"login"}
      )
)
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name = "login", unique = true, nullable = false, length = 100)
    private String login;
    @Column(name = "password", unique = true, nullable = false, length = 100)
    private String password;
    @OneToMany(mappedBy = "user")
    List<Location> users;
}
