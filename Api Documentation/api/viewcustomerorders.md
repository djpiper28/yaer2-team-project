# /api/viewcustomerorders
This is the endpoint that will return a list of orders for a given customer ID.

## Request
HTTP GET to `/api/viewcustomerorders`. No headers are needed.

## Data Format
A json object that stores the nonce and, the customer ID.
| property | type | meaning |
|---|---|---|
| nonce | string | the nonce for the privelleged gateway |
| access-token | string | the user's access token |

## Output Format
A json array with a set of identically formatted nodes. Each node contains the following properties:

| property | type | meaning |
|---|---|---|
| orderid | uuid | the order ID of an order |
| customerid | uuid | the customer ID assocated with an order |
| tableno | integer | the table number associated with an order |
| status | integer | the status of the order |
| ordertime | timestamp | the time at which an order was placed |
| lastchangedtime | timestamp | the last time an order was changed |

### Order Line Object
| property | type | meaning |
|---|---|---|
| menu-id | string | a string of the uuid of an item from the menu that was ordered |
| quantity | int | the number of the item (>= 0) |
| special-requests | string | any special requests for this item |

---

## Example Data
```json
{
  "nonce": "1233",
  "access-token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiIxMjM0NTY3ODkwIiwibmFtZSI6IkpvaG4gRG9lIiwiaWF0IjoxNTE2MjM5MDIyfQ.SflKxwRJSMeKKF2QT4fwpMeJf36POk6yJV_adQssw5c",
}
```

## Example Output
```json
[
  {
    "orderid": "3ef814db-c287-4808-ad1a-60e97381ddbb",
    "customerid": "3cf814db-c227-4818-ad1a-60e97381ddbb",
	"items": [
	  	{
		  "quantity": 1,
		  "menu-id": "abcd-ds893-3883-2323",
		  "special-requests": "extra cheese"
		}
	],
    "tableno": 3,
    "status": 0,
    "ordertime": "2022-02-21 10:23:54",
    "lastchangedtime": "2022-02-21 10:23:54",
  },
  {
    "orderid": "3ef814hb-g287-4848-ad1a-60e97381ddbb",
    "customerid": "3cf814db-c227-4818-ad1a-60e97381ddbb",
	"items": [
	  	{
		  "quantity": 1,
		  "menu-id": "abcd-ds893-3883-2323",
		  "special-requests": "extra cheese"
		}
	],
    "tableno": 3,
    "status": 1,
    "ordertime": "2022-02-21 10:28:54",
    "lastchangedtime": "2022-02-21 10:23:54",
  },
  {
    "orderid": "3ef814db-d687-4809-ad1a-60e97381ddbb",
    "customerid": "3cf814db-c227-4818-ad1a-60e97381ddbb",
	"items": [
	  	{
		  "quantity": 1,
		  "menu-id": "abcd-ds893-3883-2323",
		  "special-requests": "extra cheese"
		}
	],
    "tableno": 6,
    "status": 0,
    "ordertime": "2022-02-20 11:29:22",
    "lastchangedtime": "2022-02-20 11:23:54",
  }
]
