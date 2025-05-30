package org.kivislime.weather.repository;

import org.kivislime.weather.entity.Location;
import org.kivislime.weather.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface LocationRepository extends JpaRepository<Location, Long> {
    List<Location> findByUserId(Long userId);

    Optional<Location> findByUserIdAndLatitudeAndLongitude(Long userId, Double latitude, Double longitude);

    long countByUser(User user);

    long deleteByUserIdAndLatitudeAndLongitude(Long user_id, Double latitude, Double longitude);
}
