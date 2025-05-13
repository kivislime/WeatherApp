package org.kivislime.weather;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Date;
import java.util.UUID;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "Sessions")
public class Sessions {
    @Id
    private UUID id;

    @JoinColumn(name = "UserId")
    private Long userId;

    @JoinColumn(name = "ExpiresAt")
    private Date expiresAt;
}
