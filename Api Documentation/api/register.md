# /api/register
This is the endpoint that will register a user.

## Request
HTTP POST to `/api/register`. No headers are needed. The data is needed.

## Data Format
A json object that stores the nonce and, the user details
| property | type | meanign |
|---|---|---|
| nonce | string | the nonce that is for the register attempt |
| email | string | the user's email address |
| phonenumber | string | the user's phone number |
| firstname | string | the user's first name |
| surname | string | the user's surname |
| password | string | the user's password |

## Errors
 - User already registered (406)
 - Another error occurred (500)

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
  "phonenumber": "0800001066",
  "firstname": "Dave",
  "surname": "Cohen",
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