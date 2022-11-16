# /api/notify

This is the endpoint that will notify the waiters.

## Request

HTTP POST to `/api/notify`. No headers are needed. The data is needed.

## Data Format

A json object that stores the nonce and, the user details
| property | type | meaning |
|---|---|---|
| nonce | string | the nonce for the privelleged gateway |
| access-token | string | the user's access token |
| table-number | int | the table number that the food should be sent to |
| notif-body | string | the text to send to the waiter |

## Errors

- An error occurred (500)

## Output Format

A json object that stores the nonce that the user has been given.

| property | type    | meaning                              |
| -------- | ------- | ------------------------------------ |
| status   | boolean | whether the operation was successful |

---

## Example Data

```json
{
  "nonce": "1233",
  "access-token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiIxMjM0NTY3ODkwIiwibmFtZSI6IkpvaG4gRG9lIiwiaWF0IjoxNTE2MjM5MDIyfQ.SflKxwRJSMeKKF2QT4fwpMeJf36POk6yJV_adQssw5c",
  "table-no": 5,
  "notif-body": "Hello waiter! My food is raw and, my chair is not comfy and, the place smells. Grrrrr"
}
```

## Example Output

```json
{
  "success": true
}
```
