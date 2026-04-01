package dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import lombok.Builder;
import lombok.Data;
import model.Visit;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;

@Data
@Builder
public class EndpointHit {
    @NotNull
    @NotBlank
    private String app;

    @NotNull
    @NotBlank
    private String uri;

    @NotNull
    @NotBlank
    private String ip;

    @NotNull
    @PastOrPresent
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime timestamp;

    public Visit toVisit() {
        Visit visit = new Visit();
        visit.setApp(this.app);
        visit.setUri(this.uri);
        visit.setIp(this.ip);
        visit.setTimestamp(this.timestamp);
        return visit;
    }

    public static EndpointHit fromVisit(Visit visit) {
        return EndpointHit.builder()
                .app(visit.getApp())
                .uri(visit.getUri())
                .ip(visit.getIp())
                .timestamp(visit.getTimestamp())
                .build();
    }
}
