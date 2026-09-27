# API Documentation — SecureBank Digital Banking System

Base URL: `http://localhost:8080/api`

All responses (success and error) follow a consistent envelope:

```json
{
  "success": true,
  "message": "Human readable message",
  "data": { },
  "timestamp": "2026-09-26T10:15:30"
}
```

Errors:

```json
{
  "success": false,
  "status": 422,
  "error": "Insufficient Balance",
  "message": "Insufficient balance in account 100000000001 for this withdrawal",
  "path": "/api/transactions/withdraw",
  "details": null,
  "timestamp": "2026-09-26T10:15:30"
}
```

Authenticated requests must include:

```
Authorization: Bearer <jwt-token>
```

---

## Auth — `/api/auth`

| Method | Endpoint         | Auth | Description                    |
|--------|------------------|------|---------------------------------|
| POST   | `/auth/register` | No   | Register a new customer account |
| POST   | `/auth/login`    | No   | Log in and receive a JWT         |

**POST `/auth/register`**
```json
{
  "username": "johndoe",
  "email": "john@example.com",
  "password": "Password123",
  "firstName": "John",
  "lastName": "Doe",
  "phoneNumber": "9876543210",
  "address": "221B Baker Street"
}
```
`201 Created` → returns the created `UserDto`.

**POST `/auth/login`**
```json
{ "username": "johndoe", "password": "Password123" }
```
`200 OK` →
```json
{
  "token": "eyJhbGciOi...",
  "type": "Bearer",
  "id": 12,
  "username": "johndoe",
  "email": "john@example.com",
  "roles": ["ROLE_CUSTOMER"]
}
```

---

## Users — `/api/users`

| Method | Endpoint     | Auth | Description                 |
|--------|--------------|------|-------------------------------|
| GET    | `/users/me`  | Yes  | Get the logged-in user's profile |
| PUT    | `/users/me`  | Yes  | Update the logged-in user's profile |

---

## Accounts — `/api/accounts`

| Method | Endpoint                            | Auth | Description                             |
|--------|--------------------------------------|------|-------------------------------------------|
| POST   | `/accounts`                          | Yes  | Create a new account (SAVINGS/CURRENT)    |
| GET    | `/accounts`                          | Yes  | List the current user's accounts          |
| GET    | `/accounts/{accountId}`              | Yes  | Get a single account (must be the owner)  |
| GET    | `/accounts/{accountId}/transactions` | Yes  | Paginated transaction history for an account |

**POST `/accounts`**
```json
{ "accountType": "SAVINGS", "initialDeposit": 1000.00 }
```

---

## Transactions — `/api/transactions`

| Method | Endpoint               | Auth | Description                       |
|--------|-------------------------|------|-------------------------------------|
| POST   | `/transactions/deposit`  | Yes  | Deposit money into an owned account |
| POST   | `/transactions/withdraw` | Yes  | Withdraw money from an owned account |
| POST   | `/transactions/transfer` | Yes  | Transfer money to another account by account number |
| GET    | `/transactions/me`       | Yes  | Paginated transaction history across all of the user's accounts |

**POST `/transactions/deposit`**
```json
{ "accountId": 10, "amount": 250.50, "description": "Salary credit" }
```

**POST `/transactions/withdraw`**
```json
{ "accountId": 10, "amount": 100.00, "description": "ATM withdrawal" }
```
Returns `422 Unprocessable Entity` if the balance is insufficient.

**POST `/transactions/transfer`**
```json
{
  "fromAccountId": 10,
  "toAccountNumber": "100000000002",
  "amount": 300.00,
  "description": "Rent payment"
}
```
Returns `400 Bad Request` for a same-account transfer, `404` if the destination account
number doesn't exist, `422` for insufficient funds.

---

## Beneficiaries — `/api/beneficiaries`

| Method | Endpoint              | Auth | Description                     |
|--------|------------------------|------|-----------------------------------|
| POST   | `/beneficiaries`       | Yes  | Add a beneficiary (must reference an existing account number) |
| GET    | `/beneficiaries`       | Yes  | List the current user's beneficiaries |
| DELETE | `/beneficiaries/{id}`  | Yes  | Remove a beneficiary               |

---

## Admin — `/api/admin` (requires `ROLE_ADMIN`)

| Method | Endpoint                              | Description                                |
|--------|-----------------------------------------|----------------------------------------------|
| GET    | `/admin/dashboard`                      | Bank-wide statistics                        |
| GET    | `/admin/customers?keyword=&page=&size=` | Search + paginate customers                 |
| PATCH  | `/admin/customers/{userId}/status?enabled=true|false` | Activate/deactivate a customer login |
| GET    | `/admin/accounts?keyword=&page=&size=`  | Search + paginate accounts                  |
| GET    | `/admin/accounts/status/{status}`       | Filter accounts by status (ACTIVE/INACTIVE/CLOSED) |
| PATCH  | `/admin/accounts/{accountId}/status?status=INACTIVE` | Change an account's status |
| GET    | `/admin/transactions?type=&status=&page=&size=` | Search + paginate + filter all transactions |

---

## HTTP Status Codes Used

| Code | Meaning                                                        |
|------|------------------------------------------------------------------|
| 200  | Success                                                          |
| 201  | Resource created (register, create account, deposit/withdraw/transfer, add beneficiary) |
| 400  | Bad request / invalid operation (validation errors, same-account transfer, inactive account) |
| 401  | Missing/invalid/expired JWT, or bad login credentials            |
| 403  | Authenticated but not authorized for this resource (wrong owner, or non-admin hitting `/admin/**`) |
| 404  | Resource not found (account, user, beneficiary, transaction)     |
| 409  | Conflict (duplicate username/email/beneficiary, concurrent modification) |
| 422  | Insufficient balance                                             |
| 500  | Unexpected server error                                          |

---

## Pagination

Any endpoint returning a page uses this envelope for `data`:

```json
{
  "content": [ ... ],
  "pageNumber": 0,
  "pageSize": 10,
  "totalElements": 42,
  "totalPages": 5,
  "last": false
}
```

Query parameters: `page` (0-indexed, default `0`), `size` (default `10`).
