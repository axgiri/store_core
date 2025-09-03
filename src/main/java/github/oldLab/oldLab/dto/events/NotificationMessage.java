package github.oldLab.oldLab.dto.events;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class NotificationMessage {
    @NotNull(message = "recipient cannot be null")
    private String recipient;

    @NotNull(message = "text cannot be null")
    private String text;

    @JsonProperty("is_html")
    private boolean isHtml;

    private String subject;
}
