package com.example.projet-app.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.*;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class command {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(nullable = false)
    private Integer command-id;

}
