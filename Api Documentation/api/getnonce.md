# /api/getnonce
This is the endpoint that will return a list of items that are on the menu.

## Request
HTTP GET to `/api/menu`. No headers are needed.

## Format
A json object that stores the nonce that the user has been given.

| property | type | meaning |
|---|---|---|
| nonce | string | a string representation of a nonce |

---

## Example
```json
{
  "nonce": "12465"
}
```