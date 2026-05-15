package dev.vivim.weatherviewer.repository;

import dev.vivim.weatherviewer.model.Location;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public interface LocationRepository extends JpaRepository<Location,Integer> {
    @Query("SELECT l FROM Location l WHERE l.user.id = :user_id")
    List<Location> findUserLocations(@Param("user_id") int user_id);

    @Modifying
    @Transactional
    @Query("DELETE FROM Location l WHERE l.id = :id AND l.user.id = :user_id")
    void deleteLocation(@Param("id") int id, @Param("user_id") int user_id);
}
