package com.example.auto_ria.models.socket;


import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.springframework.web.socket.WebSocketSession;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Session {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    private String sessionId;

    @Column(updatable = false)
    private String userId;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", timezone = "GMT")
    private LocalDateTime disconnectedAt;

    private String userType;

    private Boolean isOnline;

    @Column(updatable = false)
    @CreationTimestamp
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", timezone = "GMT")
    private LocalDateTime createdAt;

    @Builder
    public Session(String sessionId, String userId, LocalDateTime disconnectedAt, String userType, Boolean isOnline) {
        this.sessionId = sessionId;
        this.userId = userId;
        this.disconnectedAt = disconnectedAt;
        this.userType = userType;
        this.isOnline = isOnline;
    }

    public void setOnline(boolean isOnline) {
        this.isOnline = isOnline;
    }
}
