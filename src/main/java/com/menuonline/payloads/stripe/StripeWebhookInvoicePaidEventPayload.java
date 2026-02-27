package com.menuonline.payloads.stripe;

public record StripeWebhookInvoicePaidEventPayload(String customerEmail,
        String customer, // customer key(stripe id)
        String name, //
        String invoice, // invoice key
        String invoicePdf, // url to download the invoice pdf
        String status, // paid
        String periodEnd,
        String periodStart
        ) {


}
