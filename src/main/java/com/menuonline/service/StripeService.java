package com.menuonline.service;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import com.menuonline.exceptions.HttpServiceException;
import com.menuonline.payloads.SubscriptionDetailResponse;
import com.stripe.StripeClient;
import com.stripe.exception.StripeException;
import com.stripe.model.Customer;
import com.stripe.model.Subscription;
import com.stripe.model.checkout.Session;
import com.stripe.param.CustomerListParams;
import com.stripe.param.SubscriptionCancelParams;
import com.stripe.param.SubscriptionListParams;
import com.stripe.param.SubscriptionRetrieveParams;
import com.stripe.param.checkout.SessionCreateParams;
import com.stripe.service.V1Services;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class StripeService {

    private final StripeClient client;

    private final String planMonthly;

    public StripeService(@Value("${stripe.secretKey}") String secretKey,
            @Value("${stripe.plans.monthly-simple}") String planMonthly) {
        this.planMonthly = planMonthly;
        client = new StripeClient(secretKey);
    }

    public Optional<Customer> findCustomerByEmail(String email) {
        CustomerListParams params = CustomerListParams.builder()
                .setEmail(email)
                .build();
        V1Services v1 = client.v1();
        try {
            List<Customer> data = v1.customers().list(params).getData();
            log.info("findSubscriptionByEmail - email:{} number of customers:{}", email, data.size());
            return data.stream().max(Comparator.comparingLong(c -> c.getCreated()));
        } catch (StripeException e) {
            log.warn("findCustomerByEmail - email:{} exception:{}", email, e.getLocalizedMessage());
            return Optional.empty();
        }
    }

    public Optional<String> findEmailByCustomer(String customer) {
        try {
            Customer customerObject = client.v1().customers().retrieve(customer);
            if (customerObject == null)
                return Optional.empty();

            return Optional.of(customerObject.getEmail());
        } catch (StripeException e) {
            log.warn("findEmailByCustomer - customer:{} exception:{}", customer, e.getLocalizedMessage());
            return Optional.empty();
        }
    }

    /**
     * @param email
     * @return all subscriptions but canceled
     */
    public Optional<?> findSubscriptionByEmail(String email) {
        try {
            CustomerListParams params = CustomerListParams.builder()
                    .setEmail(email)
                    .build();
            V1Services v1 = client.v1();
            List<Customer> data = v1.customers().list(params).getData();
            log.info("findSubscriptionByEmail - email:{} number of customers:{}", email, data.size());
            Optional<Customer> customer = data.stream().max(Comparator.comparingLong(c -> c.getCreated()));
            return customer.flatMap(c -> {
                SubscriptionListParams subsParam = SubscriptionListParams.builder()
                        .setCustomer(c.getId())
                        .addExpand("data.default_payment_method")
                        .addExpand("data.latest_invoice")
                        .build();
                try {
                    List<Subscription> subscriptions = v1.subscriptions().list(subsParam).getData();
                    subscriptions.forEach(s -> s.getDaysUntilDue());
                    System.out.println("### subscription: " + subscriptions);
                    return subscriptions.stream()
                            .max(Comparator.comparingLong(s -> s.getCreated()));
                } catch (StripeException e) {
                    e.printStackTrace();
                    log.warn("findSubscriptionByEmail - exception: {}", e.getMessage());
                    return Optional.empty();
                }
            });
        } catch (StripeException e) {
            log.warn("findSubscriptionByEmail - email:{} exception: {}", email, e.getLocalizedMessage());
        }
        return Optional.empty();
    }

    public Optional<?> findSubscriptionByCustomerId(String customerId) {
        try {
            Customer customer = client.v1().customers().retrieve(customerId);

            List<Subscription> data = customer.getSubscriptions().getData();
            log.info("findSubscriptionByCustomerId - customer:{} subscription size:{}",
                    customerId, data.size());
            return data.stream()
                    .max(Comparator.comparingLong(s -> s.getCreated()));
        } catch (StripeException e) {
            log.warn("findSubscriptionByCustomerId - exception: {}", e.getMessage());
            throw new HttpServiceException(null, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    public Optional<String> generateChangePaymentMethodUrl(String customer, String subscription) {
        log.info("generateChangePaymentMethodUrl - customer:{} subscription:{}", customer, subscription);
        try {
            V1Services v1 = client.v1();

            SessionCreateParams sessionCreateParams = SessionCreateParams.builder()
                    .setCustomer(customer)
                    .addPaymentMethodType(SessionCreateParams.PaymentMethodType.CARD)
                    .setSetupIntentData(SessionCreateParams.SetupIntentData.builder()
                            .putMetadata("customer_id", customer)
                            .putMetadata("subscription_id", subscription)
                            .build())
                    .setMode(SessionCreateParams.Mode.SETUP)
                    .setSuccessUrl("https://itimenu.app")
                    .build();
            Session session;
            session = v1.checkout().sessions().create(sessionCreateParams);
            return Optional.of(session.getUrl());
        } catch (StripeException e) {
            log.warn("generateChangePaymentMethodUrl - customer:{} subscription:{} exception: {}",
                    customer, subscription, e);
            return Optional.empty();
        }
    }

    public String cancel(String subscriptionId) {
        SubscriptionCancelParams params = SubscriptionCancelParams.builder()
                .setProrate(true)
                // .setCancellationDetails(CancellationDetails)
                .build();
        try {
            Subscription cancel = client.v1().subscriptions().cancel(subscriptionId, params);
            return cancel.getId();
        } catch (StripeException e) {
            log.warn("cancel - exception: {}", e.getMessage());
            throw new HttpServiceException(null, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    public Optional<String> generateNewPlanUrl(String email) {
        log.info("generateNewPlanUrl - email:{} plan:{}", email, planMonthly);
        try {
            SessionCreateParams sessionCreateParams = SessionCreateParams.builder()
                    .setSuccessUrl("https://itimenu.app/admin/subscription")
                    .setMode(SessionCreateParams.Mode.SUBSCRIPTION)
                    .setCustomerEmail(email)
                    .setLocale(SessionCreateParams.Locale.PT_BR)
                    .setBrandingSettings(SessionCreateParams.BrandingSettings
                            .builder()
                            .setFontFamily(SessionCreateParams.BrandingSettings.FontFamily.ROBOTO)
                            .build())
                    .addLineItem(SessionCreateParams.LineItem.builder()
                            .setQuantity(1l)
                            .setPrice(planMonthly)
                            .build())
                    .build();
            Session session = client.v1().checkout().sessions().create(sessionCreateParams);
            System.out.println(session);
            return Optional.ofNullable(session).map(Session::getUrl);
        } catch (StripeException e) {
            log.warn("generateNewPlanUrl - exception: {}", e.getMessage());
            return Optional.empty();
        }
    }

    public Optional<SubscriptionDetailResponse> findDetails(String subscriptionId) {
        try {
            SubscriptionRetrieveParams params = SubscriptionRetrieveParams.builder()
                    .addExpand("default_payment_method").build();
            Subscription subscription = client.v1().subscriptions().retrieve(subscriptionId, params);
            return Optional.ofNullable(SubscriptionDetailResponse.from(subscription));
        } catch (StripeException e) {
            log.warn("findDetails - exception: {}", e.getMessage());
        }
        return Optional.empty();
    }

}
