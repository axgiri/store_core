package tech.github.oldlabclient.service.report;

import tech.github.oldlabclient.dto.request.ReportRequest;
import tech.github.oldlabclient.enumeration.ReportTypeEnum;

public interface ReportStrategy {

    ReportTypeEnum getType();

    void validate(ReportRequest request);
}
