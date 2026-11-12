package com.gateway.payment.rules.moteur;

import com.gateway.payment.rules.domain.LimitPeriod;

import java.time.Clock;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;
import java.time.temporal.WeekFields;
import java.util.Locale;

public final class PeriodKeys {
    private PeriodKeys() {}

    public static String key(LimitPeriod period, Clock clock) {
        LocalDate day = LocalDate.now(clock);
        return switch (period == null ? LimitPeriod.DAY : period) {
            case DAY -> "D:" + day;
            case WEEK -> {
                WeekFields wf = WeekFields.of(Locale.FRANCE);
                yield "W:" + day.get(wf.weekBasedYear()) + "-W" + day.get(wf.weekOfWeekBasedYear());
            }
            case MONTH -> "M:" + day.getYear() + "-" + String.format("%02d", day.getMonthValue());
            case YEAR -> "Y:" + day.getYear();
            case ROLLING_24H -> "R24:" + day + "T" + java.time.LocalTime.now(clock).getHour();
            case ROLLING_30D -> "R30:" + day;
            case CUSTOM -> "C:" + day;
        };
    }

    static LocalDate weekStart(LocalDate day) {
        return day.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
    }
}
