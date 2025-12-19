package com.petuber.backend.driver;

import com.petuber.backend.user.User;
import com.petuber.backend.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/drivers")
@CrossOrigin(origins = {"http://localhost:3000", "http://localhost:3001"})
@RequiredArgsConstructor
public class DriverController {

    private final DriverRepository driverRepository;
    private final UserRepository userRepository;

    @GetMapping("/test")
    public String test() {
        System.out.println("✅ DriverController is loaded and working!");
        return "DriverController is working!";
    }

    @GetMapping("/user/{userId}")
    public Driver getDriverByUserId(@PathVariable Long userId) {
        return driverRepository.findByUser_Id(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Driver not found for user ID: " + userId));
    }

    @GetMapping("/{id}")
    public Driver getDriverById(@PathVariable Long id) {
        return driverRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Driver not found with ID: " + id));
    }

    @PostMapping
    public Driver createDriver(@RequestBody DriverRequest request) {
        // Check if driver already exists for this user
        User user = userRepository.findById(request.getUserId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found with ID: " + request.getUserId()));

        if (driverRepository.findByUser(user).isPresent()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Driver already exists for this user");
        }

        Driver driver = new Driver();
        driver.setUser(user);
        driver.setAvailable(request.isAvailable());
        return driverRepository.save(driver);
    }

    @PatchMapping("/{id}/availability")
    public Driver updateAvailability(@PathVariable Long id, @RequestBody AvailabilityRequest request) {
        Driver driver = driverRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Driver not found with ID: " + id));
        
        boolean oldAvailability = driver.isAvailable();
        driver.setAvailable(request.isAvailable());
        Driver updated = driverRepository.save(driver);
        
        System.out.println("🔄 Driver " + id + " availability updated: " + oldAvailability + " -> " + request.isAvailable());
        
        return updated;
    }

    // DTOs for request bodies
    @lombok.Data
    static class DriverRequest {
        private Long userId;
        private boolean available;
    }

    @lombok.Data
    static class AvailabilityRequest {
        private boolean available;
    }
}

