# /api/modifytableno
This is the endpoint that will modify the table number of an order.

## Request
HTTP POST to `/api/modifytableno. 

## Data Format
A json object that stores the nonce, the order ID, the new table number.
| property | type | meaning |
|---|---|---|
| order-id | uuid | the ID of an order |
| new-number | int | the new table number to modify to |
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
  "new-number": "5",
  "nonce": "1233",
  "access-token": "eyJhbGciOiJIUzI1NiIsInC5cCI6IkpXVCJ9.eyJzdWIiOiIxMjM0NTY3ODkwIiwibmFtZSI6IkpvaG4gRG9lIiwiaWF0IjoxNTE2MjM5MDIyfQ.SflKxwRJSMeKKF2QT4fwpMeJf36POk6yJV_adQssw5c",
}
```

## Example Output
```json
{
  "success": true
}
