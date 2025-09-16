package github.oldLab.oldLab.seeder.factory;

import java.time.Instant;
import java.util.concurrent.ThreadLocalRandom;

import org.springframework.stereotype.Component;

import com.github.javafaker.Faker;

import github.oldLab.oldLab.Enum.ReportReasonEnum;
import github.oldLab.oldLab.Enum.ReportTypeEnum;
import github.oldLab.oldLab.entity.Person;
import github.oldLab.oldLab.entity.Report;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class ReportFactory implements DataFactory<Report> {

    private final Faker faker;

    public Report create(Person reporter, ReportTypeEnum type, Long targetId) {
        var reasons = ReportReasonEnum.values();
        return new Report()
                .setReporterId(reporter)
                .setReason(reasons[ThreadLocalRandom.current().nextInt(reasons.length)])
                .setDetails(faker.lorem().sentence())
                .setModerator(null)
                .setCreatedAt(Instant.now())
                .setUpdatedAt(null)
                .setType(type)
                .setTargetId(targetId);
    }

    @Override
    public Report create() {
        return new Report();
    }
}
