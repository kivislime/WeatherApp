package org.kivislime.weather.repository;

import org.kivislime.weather.entity.Location;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface LocationRepository extends JpaRepository<Location, Long>{
    List<Location> findByUserId(Long userId);
    long deleteByUserIdAndLatitudeAndLongitude(Long user_id, Double latitude, Double longitude);
}
