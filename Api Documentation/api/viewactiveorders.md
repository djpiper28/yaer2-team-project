# /api/viewactiveorders
This is the endpoint that will return all active orders.

## Request
HTTP POST to `/api/viewactiveorders`. No headers are needed. The data is needed.

## Data Format
A json object that stores the nonce and, the user details
| property | type | meaning |
|---|---|---|
| nonce | string | the nonce for the privelleged gateway |
| access-token | string | the user's access token |
| table-number | int | the table number that the food should be sent to |
| itms | json array | an array of the order line json objects |

### Order Line Json Object
| property | type | meaning |
|---|---|---|
| menu-id | string | a string of the uuid of an item from the menu that was ordered |
| quantity | int | the number of the item (>= 0) |
| special-requests | string | any special requests for this item |

## Errors
 - An error occurred (500)

## Output Format
A json object that stores the nonce that the user has been given.

| property | type | meaning |
|---|---|---|
| order-id | string | the id of the order that was just placed|

---

## Example Data
```json
{
  "nonce": "1233",
  "access-token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiIxMjM0NTY3ODkwIiwibmFtZSI6IkpvaG4gRG9lIiwiaWF0IjoxNTE2MjM5MDIyfQ.SflKxwRJSMeKKF2QT4fwpMeJf36POk6yJV_adQssw5c",
  "table-number": 5,
  "items": [
  	{
	  "quantity": 1,
	  "menu-id": "abcd-ds893-3883-2323",
	  "special-requests": "extra cheese"
	}
  ]
}

```

## Example Output
Returns the id of an order that was just placed.
```json
{
  "menu-id": "abcd-ds893-3883-2323"
}
```