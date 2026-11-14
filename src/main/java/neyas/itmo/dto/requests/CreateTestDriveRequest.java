package neyas.itmo.dto.requests;

import neyas.itmo.framework.NotNull;

import java.time.LocalDateTime;
import java.util.UUID;

public record CreateTestDriveRequest(
    @NotNull UUID carId,
    @NotNull LocalDateTime startsAt
){
}
