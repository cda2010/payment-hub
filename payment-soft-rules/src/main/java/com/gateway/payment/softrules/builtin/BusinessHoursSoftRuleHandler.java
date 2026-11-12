package com.gateway.payment.softrules.builtin;

import com.gateway.payment.common.exception.BusinessException;
import com.gateway.payment.softrules.SoftRuleSupport;
import com.gateway.payment.softrules.spi.SoftRuleApplyContext;
import com.gateway.payment.softrules.spi.SoftRuleApplyResult;
import com.gateway.payment.softrules.spi.SoftRuleHandler;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

@Component
public class BusinessHoursSoftRuleHandler implements SoftRuleHandler {

    @Override
    public Set<String> types() {
        return Set.of("BUSINESS_HOURS");
    }

    @Override
    public SoftRuleApplyResult apply(SoftRuleApplyContext ctx) {
        Map<String, Object> params = ctx.params();
        Map<String, Object> context = ctx.context();
        Map<String, Object> vars = new LinkedHashMap<>();

        String skipFlag = SoftRuleSupport.str(params.get("skipFlag"));
        if (skipFlag == null) {
            skipFlag = "skipCutoffCheck";
        }
        if (SoftRuleSupport.isTruthy(context.get(skipFlag))) {
            vars.put("cutoffOk", true);
            return SoftRuleApplyResult.of("BUSINESS_HOURS", "Contournement " + skipFlag, vars);
        }

        String zoneId = SoftRuleSupport.str(params.get("zone"));
        if (zoneId == null) {
            zoneId = "Africa/Ouagadougou";
        }
        LocalTime from = parseTime(SoftRuleSupport.str(params.get("from")), LocalTime.of(8, 0));
        LocalTime to = parseTime(SoftRuleSupport.str(params.get("to")), LocalTime.of(16, 0));
        boolean weekdaysOnly = params.get("weekdaysOnly") == null
                || Boolean.parseBoolean(String.valueOf(params.get("weekdaysOnly")));

        ZonedDateTime now = ZonedDateTime.now(ZoneId.of(zoneId));
        DayOfWeek day = now.getDayOfWeek();
        LocalTime time = now.toLocalTime();
        boolean weekday = day != DayOfWeek.SATURDAY && day != DayOfWeek.SUNDAY;
        boolean inWindow = !time.isBefore(from) && time.isBefore(to);
        if ((weekdaysOnly && !weekday) || !inWindow) {
            String code = SoftRuleSupport.str(params.get("errorCode"));
            if (code == null) {
                code = "BUSINESS_HOURS";
            }
            throw new BusinessException(code,
                    "Hors fenêtre (" + (weekdaysOnly ? "lun–ven " : "") + from + "–" + to + ", " + zoneId + ")");
        }
        vars.put("cutoffOk", true);
        return SoftRuleApplyResult.of("BUSINESS_HOURS", "Fenêtre OK", vars);
    }

    private static LocalTime parseTime(String raw, LocalTime fallback) {
        if (!StringUtils.hasText(raw)) {
            return fallback;
        }
        return LocalTime.parse(raw.trim());
    }
}
