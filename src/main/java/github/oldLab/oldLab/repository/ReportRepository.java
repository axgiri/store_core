package github.oldLab.oldLab.repository;

import github.oldLab.oldLab.Enum.ReportStatusEnum;
import github.oldLab.oldLab.entity.Report;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ReportRepository extends JpaRepository<Report, Long> {
    List<Report> findAllByStatus(ReportStatusEnum status, PageRequest pageRequest);
}
