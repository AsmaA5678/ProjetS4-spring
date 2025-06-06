package com.example.projet-app.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.*;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class user {

    @Column(nullable = false)
    private String name;

}
