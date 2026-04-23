# B2B vs B2C Commerce Systems

The buyer journey, data model, checkout flow, and pricing engine are fundamentally
different between B2B and B2C. Platform choice often follows from that difference.

Quick terms used in this note:

- `B2C` = `business to consumer`
- `B2B` = `business to business`
- `DTC` = `direct to consumer`
- `ERP` = `enterprise resource planning` system, such as SAP
- `ACL` = `anti-corruption layer`
- `EDI` = `Electronic Data Interchange`, a standard way to exchange business documents between companies
- `cXML` = `commerce XML`, a purchase-order and procurement message format
- `OCI` = `Open Catalog Interface`, a SAP-style punchout catalog protocol
- `OCC` = `OmniCommerce Connect`, the API layer exposed by SAP Hybris
- `Punchout` = letting a buyer browse a supplier catalog from inside the buyer's own procurement system

---

## 1. Core Differences — Buyer Model

| Dimension | B2C | B2B |
|---|---|---|
| **Buyer** | Individual consumer | Business entity (company → department → buyer) |
| **Decision** | Emotional, impulse-driven, fast | Rational, multi-stakeholder, slow |
| **Price** | Public list price | Negotiated contract price per account |
| **Payment** | Credit card, immediate | Invoice, net 30/60/90 terms, purchase order |
| **Checkout** | 2–3 steps, frictionless | Approval workflow (buyer → manager → finance) |
| **Order size** | Small, frequent, low-value | Large, infrequent, high-value |
| **Catalog** | Same for all users | Account-specific catalog visibility |
| **Tax** | Included in price display | Often tax-exempt; B2B VAT reclaim |
| **Loyalty** | Points, tiers, coupons | Volume discounts, rebates, annual contracts |
| **Relationship** | Transactional, anonymous | Account-managed, long-term contracts |

---

## 2. Data Model Differences

### B2C data model

```
User (anonymous or authenticated)
  └─ Cart → Order
              ├─ OrderEntry (SKU × qty × price)
              └─ Payment (credit card, captured at ship)
```

### B2B data model

```
Company (legal entity, VAT number, credit limit)
  └─ OrgUnit / Business Unit (department)
        └─ B2BCustomer (buyer — employee)
              └─ CostCenter (budget tracking)
                    └─ Cart → PurchaseOrder (PO number, approval status)
                                ├─ OrderEntry
                                └─ B2BPaymentType (ACCOUNT credit / purchase order)

ContractPrice (CompanyId × ProductId × price × validity dates)
BudgetExceeded → ApprovalWorkflow → EmailNotification
```

The key architectural difference: in B2B, **pricing is a function of the account identity**,
not just the SKU. The same bolt costs £0.40 for a walk-in customer and £0.27 for a
customer with a frame contract. This affects every read path in the system.

---

## 3. Hybris B2B Accelerator — How It Works

SAP Hybris ships two Accelerator templates out of the box: `yb2cstorefront` (B2C)
and `yb2baccelerator` (B2B). The B2B template adds the following capabilities:

### Account Hierarchy

```
B2BUnit (root company: "ACME Corp")
  ├─ B2BUnit (division: "Engineering")
  │     └─ B2BCustomer (buyer: alice@acme.com — role: BUYER)
  └─ B2BUnit (division: "Procurement")
        └─ B2BCustomer (approver: bob@acme.com — role: APPROVER)
```

A buyer places an order. If the total exceeds the CostCenter budget, the order enters
a `PENDING_APPROVAL` state. The approver receives an email and can approve/reject via
the storefront. Only on approval does the order flow to fulfillment.

### Contract Pricing in FlexibleSearch

```sql
-- Hybris PriceRow resolution for B2B: account-specific price has highest priority
SELECT {pr.price} FROM {PriceRow AS pr}
WHERE {pr.product} = ?product
AND {pr.ug} IN (
    SELECT {ug.pk} FROM {UserPriceGroup AS ug}
    JOIN {B2BUnit2PriceGroup AS rel} ON {rel.target} = {ug.pk}
    WHERE {rel.source} = ?currentB2BUnit
)
ORDER BY {pr.price} ASC
```

If no contract price exists, fall back to the standard B2C price row. This cascade
is performance-sensitive: eager-load the user price group at session start, cache it,
never re-query on every product page.

### Purchase Order Flow

```
Buyer adds to cart → Places order with PO number "PO-2024-001"
    → B2BOrderService checks: BudgetExceeded?
        YES → WorkflowService creates ApprovalTask → PENDING_APPROVAL
        NO  → Order proceeds directly to CREATED → fulfillment
```

### Punchout (OCI / cXML)

Large B2B buyers (automotive, pharma) want to browse the supplier catalog from inside
their own ERP (SAP Ariba, Coupa, Oracle). Punchout allows them to do so:

1. Buyer logs in to their ERP → clicks "Search Catalog" → ERP sends a punchout setup request to your Hybris
2. Hybris creates a punchout session, returns a URL to the ERP
3. ERP opens an iframe with the Hybris storefront
4. Buyer adds products → clicks "Checkout" → Hybris POSTs the cart back to the ERP as cXML
5. ERP creates a Purchase Order internally → sends it back to Hybris as a standard order

This is purely B2B. It does not exist in the B2C Accelerator.

---

## 4. Shopify B2B (Shopify Plus) — How It Works

Shopify's B2B features launched in 2022 (Shopify Plus only). The mental model is
simpler than Hybris B2B but covers the main use cases.

