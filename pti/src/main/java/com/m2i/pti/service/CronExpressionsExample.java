package com.m2i.pti.service;

import org.quartz.CronScheduleBuilder;
import org.quartz.DateBuilder;
import org.quartz.Trigger;
import org.quartz.TriggerBuilder;

public class CronExpressionsExample {
    private static final String GROUP_NAME = "CroneExamples";

    // Fire at current time + 1 min every day
    public static Trigger fireAfterAMinEveryDayStartingAt(int startHr, int startMin, int startSec, int nowMin, int nowHr) {
        Trigger trigger = TriggerBuilder.newTrigger()
                .withIdentity("fireAfterAMinStartingAt", GROUP_NAME)
                .startAt(DateBuilder.todayAt(startHr, startMin, startSec))
                .withSchedule(CronScheduleBuilder.cronSchedule("0 0 * * * ?"))    // a remplacer avec les données de la base de données
                .build();
        return trigger;
    }
}
