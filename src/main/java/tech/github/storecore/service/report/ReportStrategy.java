package tech.github.storecore.service.report;

import java.util.UUID;

import tech.github.storecore.dto.request.ReportRequest;
import tech.github.storecore.enumeration.ReportTypeEnum;

public interface ReportStrategy {

    ReportTypeEnum getType();

    void validate(UUID reporterId, ReportRequest request);
}
