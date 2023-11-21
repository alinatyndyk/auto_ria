package com.example.auto_ria.models.responses.statistics;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StatisticsResponse {
    private long viewsDay;
    private long viewsWeek;
    private long viewsMonth;
    private long viewsAll;
}
