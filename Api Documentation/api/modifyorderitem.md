# /api/modifyorderitem
This is the endpoint that will modify an item of an order. Its main use is to overwrite requests. To change quantity or add items, another order can be placed. 

## Request
HTTP POST to `/api/modifyorderitem. 

## Data Format
A json object that stores the nonce, the order ID, the menu item ID, and the new orderline
| property | type | meaning |
|---|---|---|
| order-id | uuid | the ID of an order |
| nonce | string | the nonce for the privelleged gateway |
| access-token | string | the user's access token |
| order-lines | json array | an array of the old and new order line json objects (old must be first) |

### Order Line Json Object
| property | type | meaning |
|---|---|---|
| menu-id | string | a string of the uuid of an item from the menu that was ordered |
| quantity | int | the number of the item (>= 0) |
| special-requests | string | any special requests for this item |

## Output Format

| property | type | meaning |
|---|---|---|
| success | boolean | the truth value of whether the order has been modified |

---

## Example Data
```json
{
  "order-id": "3ef814db-c287-4808-ad1a-60e97381ddbb",
  "nonce": "1233",
  "access-token": "eyJhbGciOiJIUzI1NiIsInC5cCI6IkpXVCJ9.eyJzdWIiOiIxMjM0NTY3ODkwIiwibmFtZSI6IkpvaG4gRG9lIiwiaWF0IjoxNTE2MjM5MDIyfQ.SflKxwRJSMeKKF2QT4fwpMeJf36POk6yJV_adQssw5c",
  "order-lines": [
  	{
	  "quantity": 1,
	  "menu-id": "abcd-ds893-3883-2323",
	  "special-requests": "extra cheese"
	},
	{
	  "quantity": 1,
	  "menu-id": "abcd-ds893-3883-2323",
	  "special-requests": "even more than extra cheese"
	}
  ]
}
```

## Example Output
```json
{
  "success": true
}
