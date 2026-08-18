package com.menuonline.payloads;

import java.math.BigDecimal;
import java.util.List;

import com.menuonline.exceptions.HttpServiceException;

public record AvailablePlansResponse(String productId, String name, List<String> description, List<AvailablePriceResponse> priceOptions) {

    public static record AvailablePriceResponse(String priceId, BigDecimal value, PlanRecurringInterval recurring, BigDecimal savingValue) {
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
