package com.menuonline.payloads.stripe;

import java.time.LocalDateTime;
import java.util.List;

import com.menuonline.utils.DateUtils;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public record StripeWebhookInvoice(String id,
        String customerEmail,
        String customer,
        String invoicePdf,
        Lines lines) {

    public static record Lines(List<LineItem> data) {
    }

    public static record LineItem(String object, LineItemParent parent, LineItemPeriod period) {
    }

    public static record LineItemParent(SubscriptionItemDetail subscriptionItemDetails) {
    }

    public static record LineItemPeriod(LocalDateTime start, LocalDateTime end) {
        public LineItemPeriod(Long start, Long end) {
            this(DateUtils.secondsToObject(start), DateUtils.secondsToObject(end));
        }
    }

    public static record SubscriptionItemDetail(String subscription) {
    }

    public String subscriptionId() {
        if (lines() != null && lines.data() != null && !lines().data().isEmpty()) {
            LineItem lineItem = lines().data().get(0);
            if (lineItem != null
                    && lineItem.parent() != null
                    && lineItem.parent().subscriptionItemDetails() != null) {
                return lineItem.parent().subscriptionItemDetails().subscription();
            }
        }
        return null;
    }

    public LocalDateTime endDate() {
        if (lines() != null && lines.data() != null && !lines().data().isEmpty()) {
            LineItem lineItem = lines.data().get(0);
            return lineItem.period().end();
        }
        return null;
    }

    public LocalDateTime startDate() {
        if (lines() != null && lines.data() != null && !lines().data().isEmpty()) {
            LineItem lineItem = lines.data().get(0);
            return lineItem.period().start();
        }
        return null;
    }
}
