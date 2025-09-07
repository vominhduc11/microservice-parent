package com.devwonder.reportservice.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "reports")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Report {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "report_name", nullable = false)
    private String reportName;
    
    @Enumerated(EnumType.STRING)
    private ReportType type;
    
    @Column(name = "report_data", columnDefinition = "TEXT")
    private String reportData;
    
    @Column(name = "generated_by", nullable = false)
    private Long generatedBy;
    
    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
    
    @Column(name = "report_period_start")
    private LocalDateTime reportPeriodStart;
    
    @Column(name = "report_period_end")
    private LocalDateTime reportPeriodEnd;
    
    public enum ReportType {
        SALES, INVENTORY, USER_ACTIVITY, FINANCIAL, CUSTOM
    }
}