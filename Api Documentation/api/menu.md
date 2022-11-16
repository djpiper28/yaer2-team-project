# /api/menu
This is the endpoint that will return a list of items that are on the menu.

## Request
HTTP GET to `/api/menu`. No headers are needed.

## Format
A json array of types and, a json array of the menu items. Each node contains the following properties:

### Menu Type Array
| property | type | meaning |
|---|---|---|
| uuid | string | the uuid of the menu type |
| name | string | the name of the type category |
| desc | string | the description of the type category |
| image-uri | string | an image to use on the frontend to represent this type |

### Menu Items Array
| property | type | meaning |
|---|---|---|
| price | float | the price of the menu item in pounds |
| name | string | the name of the menu item |
| description | string | the description of the menu item |
| image-uri | string | a relative uri to the image that represents the menu item |
| uuid | string (uuid) | the menu item's uuid stored as a hexadecimal string |
| active | boolean | whether the menu item is enabled and should be shown |
| in-stock | boolean | whether the menu item is in stock |
| item-type | string | the type id of the item, see the menu type array to get more info |

---

## Example
```json
{
	"types": [
	  {
	    "uuid": "3ef814db-c287-4808-ad1a-60e97381ddbb",
	    "name": "Type name",
		"desc": "yummy yummy",
	    "image-uri": "/cdn/cheese.png"
	  }
	],
	"items": [
	  {
	    "price": 3.99,
	    "name": "Cheese Burger",
	    "description": "Cheese burger with onions, sauce and stuff",
	    "image-uri": "/cdn/cheese_burger.png",
	    "uuid": "3ef814db-c287-4808-ad1a-60e97381ddbb",
	    "active": true,
	    "in-stock": true,
	    "item-type": "3ef814db-c287-4808-ad1a-60e97381ddbb"
	  },
	  {
	    "price": 3.49,
	    "name": "Ham Burger",
	    "description": "Ham burger with onions, sauce and stuff",
	    "image-uri": "/cdn/ham_burger.png",
	    "uuid": "87d8f637-c792-4008-8c54-15ee502c7eb0",
	    "active": true,
	    "in-stock": true,
	    "item-type": "3ef814db-c287-4808-ad1a-60e97381ddbb"
	  },
	  {
	    "price": 1.99,
	    "name": "Chips",
	    "description": "Chips cooked in sunflower oil with a light sprinkle of salt.",
	    "image-uri": "/cdn/chips.png",
	    "uuid": "7e6c237a-aca2-4cf1-911e-5c219b1eda4f",
	    "active": true,
	    "in -stock": true,
	    "item-type": "3ef814db-c287-4808-ad1a-60e97381ddbb"
	  }
	]
}
```
