## Purpose

Application CUPA is designed to implement all processes of Creditco UnionPay Acquaring services.

## Creditco UP Acquaring services explained

"Real" acquaring services are provided by OneRoadPayments using "MyGateway" software solution. Creditco acts as "Master Merchant" which solds acquaring
services to its clients - Merchants.
OneRoadPayments provides multichannel acquaring services: UnionPay, WeeChat, AliPay. It supports online and offline (email) payments.
Creditco has a few "Merchant" accounts in OneRoadPayments and multiple Merchants (own clients).
OneRoadPaymets provides some reporting and balances, but all is provided or generated doesn't split to Creditco clients - its Creditco responability
to organize the accounting and generate reports or carculate balances for each of its clients (Merchants).

## CUPA features

Creditco uses CUPA (this application) as a central component for its acquaring services.
CUPA application consists of a few logical components:

- Web application for management and monitoring (used by both Merchants and Creditco)
- MyGateway rest client - to access OneRoadPayment services using Creditco credentials and "MasterMerchant" keys
- REST Api to provide Creditco services to its clients (Merchants).
- Internal database with the configuration and transactions

## Application prototype and vision

CUPA version 0.0.14 is an application prototype for Proof of Concept. It was passed and now application should be prepared for GoLive.
CUPA version 0.0.14 was built using JHipster as a platform. It is great platform but too complicated (too expensive) to support by a small team of
Java developers (without dedicated FrontEnd developer). It is planned move to Vapp-base, Vaadin based UI application (using vapp-base project).
Vapp-base was built to migrate from JHipster applications, it provides many standard JHipster features like login and user management, has similar database
architecture. But all custome built UI should be migrated from Typescript to Vaadin. Vapp-base is a platform for all Creditco applications. It will share
the same codebase, modules (for instance PullTasks).

Migration to Vaadin will change the authentication from JWT based to Session based + stateless for API calls. Vapp-base provides JWT configuration but
it was not tested in production yet. Also CUPA will provide simplified API access using X-API-Key value in the request HTTP header. It is OK to
go Live with that custom X-API-Key based authentication.

Application will be deployed on one or a few dedicated servers as SpringBoot linux service. After vapp-base will migrate to SpringBoot 4 and the load increases - it is possible that application will migrate to containerized deployment.

## Requirements to UI

Single language (English) UI version but with configurable locales and time zones for users. UTC timezone used by default. The most important Local settings
are date and datetime formats. Latest vapp-base provides support for such a features.

UI is accessed by both Creditco and Merchant employees. Access is controlled by roles. There are three roles in use:

- ROLE_ADMIN - full access.
- ROLE_CREDITCO - Creditco employee. Almost all Merchant management features.
- ROLE_MERCHANT - UI for merchant. User has configured a list of merchants and its data which can be accessed.
- ROLE_USER - the purpose is not clear at the moment.

## Security

There are a few security implementation and layers:

- Session based for Vaadin UI - standard from Spring and Vaadin
- Stateless for REST API - uses X-API-Key values in HTTP headers. JWT access may be disabled and not used in first versions.

But also there is another data access layer: access to particular Merchant data. It is implemented by AllowedMIDs parameter on the user.

## Changes required to GoLive

While Proof of Concept phase was passed there are a few changes required to GoLive:

- Existing Merchant entity must be splitted into Merchant and MasterMerchant.
- Dynamic limit of allowed transaction count must be implementd for Merchants (clients)
- Prefixes must be required for ClientID and OrderId for all Merchant operations.
- Migration to Vapp-base.
- Integration guide must be prepared.

### MasterMerchants

Existing Merchant entity must be splitted into two:

- MasterMerchant - it contains configuration to connect OneRoadPayments. OneRoadPayments will report balances for our MasterMerchant only. Creditco's MasterMerchant is just Merchant in OneRoadPayments.
- Merchant - it is client of Creditco. It can access services using allowed MasterMerchant configuration. It doesn't have credential to access OneRoadPayments, only a fields for TEST and PROD X-API-Key. It also has setup for allowed transaction count limit, client and order id prefixes etc. User.allowedMids - is related to this entity.

### Dynamic limit of allowed transaction count

The limit of allowed transaction is required for newly onboarded Merchants. The limit settings contains:

- StartDate (localDate)
- initial allowed transaction count
- final date
- final allowed transaction count

The PaymentTransactionService should check:
if limits are set and we are between initial and final dates: calculate floor count of allowed transactions
if limits are set and we are after final date - use final limit
if limits are set and we are before initial date - use 0.

Limists are not applied to TEST mode.

### Prefixes for ClientID and OrderID

OneRoadPayments don't recognizes Creditco's Merchants. It is on Creditco. To simplify the job Creditco may set required prefixes for all MerchantClientID and OrderId values. Those prefixes are applied to the Merchant configuration.

### Integration guide

Integration guide should explain the most important entities and configuration options
should explain how to connect to the UI and API
should explain authentication

### Migration to Vapp-base

There are a few days before GoLive. But it looks like migration should be implemented before GoLive:

- because we need to develop new features, many of them involve UI
- we need to be ready to reuse other modules from vapp-base platform, first of all PullTasks
- migration will require SpringBoot update to 3.5, changes in security configuration may be needed.
- the need of rapid development of new features on UI improvements is prognozed after GoLive - as a demands of new Merchants.
