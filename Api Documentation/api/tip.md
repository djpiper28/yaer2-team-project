# /api/tip
This is the endpoint that will place a tip.

## Request
HTTP POST to `/api/tip`. No headers are needed. The data is needed.

## Data Format
A json object that stores the nonce and, the tip details.
| property | type | meaning |
|---|---|---|
| order-id | uuid| the order ID of an order |
| amount | double | the amount of tip that is given to an order |
| nonce | string | the nonce for the privelleged gateway |
| access-token | string | the user's access token |

## Errors
 - An error occurred (500)

## Output Format

| property | type | meaning |
|---|---|---|
| success | boolean | the truth value of whether a tip has been placed |

---

## Example Data
```json
{
  "order-id": "abcd-ds893-3883-2323",
  "amount": 2.5,
  "nonce": "1233",
  "access-token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiIxMjM0NTY3ODkwIiwibmFtZSI6IkpvaG4gRG9lIiwiaWF0IjoxNTE2MjM5MDIyfQ.SflKxwRJSMeKKF2QT4fwpMeJf36POk6yJV_adQssw5c",
	
}
```

## Example Output
```json
{
  "success": true
}
```