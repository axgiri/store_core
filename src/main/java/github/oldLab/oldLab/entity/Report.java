package github.oldLab.oldLab.entity;

import github.oldLab.oldLab.Enum.ReportReasonEnum;
import github.oldLab.oldLab.Enum.ReportStatusEnum;
import github.oldLab.oldLab.Enum.ReportTypeEnum;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Column;
import jakarta.persistence.EnumType;
import jakarta.persistence.FetchType;
import  jakarta.persistence.GenerationType;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.time.Instant;

@Entity
@Table(name = "reports")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
public class Report {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Version
    private Long version;

    @NotNull(message = "Reporter id cannot be null")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reporter_id", nullable = false)
    private Person reporterId; // Person, who throw report

    @NotNull(message = "Report reason cannot be null")
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ReportReasonEnum reason;

    @Column(columnDefinition = "TEXT")
    private String details; // For other reason

    @NotNull(message = "Report status cannot be null")
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ReportStatusEnum status = ReportStatusEnum.PENDING; // PENDING, REVIEWED, REJECTED, RESOLVED

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "moderator_id")
    private Person moderator;

    @NotNull(message = "Report time of create cannot be null")
    @Column(name = "created_at")
    private Instant createdAt = Instant.now();

    @Column(name = "resolved_at")
    private Instant updatedAt;

    @NotNull(message = "Report type cannot be null")
    @Enumerated(EnumType.STRING)
    @Column(name = "report_type", nullable = false)
    private ReportTypeEnum type; // USER, SHOP, REVIEW

    @NotNull(message = "Report target id cannot be null")
    @Column(name = "target_id", nullable = false)
    private Long targetId;
}
