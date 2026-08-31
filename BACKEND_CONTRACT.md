# Sancak Kombi Admin API Contract

The mobile client now expects the following backend behavior.

## Customer archive

`PATCH /api/admin/customers/{id}/archive` must set `isArchived = true` and keep all historical records. `GET /api/admin/customers` should omit archived customers by default.

## Finance linkage

`PATCH /api/admin/finance/records/{id}` accepts `{ "status": "paid" | "partial" | "unpaid" }`.
It updates the existing finance record and returns the refreshed mobile record;
the mobile client must not create a duplicate income record for collection.

`POST /api/admin/finance/records` accepts only `GELIR`/`GIDER` records with a
positive finite amount, a valid status, and (when supplied) a valid ISO due
date. Invalid JSON or invalid financial values must be rejected with `400`.

Finance records created from a completed service must include:

- `appointmentId`
- `category`
- `source` containing the customer and service name
- `note` containing the service note only

`DELETE /api/admin/appointments/{id}` must run transactionally: archive/delete the appointment, remove or void linked income records, reverse linked stock movements, and return the recalculated finance summary.

## Receipt detail

`GET /api/admin/finance/receipt/{entryId}` should return the actual appointment-backed customer, address, device, work note, payment, warranty, service fee, other fee, and used parts. Do not return placeholder customer, phone, date, device, or warranty values.

## Catalog and inventory

- `GET/POST /api/admin/catalog/items`
- `GET/POST /api/admin/inventory/items`
- `GET/POST /api/admin/inventory/movements`

Creating an inventory item sends an empty `id`; the response returns the
server-generated item in `data`. Initial quantity is then recorded as an
`IN` movement using that returned ID. Inventory item quantity is never
written directly by the item endpoint.

Completing a job must apply stock movements idempotently by appointment/job revision. Editing a completed job applies only the quantity delta. Deleting the appointment creates reversal movements. A low-stock request may proceed after an explicit client confirmation.

The same `POST /api/admin/appointments/{id}/complete` endpoint is used to edit a completed job report. The backend must run the completed-report revision RPC after the new report payload is validated; report fields and stock revision must commit together.

## Central product-stock bridge

The public `products` catalogue and the admin/mobile `inventory_items` records
are linked by `inventory_items.product_id`. Apply
`supabase/migrations/20260830130000_central_inventory_products_bridge.sql`
before testing the shared stock flow. After that migration:

- `admin_record_stock_movement` updates the linked public product stock in the same transaction.
- Product removal uses `admin_archive_product`; it sets `products.is_archived=true` and keeps the linked inventory item and movement history intact.
- Direct legacy product stock edits are mirrored as auditable adjustments.
- The legacy stock panel's quick exit must use `admin_record_product_movement`; it must not insert directly into `movements`.
- Stock quantities must be changed through movement records, not by editing `inventory_items.quantity` directly.
WhatsApp Embedded Signup access tokens must remain server-side. The web connection endpoint returns only connection IDs/status; it must never return the Meta access token to the mobile or browser client.

## Voice appointment parsing

`POST /api/admin/voice/parse` accepts `{ "voiceText": "..." }` with the same
Bearer admin token used by the other mobile endpoints. Gemini is called only
by the web server using the server-only `GEMINI_API_KEY` environment variable;
the Android APK must not contain a Gemini key or call Google directly. If the
server-side key/service is unavailable, the mobile client may use its local
parser, but it must leave unknown customer, phone, address, date, and time
fields empty rather than inventing values.

## Build and release signing

The repository includes the Gradle Wrapper and uses Gradle 9.3.1, which is
required by Android Gradle Plugin 9.1.1. `gradlew.bat assembleDebug` produces a
device-test APK. Release builds require the existing publishing keystore;
provide `KEYSTORE_PATH`, `STORE_PASSWORD`, and `KEY_PASSWORD` as local/CI
secrets. Never generate or commit a replacement key for an already published
application.

Inventory card metadata updates should use
`supabase/migrations/20260831110000_inventory_item_metadata_rpc.sql`.
The RPC updates a linked product and inventory card in one transaction and
never changes quantity; quantity remains movement-ledger-owned.

## Payment accounts

`GET /api/admin/finance/bank-accounts` returns only the account details stored
in Supabase. The server must not substitute an embedded IBAN when the database
is unavailable or a field is empty. `isReady` is true only when bank, account
holder, and IBAN are all present.

The mobile screen must not invent a payment account, amount, or account key.
Missing account data or a non-positive amount must block copy/send actions.
The bank-account `PUT` endpoint must return `400` for malformed JSON or a
non-array body, and bank-transfer messaging must reject incomplete account
data before calling WhatsApp/Meta. The bank-transfer endpoint must also reject
malformed JSON and non-positive amounts before calling WhatsApp/Meta.

Completed-job finance payloads must preserve `paid`, `partial`, and `unpaid`
separately. Partial/unpaid records require a real payment promise date; an
empty or fabricated date must be rejected by the backend.
