package com.menuonline.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.stripe.StripeClient;
import com.stripe.exception.StripeException;
import com.stripe.model.Account;
import com.stripe.model.Customer;
import com.stripe.model.StripeCollection;
import com.stripe.net.RequestOptions;
import com.stripe.param.CustomerListParams;
import com.stripe.param.InvoicePaymentListParams;
import com.stripe.param.SubscriptionListParams;
import com.stripe.param.SubscriptionSearchParams;
import com.stripe.param.SubscriptionUpdateParams;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class StripeService {

    // @Value("${stripe.secretKey}")
    // private String secretKey;

    private StripeClient client;

    public StripeService(@Value("${stripe.secretKey}") String secretKey) {
        client = new StripeClient(secretKey);
    }

    public void test() {
        try {
            client.v1().customers().list(CustomerListParams.builder().setEmail("test@mail.com").build())
                .getData().forEach(customer -> {
                    System.out.println("###### --> Customer: " + customer);
                    // customer.getSubscriptions().getData().forEach(subs -> {
                    //     System.out.println("##### --> subscription: " + subs);
                    // });
                });

            client.v1().subscriptions().list(SubscriptionListParams.builder().setCustomer("cus_U2UvMjXAUv4PKj").build())
                .getData().forEach(subs -> {
                    subs.getItems().getData().forEach(items -> {
                        items.getPlan().getActive();
                    });
                    System.out.println(subs);
                });

            client.v1().invoicePayments().list(InvoicePaymentListParams.builder().setInvoice("in_1T4QvKRsjQxNujCXwxUDZtVK").build())
                .getData().forEach(inv -> System.out.println(inv));
        } catch (StripeException e) {
            e.printStackTrace();
        }
    }

    public void cancelSubscription() {
        SubscriptionUpdateParams subscriptionUpdateParams = SubscriptionUpdateParams.builder()
                .setCancelAtPeriodEnd(true).build();

        try {
            client.v1().subscriptions().update("<SUBSCRIPTION_ID>?", subscriptionUpdateParams);
        } catch (StripeException e) {
            e.printStackTrace();
        }
    }
}
