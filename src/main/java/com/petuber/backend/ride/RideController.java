package com.petuber.backend.ride;

import com.petuber.backend.user.User;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/rides")
@CrossOrigin(origins = {"http://localhost:3000", "http://localhost:3001"})
@RequiredArgsConstructor
public class RideController {

    private final RideRepository rideRepository;
    private final RideService rideService;

    @GetMapping("/{id}")
    public Ride getRide(@PathVariable Long id) {
        return rideRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Ride not found with ID: " + id));
    }

    @GetMapping("/driver/active")
    public Ride getActiveRide() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof User) {
            User driver = (User) authentication.getPrincipal();
            return rideRepository.findByDriverAndStatusIn(
                driver,
                List.of(RideStatus.ACCEPTED, RideStatus.IN_PROGRESS)
            ).orElseThrow(() -> new RuntimeException("No active ride found"));
        }
        throw new RuntimeException("Driver not authenticated");
    }

    @PostMapping("/request")
    public Ride requestRide(@RequestBody Ride ride) {
        // Get current authenticated user (client)
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof User) {
            User client = (User) authentication.getPrincipal();
            ride.setClient(client);
        }
        
        // Use RideService to create ride and notify drivers via WebSocket
        return rideService.createRide(ride);
    }

    @PostMapping("/{id}/accept")
    public Ride acceptRide(@PathVariable Long id) {
        // Get current authenticated user (driver)
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof User) {
            User driver = (User) authentication.getPrincipal();
            return rideService.acceptRide(id, driver);
        }
        throw new RuntimeException("Driver not authenticated");
    }

    @PatchMapping("/{id}/start")
    public Ride startRide(@PathVariable Long id) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof User) {
            return rideService.startRide(id);
        }
        throw new RuntimeException("Driver not authenticated");
    }

    @PatchMapping("/{id}/complete")
    public Ride completeRide(@PathVariable Long id) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof User) {
            return rideService.completeRide(id);
        }
        throw new RuntimeException("Driver not authenticated");
    }
}

