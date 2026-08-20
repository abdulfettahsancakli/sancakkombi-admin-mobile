# Sancak Kombi Admin API Contract

The mobile client now expects the following backend behavior.

## Customer archive

`PATCH /api/admin/customers/{id}/archive` must set `isArchived = true` and keep all historical records. `GET /api/admin/customers` should omit archived customers by default.

## Finance linkage

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

Completing a job must apply stock movements idempotently by appointment/job revision. Editing a completed job applies only the quantity delta. Deleting the appointment creates reversal movements. A low-stock request may proceed after an explicit client confirmation.
