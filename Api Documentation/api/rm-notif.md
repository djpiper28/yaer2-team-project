# /api/rm-notif
This is the endpoint that will mark a notification as deleted/dismissed.

## Request
HTTP POST to `/api/rm-notif`. No headers are needed. The data is needed.

## Data Format
A json object that stores the nonce and, the user details
| property | type | meaning |
|---|---|---|
| nonce | string | the nonce for the privelleged gateway |
| access-token | string | the user's access token |
| notif-id | string | the id of the notification to delete |

## Errors
 - An error occurred (500)
 - 403 if you are not a waiter

## Output Format
A json object that stores the nonce that the user has been given.

| property | type | meaning |
|---|---|---|
| status | boolean | whether the operation was successful |

---

## Example Data
```json
{
  "nonce": "1233",
  "access-token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiIxMjM0NTY3ODkwIiwibmFtZSI6IkpvaG4gRG9lIiwiaWF0IjoxNTE2MjM5MDIyfQ.SflKxwRJSMeKKF2QT4fwpMeJf36POk6yJV_adQssw5c",
  "notif-id": "132s1d35456d45646"
}

```

## Example Output
```json
{
  "success": true
}
```