### Company Model

```
Company (e.g., "ACME Corp")
  ├─ CompanyLocation (billing/shipping address — US Office, EU Office)
  │     └─ Contact (buyer: alice@acme.com — role: Ordering, Admin)
  └─ PaymentTerms (Net 30, Net 60, Net 90)
  └─ PriceList (custom price list: 20% off all products for ACME)
```

One company → many locations → buyers are contacts assigned to locations.
Contacts can have `Ordering` or `Admin` roles. No native approval workflow (this
must be built with custom app logic or Shopify Functions).

### Price Lists

```graphql
mutation createCompanyPriceList {
  priceListCreate(input: {
    name: "ACME Corporate Pricing"
    currency: JPY
    fixedPricesAddedCount: 0
  }) {
    priceList {
      id
    }
  }
}
```

Price lists support:
- **Fixed price** per variant (override the list price completely)
- **Percentage adjustment** on all products (−20% for all B2B customers)
- **Volume pricing** tiers (buy 10+: ¥1,000; buy 100+: ¥850)

### Payment Terms

```graphql
mutation setPaymentTerms {
  paymentTermsCreate(input: {
    referenceId: "order_id"
    paymentTermsTemplateId: "gid://shopify/PaymentTermsTemplate/2"  # Net 30
  }) { ... }
}
```

Net 30/60/90 terms appear on the invoice PDF and trigger dunning reminders.
Payment is expected later — no credit card required at checkout.

### B2B Checkout Differences vs B2C

| Feature | B2C Shopify | B2B Shopify Plus |
|---|---|---|
| Login required | Optional (guest checkout) | Always required |
| Price display | Same for all | Company-specific price list |
| Payment at checkout | Card, PayPal, etc. | Card, or "Pay by invoice" (net terms) |
| Checkout customization | Checkout Extensibility | Same + B2B-aware APIs |
| Approval workflow | Not available | Not native — needs custom app |
| PO number | Not built-in | Supported via custom attributes |

### B2B via Headless (Hydrogen / Storefront API)

For full control, use Shopify's Storefront API with B2B context:

```typescript
// Identify buyer as B2B company contact
const storefront = createStorefrontClient({
  privateStorefrontToken: process.env.PRIVATE_STOREFRONT_TOKEN,
  storeDomain: process.env.PUBLIC_STORE_DOMAIN,
  buyerIp: request.headers.get('x-forwarded-for'),
  customerAccount: {                    // B2B buyer context
    id: session.companyContactId,
    accessToken: session.accessToken,
  },
});
```

Once the buyer context is set, all API calls return B2B-specific prices, catalogs,
and payment options automatically.

---

## 5. Hybris B2B vs Shopify Plus B2B — Decision Matrix

| Capability | Hybris B2B Accelerator | Shopify Plus B2B |
|---|---|---|
| Account hierarchy depth | Unlimited (tree structure) | Company → Location → Contact (3 levels) |
| Approval workflows | Native, configurable by amount | Not native — custom app required |
| Contract pricing | Full FlexibleSearch integration | Price lists per company |
| Volume tiers | Custom PriceRow logic | Native volume pricing |
| Punchout (cXML/OCI) | Native integration points | Not available natively |
| ERP integration | SAP S/4HANA native, IDOC/BAPI | Via API + middleware |
| Catalog visibility | Per-account catalog restrictions | Via catalogs (Headless API) |
| Customization cost | Very high (Java/Spring) | Lower (Shopify Functions + Apps) |
| Time to launch | 6–18 months | 1–3 months |
| Scale ceiling | Proven at enterprise scale | Proven mid-market, growing enterprise |

**Rule of thumb:**
- Choose **Hybris** when: deep ERP integration, complex approval workflows, punchout
  to Ariba/Coupa, 100k+ SKUs with complex pricing rules, strict data sovereignty.
- Choose **Shopify Plus** when: mid-market B2B, faster time-to-market, existing DTC
  on Shopify expanding to wholesale, simpler pricing models.

---

## 6. Channel Coexistence Patterns

Many commerce businesses run both a direct-to-consumer `B2C` channel and a
wholesale or partner-facing `B2B` channel. The hard problem is how those two
channels coexist without leaking prices, catalogs, or workflow rules across audiences.

```
Option A — Same platform, separate storefronts:
  Hybris: one backend, two Accelerator frontends (B2C storefront + B2B portal)
  Shared catalog, shared inventory, separate pricing rules
  Risk: B2B price leaks to B2C if the `ACL` (`anti-corruption layer`) is misconfigured

Option B — Separate platforms:
  B2C: Shopify Plus or custom React + Hybris OCC API
  B2B: Hybris B2B Accelerator or EDI directly to ERP
  Simpler security model, but double the maintenance

Option C — Headless unified backend, audience-aware frontend:
  Single OCC/GraphQL API, buyer identity determines price list + catalog
  One codebase, feature flags per channel
  Most modern — and most complex to implement correctly
```

Practical framing:
"In enterprise retail, B2B and B2C are not separate businesses — they share catalog,
inventory, and fulfillment infrastructure. The separation lives in the pricing engine,
the checkout flow, and the approval layer. My approach is Option C: a single headless
backend with buyer context determining which price list, catalog visibility, and payment
method to expose. The risk is ACL complexity — I enforce it at the API layer, not in
the frontend, so a misconfigured UI cannot leak wholesale prices to public traffic."
