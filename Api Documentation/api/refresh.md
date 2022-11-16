# /api/refresh
This is the endpoint that will issue an access token for a user from a refresh token.

## Request
HTTP POST to `/api/refresh`. No headers are needed. The data is needed.

## Data Format
A json object that stores the nonce and, the user details
| property | type | meaning |
|---|---|---|
| nonce | string | the nonce that is for the register attempt |
| refresh-token | string | the refresh token |

## Errors
 - An error occurred (500)

## Output Format
A json object that stores the nonce that the user has been given.

| property | type | meaning |
|---|---|---|
| access-token | string | a jwt, this is the refresh token to get access tokens for the account |

---

## Example Data
```json
{
  "nonce": "1233",
  "refresh-token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiIxMjM0NTY3ODkwIiwibmFtZSI6IkpvaG4gRG9lIiwiaWF0IjoxNTE2MjM5MDIyfQ.SflKxwRJSMeKKF2QT4fwpMeJf36POk6yJV_adQssw5c"
}

```

## Example Output
See [../JWT.md](../JWT.md).
```json
{
  "access-token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiIxMjM0NTY3ODkwIiwibmFtZSI6IkpvaG4gRG9lIiwiaWF0IjoxNTE2MjM5MDIyfQ.SflKxwRJSMeKKF2QT4fwpMeJf36POk6yJV_adQssw5c"
}
```