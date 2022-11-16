# Real Time Order Service
Connect via websocket to the server (default http://localhost:8010) with the `Authorization`
parameter set to a staff (waiter or kitchen) JWT.
I.e: `http://localhost:8010&Authorization=asdjhiasghydasoiudgasiuod/.diaopsduaposiyduai8sdyasoiud`

## Initial Sent Response
- A map of menu ids to menu item details.
- A list of orders and, their order lines.

```json
{
  "menu-items": {
    "04f7e017-c002-429a-a8db-2ab475e1c1b5": {
      "name": "PEPSI MAX",
      "desc": "Quench that thirst."
    },
    "05b88c0d-66d1-4a46-a64a-14dd254518a8": {
      "name": "CHURROS - LARGE",
      "desc": "A simple, innocent, delicious cinnamon sugar snack, six times over. Get dippin’."
    },
    "08bd520a-9c52-4fd7-aa92-b681ae823cfb": {
      "name": "CRUNCHY TACO",
      "desc": "The Stuff of Legends. The Crunchy Taco has a crunchy corn body and a wicked haymaker. What you see is what you get with the Crunchy Taco; no hidden agendas with this guy. Hard. Crunchy. Delicious."
    }
  },
  "orders": [
    {
      "order-id": "0de6f6d9-125c-4064-badf-7fd4871864a4",
      "order-lines": [
        {
          "menu-id": "1e7bcb49-e77b-408e-9ae1-00c3f355ace5",
          "requests": "Please work",
          "quantity": 4
        }
      ],
      "customer-id": "c3caf127-9916-4756-89a2-9a498900e7a2",
      "table-no": 1,
      "status": 0,
      "placed-time": 0,
      "last-updated": 0
    }
  ]
}
```

## Further Sent Data
 - The same menu map
 - A list of added orders (in full detail)
 - A list of removed orders (a list of ids)
 - A list of changed ordres (in full detail)

 ```json
 {
  "menu-items": {
    "04f7e017-c002-429a-a8db-2ab475e1c1b5": {
      "name": "PEPSI MAX",
      "desc": "Quench that thirst."
    },
    "05b88c0d-66d1-4a46-a64a-14dd254518a8": {
      "name": "CHURROS - LARGE",
      "desc": "A simple, innocent, delicious cinnamon sugar snack, six times over. Get dippin’."
    },
    "08bd520a-9c52-4fd7-aa92-b681ae823cfb": {
      "name": "CRUNCHY TACO",
      "desc": "The Stuff of Legends. The Crunchy Taco has a crunchy corn body and a wicked haymaker. What you see is what you get with the Crunchy Taco; no hidden agendas with this guy. Hard. Crunchy. Delicious."
    },
    "10b77474-f0a2-4399-980c-45f060193efc": {
      "name": "VOLCANO BURRITO",
      "desc": "The Volcano Burrito is the ‘all-rounder’ of Burritos. You want something spicy? Tick. You want something crunchy? Tick."
    },
    "18597e07-5a29-4cb9-8ed1-3571ac1bef9b": {
      "name": "NACHOS BELL GRANDE",
      "desc": "A large portion of seasoned nachos topped with extra warm nacho cheese sauce, black beans, seasoned beef, diced tomatoes and cool sour cream."
    },
    "1aaac395-5233-45f8-9083-0a868d93d4cd": {
      "name": "FRIES BELL GRADE",
      "desc": "A large portion of seasoned fries topped with extra warm nacho cheese sauce, seasoned beef, diced tomatoes and cool sour cream."
    },
    "1e7bcb49-e77b-408e-9ae1-00c3f355ace5": {
      "name": "SOFT TACO",
      "desc": "Floating on a cloud. Instead of a tough corn shell, the Soft Taco has a warm, flour tortilla, but has the same ingredients with its seasoned beef, lettuce, and real cheddar cheese. It’s Dreamy. It’s Soft. It’s Delicious. It’s the Soft Taco."
    },
    "2ca0eea4-0f87-421b-8610-3eae747bcc69": {
      "name": "CINNAMON TWISTS - LARGE",
      "desc": "Do the twist. The cinnamon twist. Just a simple, innocent, delicious cinnamon sugar snack."
    
  },
  "new-orders": [
    {
      "order-id": "555d412d-68dd-44ac-b231-e2d7a6d5c403",
      "order-lines": [
        {
          "menu-id": "1e7bcb49-e77b-408e-9ae1-00c3f355ace5",
          "requests": "Please work",
          "quantity": 4
        }
      ],
      "customer-id": "c3caf127-9916-4756-89a2-9a498900e7a2",
      "table-no": 1,
      "status": 0,
      "placed-time": 0,
      "last-updated": 0
    } 
  ],
  "changed-orders": [
    {
      "order-id": "70f1a69d-41be-4e0e-9fba-6c8e96fbd3c6",
      "order-lines": [
        {
          "menu-id": "1e7bcb49-e77b-408e-9ae1-00c3f355ace5",
          "requests": "Please work",
          "quantity": 4
        }
      ],
      "customer-id": "c3caf127-9916-4756-89a2-9a498900e7a2",
      "table-no": 1,
      "status": 0,
      "placed-time": 0,
      "last-updated": 0
    }
  ],
  "removed-orders": ["c3caf127-9916-4756-89a2-9a498900e7a2"]
}
```

