package github.oldLab.oldLab.serviceImpl;

import github.oldLab.oldLab.Enum.ReportStatusEnum;
import github.oldLab.oldLab.dto.request.ReportRequest;
import github.oldLab.oldLab.dto.response.ReportResponse;
import github.oldLab.oldLab.entity.Person;
import github.oldLab.oldLab.entity.Report;
import github.oldLab.oldLab.exception.UserNotFoundException;
import github.oldLab.oldLab.repository.PersonRepository;
import github.oldLab.oldLab.repository.ReportRepository;
import github.oldLab.oldLab.service.ReportService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.task.TaskExecutor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class ReportServiceImpl implements ReportService {

    private final ReportRepository repository;
    private final PersonRepository personRepository;

    @Qualifier("asyncExecutor")
    private final TaskExecutor taskExecutor;

    @Transactional
    @Override
    public ReportResponse createReport(ReportRequest request) {

        Person reporter = personRepository.findById(request.getReporterId())
                .orElseThrow(() -> new UserNotFoundException("Reporter not found"));
        Report report = request.toEntity(reporter);
        report = repository.save(report);
        return ReportResponse.fromEntityToDto(report);
    }
    @Override
    public void createAsync(ReportRequest request) {
        log.info("Creating report for target: {}", request.getTargetId());
        Person reporter = personRepository.findById(request.getReporterId())
                .orElseThrow(() -> new UserNotFoundException("Reporter not found"));

        taskExecutor.execute(() -> {
            Report report = request.toEntity(reporter);
            repository.save(report);
            log.info("Report created for target: {}", request.getTargetId());
        });
    }

    @Transactional(readOnly = true)
    @Override
    public List<ReportResponse> getAllReports(int page, int size) {
        return repository.findAll(PageRequest.of(page, size)).getContent().stream()
                .map(ReportResponse::fromEntityToDto)
                .toList();
    }

    @Transactional(readOnly = true)
    @Override
    public List<ReportResponse> getReportsByStatus(ReportStatusEnum status) {
        return repository.findAllByStatus(status).stream()
                .map(ReportResponse::fromEntityToDto)
                .toList();
    }

    @Transactional
    @Override
    public ReportResponse updateReportStatus(Long reportId, ReportStatusEnum status, Long moderatorId) {
        Report report = repository.findById(reportId)
                .orElseThrow(() -> new UserNotFoundException("Report not found"));

        Person moderator = personRepository.findById(moderatorId)
                .orElseThrow(() -> new UserNotFoundException("Moderator not found"));

        report.setStatus(status)
                .setModerator(moderator)
                .setUpdatedAt(Instant.now());

        report = repository.save(report);

        return ReportResponse.fromEntityToDto(report);
    }

}
