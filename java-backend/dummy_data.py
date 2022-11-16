import tables
import psycopg2
import uuid

def insert_items(cursor, name, description, price, prep_time, imageuri, typeid):
    imageuri = "/cdn/" + imageuri
    cursor.execute(
        "insert into menuitems (menuid, name, description, price, prep_time, imageuri, typeid) values (%s, %s, %s, %s, %s, %s, %s);",
        (str(uuid.uuid4()), name, description, price, prep_time, imageuri, typeid),
    )


def insert_types(cursor, item_type, desc):
    cursor.execute(
        "INSERT INTO menutypes (typeid, item_type, item_desc) VALUES (%s, %s, %s);",
        (str(uuid.uuid4()), item_type, desc),
    )


def getTypeUUID(cursor, queryType):
    cursor.execute(f"SELECT typeid FROM menutypes WHERE item_type = '{queryType}';")
    result = cursor.fetchall()
    return result[0][0]


def add(db):
    conn = psycopg2.connect(
        database=db,
        user="REDACTED",
        password="REDACTED",
        host="www.djpiper28.co.uk",
        port="6445",
    )
    cursor = conn.cursor()

    types = [
        ("Taco", "Warm, meaty treats."),
        ("Burrito", "Lovley warm burritos."),
        ("Drink", "Ice cold, refreshing beverages."),
        ("Quesadilla", "It's pretty good actually."),
        ("Side", "Warm, savoury snacks for all your cravings."),
        ("Dessert", "Sweet and, tasty puddings to finish your meal."),
    ]

    for table in tables.tables:
        print(f"Dropping table {table}")
        cursor.execute(f"drop table IF EXISTS {table} cascade;")

    f = open("create_db.sql")
    cursor.execute(f.read())
    f.close()

    for type in types:
        print(f"Inserting type {type}")
        insert_types(cursor, *type)

    items = [
        (
            "CHURROS - SMALL",
            "Just a simple, innocent, delicious cinnamon sugar snack with a glorious dip. Or is it? Just kidding. That’s all it is. Simple and innocent but equally delicious.",
            3.95,
            4,
            "churros-2.jpg",
            str(getTypeUUID(cursor, "Dessert")),
        ),
        (
            "CHURROS - LARGE",
            "A simple, innocent, delicious cinnamon sugar snack, six times over. Get dippin’.",
            5.45,
            2,
            "churros-1.jpg",
            str(getTypeUUID(cursor, "Dessert")),
        ),
        (
            "CINNAMON TWISTS - SMALL",
            "Do the twist. The cinnamon twist. Just a simple, innocent, delicious cinnamon sugar snack.",
            2.50,
            2,
            "cinnamon-twist-2.jpg",
            str(getTypeUUID(cursor, "Dessert")),
        ),
        (
            "CINNAMON TWISTS - LARGE",
            "Do the twist. The cinnamon twist. Just a simple, innocent, delicious cinnamon sugar snack.",
            4.69,
            4,
            "cinnamon-twist-1.jpg",
            str(getTypeUUID(cursor, "Dessert")),
        ),
        (
            "FRIES SUPREME",
            "A regular portion of seasoned fries topped with warm nacho cheese sauce, seasoned beef, diced tomatoes and cool sour cream.",
            2.50,
            3,
            "fries-1.jpg",
            str(getTypeUUID(cursor, "Side")),
        ),
        (
            "FRIES BELL GRADE",
            "A large portion of seasoned fries topped with extra warm nacho cheese sauce, seasoned beef, diced tomatoes and cool sour cream.",
            2.50,
            5,
            "fries-2.jpg",
            str(getTypeUUID(cursor, "Side")),
        ),
        (
            "MOUNTAIN DEW 500ml",
            "500ml bottle of Mountain Dew",
            0.95,
            1,
            "mtn-dew.jpg",
            str(getTypeUUID(cursor, "Drink")),
        ),
        (
            "NACHOS BELL GRANDE",
            "A large portion of seasoned nachos topped with extra warm nacho cheese sauce, black beans, seasoned beef, diced tomatoes and cool sour cream.",
            4.95,
            3,
            "nachos-1.jpg",
            str(getTypeUUID(cursor, "Side")),
        ),
        (
            "PEPSI MAX",
            "Quench that thirst.",
            0.95,
            4,
            "pepsi.jpg",
            str(getTypeUUID(cursor, "Drink")),
        ),
        (
            "SOL BEER",
            "Quench that thirst.",
            2.95,
            2,
            "sol-beer.jpg",
            str(getTypeUUID(cursor, "Drink")),
        ),
        (
            "TANGO",
            "Quench that thirst.",
            0.95,
            2,
            "tango.jpg",
            str(getTypeUUID(cursor, "Drink")),
        ),
        (
            "SOFT TACO",
            "Floating on a cloud. Instead of a tough corn shell, the Soft Taco has a warm, flour tortilla, but has the same ingredients with its seasoned beef, lettuce, and real cheddar cheese. It’s Dreamy. It’s Soft. It’s Delicious. It’s the Soft Taco.",
            1.19,
            4,
            "Soft-Taco-1370x650px.jpeg",
            str(getTypeUUID(cursor, "Taco")),
        ),
        (
            "SOFT TACO SUPREME",
            "While the Soft Taco is, you guessed it, soft, the Soft Taco Supreme® is even softer, but totally owns it. Seasoned beef, lettuce and real cheddar cheese, plus cool sour cream and fresh diced tomatoes makes this taco the real Supreme Queen.",
            2.29,
            5,
            "SoftTacoSupreme-1370x650px.jpeg",
            str(getTypeUUID(cursor, "Taco")),
        ),
        (
            "CRUNCHY TACO",
            "The Stuff of Legends. The Crunchy Taco has a crunchy corn body and a wicked haymaker. What you see is what you get with the Crunchy Taco; no hidden agendas with this guy. Hard. Crunchy. Delicious.",
            1.69,
            7,
            "Crunchy-Taco-1370x650px.jpeg",
            str(getTypeUUID(cursor, "Taco")),
        ),
        (
            "DORITOS CHEESY GORDITA CRUNCH",
            "Pillowy flatbread covered in melted cheese and wrapped around a crunchy taco shell filled with seasoned beef, crisp lettuce, cheddar cheese and Cali ranch sauce.",
            3.59,
            2,
            "Gordita-Crunch-1370x650px.jpeg",
            str(getTypeUUID(cursor, "Quesadilla")),
        ),
        (
            "BEEFY NACHO GRILLER",
            "Can’t decide if you want a burrito or nachos? Now you don’t need to! With the Beefy Nacho Griller, you can enjoy the best of both worlds.",
            2.99,
            3,
            "Beefy-Nacho-Griller-1370x650px.jpeg",
            str(getTypeUUID(cursor, "Burrito")),
        ),
        (
            "7 LAYER BURRITO",
            "A Burrito with 7 layers of greatness.",
            3.99,
            4,
            "7-Layer-Burrito-1370x650px.jpeg",
            str(getTypeUUID(cursor, "Burrito")),
        ),
        (
            "VOLCANO BURRITO",
            "The Volcano Burrito is the ‘all-rounder’ of Burritos. You want something spicy? Tick. You want something crunchy? Tick.",
            2.99,
            4,
            "VolcanoBurrito-1370x650px.jpeg",
            str(getTypeUUID(cursor, "Burrito")),
        ),
        (
            "CRISPY CHICKEN BURRITO",
            "Soft flour tortilla, two crispy chicken strips, creamy jalapeño style sauce, crisp lettuce, diced tomatoes and cheddar cheese. What’s not to love?",
            2.99,
            3,
            "CrispyChickenBurrito-1370x650px.jpeg",
            str(getTypeUUID(cursor, "Burrito")),
        ),
        (
            "QUESADILLA",
            "The rich, melty cheese sets the quesadilla apart. It’s folded and grilled flat for perfect portable snacking.",
            2.99,
            5,
            "Quesadilla-1370x650px.jpeg",
            str(getTypeUUID(cursor, "Quesadilla")),
        ),
        (
            "CHEESE QUESADILLA",
            "flour tortilla with hefty portion of three-cheese blend, and creamy jalapeño sauce.",
            1.99,
            5,
            "Cheese-Quesadilla-1370x650px.jpeg",
            str(getTypeUUID(cursor, "Quesadilla")),
        ),
    ]

    for item in items:
        print(f"Inserting {item[0]}")
        insert_items(cursor, *item)

    # Add John to the database
    cursor.execute(
        "insert into users (userid, firstname, lastname, email, phoneno, usetype, password, salt) values (%s, %s, %s, %s, %s, %s, %s, %s);",
        (
            "c3fea7ff-0515-42c2-8dde-f70d6c68e4ea",
            "john",
            "Costa",
            "john@email.com",
            "johns phone no",
            1,
            "b6132703ea4521edd1434e65556052e195bd53fb25f2d558a2bba1aa81e1a5ce156811a82ae62aa68433e0f7ef5385057452bbcda85a1e41a03e5c08715b312c",
            "2#4oF~',N,I8eE3EUkQu?'Wdh@bvF;T1gO<:,sGjL6UE3J;s#8LJ~G9kBGBv*V5PTtg8'yZHrvlKarA6T?sUNEnVT;6ku@'uV-Z,l>:38YG@*>M8D7OjvvFEd/YKt?*byFPxvv6-uHZJ-6q,9X5HPA.#4m2qH#:8/WrR.*trIX3X#,HlVT#p'OXsV254z@@F-Ln8z~tvlt8A*AV8xFwi/UhloGOr,i6f?nWEgW:LXW*Ve6lf.wB@htxEiDC'QHU",
        ),
    )

    conn.commit()
    conn.close()


add("teamdev")
