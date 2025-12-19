package com.petuber.backend.ride;

import com.petuber.backend.user.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface RideRepository extends JpaRepository<Ride, Long> {
    Optional<Ride> findByDriverAndStatusIn(User driver, List<RideStatus> statuses);
}
