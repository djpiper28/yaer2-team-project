# /api/login
This is the endpoint that will login a user

## Request
HTTP POST to `/api/login`. No headers are needed. The data is needed.

## Data Format
A json object that stores the nonce and, the user details
| property | type | meaning |
|---|---|---|
| nonce | string | the nonce that is for the register attempt |
| email | string | the user's email address |
| password | string | the user's password |

## Errors
 - An error occurred (500)
	 - There is only one error as not to leak any more information

## Output Format
A json object that stores the nonce that the user has been given.

| property | type | meaning |
|---|---|---|
| refresh-token | string | a jwt, this is the refresh token to get access tokens for the account |

---

## Example Data
```json
{
  "nonce": "1233",
  "email": "dchoen@gmail.com",
  "password": "crocs-123"
}

```

## Example Output
See [../JWT.md](../JWT.md).
```json
{
  "refresh-token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiIxMjM0NTY3ODkwIiwibmFtZSI6IkpvaG4gRG9lIiwiaWF0IjoxNTE2MjM5MDIyfQ.SflKxwRJSMeKKF2QT4fwpMeJf36POk6yJV_adQssw5c"
}
```