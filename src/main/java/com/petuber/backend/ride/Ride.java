package com.petuber.backend.ride;

import com.petuber.backend.user.User;
import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "rides")
@Data
public class Ride {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "client_id")
    private User client;

    @ManyToOne
    @JoinColumn(name = "driver_id")
    private User driver;

    private String pickupLocation;
    private String dropoffLocation;
    
    private Double pickupLat;
    private Double pickupLng;
    private Double dropoffLat;
    private Double dropoffLng;

    @Enumerated(EnumType.STRING)
    private RideStatus status;
}

