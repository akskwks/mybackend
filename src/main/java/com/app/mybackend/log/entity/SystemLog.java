package com.app.mybackend.log.entity;

import com.app.mybackend.user.entity.User;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@Table(name = "app_log", indexes = {
        @Index(name = "idx_app_log_request_at", columnList = "request_at"),
        @Index(name = "idx_app_log_user_id", columnList = "user_id")
})
public class SystemLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "log_id")
    private Long logId;

    @Column(name = "user_id", length = 100, nullable = false)
    private String userId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", referencedColumnName = "user_id", insertable = false, updatable = false)
    private User user;

    @Column(name = "method_type", length = 10, nullable = false)
    private String methodType;

    @Column(name = "api_url", length = 500, nullable = false)
    private String apiUrl;

    @Column(name = "success_yn", length = 1, nullable = false)
    private String successYn;

    @Column(name = "request_json", columnDefinition = "LONGTEXT")
    private String requestJson;

    @Column(name = "request_at", nullable = false)
    private LocalDateTime requestAt;

    @Column(name = "response_json", columnDefinition = "LONGTEXT")
    private String responseJson;

    @Column(name = "response_at", nullable = false)
    private LocalDateTime responseAt;
}
