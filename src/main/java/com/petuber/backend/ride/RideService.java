package com.petuber.backend.ride;

import com.petuber.backend.driver.Driver;
import com.petuber.backend.driver.DriverRepository;
import com.petuber.backend.user.User;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class RideService {

    private final RideRepository rideRepository;
    private final DriverRepository driverRepository;
    private final SimpMessagingTemplate messagingTemplate;

    public Ride createRide(Ride ride) {
        ride.setStatus(RideStatus.REQUESTED);
        Ride saved = rideRepository.save(ride);

        System.out.println("🚗 New ride requested: ID=" + saved.getId() + 
                          ", Pickup=" + saved.getPickupLocation() + 
                          ", Dropoff=" + saved.getDropoffLocation());

        // Notify drivers
        List<Driver> drivers = driverRepository.findByAvailableTrue();
        System.out.println("📡 Found " + drivers.size() + " available driver(s)");
        
        if (drivers.isEmpty()) {
            System.out.println("⚠️ No available drivers found! Make sure at least one driver is online.");
        }
        
        drivers.forEach(driver -> {
            String topic = "/topic/driver/" + driver.getId();
            System.out.println("📤 Sending ride request to driver ID=" + driver.getId() + " via topic: " + topic);
            try {
                messagingTemplate.convertAndSend(topic, saved);
                System.out.println("✅ Successfully sent ride request to driver " + driver.getId());
            } catch (Exception e) {
                System.err.println("❌ Error sending ride request to driver " + driver.getId() + ": " + e.getMessage());
                e.printStackTrace();
            }
        });

        return saved;
    }

    public Ride acceptRide(Long rideId, User driver) {
        Ride ride = rideRepository.findById(rideId).orElseThrow();
        ride.setDriver(driver);
        ride.setStatus(RideStatus.ACCEPTED);
        Ride saved = rideRepository.save(ride);
        
        System.out.println("✅ Ride " + rideId + " accepted by driver: " + driver.getName());
        
        // Notify client via WebSocket that ride was accepted
        String clientTopic = "/topic/ride/" + rideId;
        System.out.println("📤 Notifying client via topic: " + clientTopic);
        messagingTemplate.convertAndSend(clientTopic, saved);
        
        return saved;
    }

    public Ride startRide(Long rideId) {
        Ride ride = rideRepository.findById(rideId).orElseThrow();
        ride.setStatus(RideStatus.IN_PROGRESS);
        Ride saved = rideRepository.save(ride);
        
        System.out.println("🚗 Ride " + rideId + " started");
        
        // Notify client via WebSocket that ride started
        String clientTopic = "/topic/ride/" + rideId;
        messagingTemplate.convertAndSend(clientTopic, saved);
        
        return saved;
    }

    public Ride completeRide(Long rideId) {
        Ride ride = rideRepository.findById(rideId).orElseThrow();
        ride.setStatus(RideStatus.COMPLETED);
        Ride saved = rideRepository.save(ride);
        
        System.out.println("✅ Ride " + rideId + " completed");
        
        // Notify client via WebSocket that ride completed
        String clientTopic = "/topic/ride/" + rideId;
        messagingTemplate.convertAndSend(clientTopic, saved);
        
        return saved;
    }
}
