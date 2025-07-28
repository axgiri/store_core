package github.oldLab.oldLab.entity;

import github.oldLab.oldLab.Enum.ReportReasonEnum;
import github.oldLab.oldLab.Enum.ReportStatusEnum;
import github.oldLab.oldLab.Enum.ReportTypeEnum;
import jakarta.persistence.*;
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

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reporter_id", nullable = false)
    private Person reporter; // Кто отправил жалобу

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ReportReasonEnum reason;

    @Column(columnDefinition = "TEXT")
    private String details; // Доп. пояснение (обязательно, если reason == OTHER)

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ReportStatusEnum status = ReportStatusEnum.PENDING; // PENDING, REVIEWED, REJECTED, RESOLVED

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "moderator_id")
    private Person moderator;

    @Column(name = "created_at")
    private Instant createdAt = Instant.now();

    @Column(name = "resolved_at")
    private Instant resolvedAt;

    @Enumerated(EnumType.STRING)
    @Column(name = "report_type", nullable = false)
    private ReportTypeEnum type; // USER, SHOP, REVIEW

    @Column(name = "target_id", nullable = false)
    private Long targetId;
}
