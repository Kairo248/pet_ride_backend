package com.petuber.backend.message;

import com.petuber.backend.ride.Ride;
import com.petuber.backend.ride.RideRepository;
import com.petuber.backend.user.User;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;

@Controller
@RequiredArgsConstructor
public class MessageController {

    private final MessageRepository messageRepository;
    private final RideRepository rideRepository;

    @MessageMapping("/chat/{rideId}")
    @SendTo("/topic/chat/{rideId}")
    public Message sendMessage(
            @DestinationVariable Long rideId,
            Message message
    ) {
        // Get current authenticated user (sender)
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof User) {
            User sender = (User) authentication.getPrincipal();
            message.setSender(sender);
        }

        // Set the ride
        Ride ride = rideRepository.findById(rideId)
                .orElseThrow(() -> new RuntimeException("Ride not found with ID: " + rideId));
        message.setRide(ride);

        System.out.println("💬 Message received for ride " + rideId + ": " + message.getContent());
        
        Message saved = messageRepository.save(message);
        System.out.println("✅ Message saved and broadcasting to /topic/chat/" + rideId);
        
        return saved;
    }
}
