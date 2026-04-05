# Stripe documentation for ItiMenu

## Webhook

### Events we monitor
> source: [Provision and monitor subscriptions](https://docs.stripe.com/billing/subscriptions/build-subscriptions?payment-ui=checkout&lang=java#provision-and-monitor)

- customer.subscription.created -> when a new subscription occurs
- customer.subscription.deleted -> when a user cancel a subscription
- customer.subscription.update -> when a payment fail, it receives this object with status=past_due
- test how api(subscription) act when payment fail: what events are sent?

### Flow
> when a event come we only get the email and stripe customer id to update the subscription

- When user assign the subscription
  - Create the new subscription
  - Send greetings email
  - Is it in freetier mode?
    - Yes:
        - Set the subscription[free_tier] end date to today
    - No: nothing
- When user paid the monthly invoice
  - Update the subscription **ended_at** field
- When user fail to paid the monthly invoice
  - Send email to notificate user
  - Update subscriptions status to **PAYMENT_FAILED**
  - Should I block the use?(Maybe it can work only for 1 day)
- When user cancel
  - User still can use the app until ended_at
  - Send e-mail to customer
  - Update subscription status to **CANCELLED**


> Should(How) generate "notas fiscais": enotas.com.br, spedy.com.br
    


## Important Stripe links:
- [Track Active Subscriptions](https://docs.stripe.com/billing/subscriptions/webhooks#active-subscriptions)


