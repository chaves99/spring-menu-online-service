package com.menuonline.payloads;

import java.math.BigDecimal;

import com.menuonline.exceptions.HttpServiceException;

public record AvailablePlansResponse(String priceId, BigDecimal value, String name, String description,
        PlanRecurringInterval recurringInterval) {

    public AvailablePlansResponse(String priceId, BigDecimal value, String name,  String description, String recurring) {
        this(priceId, value, name, description, PlanRecurringInterval.get(recurring));
    }

    public enum PlanRecurringInterval {
        DAY, WEEK, MONTH, YEAR;

        public static PlanRecurringInterval get(String value) {
            return switch (value) {
                case "day" -> DAY;
                case "week" -> WEEK;
                case "month" -> MONTH;
                case "year" -> YEAR;
                default -> throw new HttpServiceException("PlanRecurringInterval not found: " + value);
            };
        }
    }
}
