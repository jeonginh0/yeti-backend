package itey.backend.domain.admin.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class DashboardResponse {
    private long mau;
    private long dailyNewUsers;
    private long todayScheduleCount;
    private BigDecimal monthlyAiCostUsd;
}
