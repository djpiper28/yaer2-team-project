# /api/orderstatus

This is the endpoint that will change the status of an order from a given order ID.

## Request

HTTP POST to `/api/orderstatus. 

## Data Format

A json object that stores the nonce and, the order ID.
| property | type | meaning |
|---|---|---|
| order-id | uuid | the ID of an order |
| new-status | int | the new status code to be given to an order |
| nonce | string | the nonce for the privelleged gateway |
| access-token | string | the user's access token |

## Output Format

| property | type | meaning |
|---|---|---|
| success | boolean | the truth value of whether the order has been modified |

---

## Example Data

```json
{
  "order-id": "3ef814db-c287-4808-ad1a-60e97381ddbb",
  "new-status": 1,
  "nonce": "1233",
  "access-token": "eyJhbGciOiJIUzI1NiIsInC5cCI6IkpXVCJ9.eyJzdWIiOiIxMjM0NTY3ODkwIiwibmFtZSI6IkpvaG4gRG9lIiwiaWF0IjoxNTE2MjM5MDIyfQ.SflKxwRJSMeKKF2QT4fwpMeJf36POk6yJV_adQssw5c",
}
```

## Example Output

```json
{
  "success": true
}
```
