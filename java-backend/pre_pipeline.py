import tables
import psycopg2
import uuid
import traceback

def insert_items(cursor, name, description, price, prep_time, active, invItems, typeid):
    menuid = str(uuid.uuid4())
    cursor.execute(
        "insert into menuitems (menuid, name, description, price, imageuri, prep_time, active, typeid) values (%s, %s, %s, %s, 'https://www.postgresql.org/media/img/about/press/elephant.png', %s, %s, %s);",
        (menuid, name, description, price, prep_time, active, typeid),
    )

    for invitem in invItems:
        invuuid = str(uuid.uuid4())
        cursor.execute(
            "insert into inventoryitems (invid, itemname, amount) values (%s, %s, %s);",
            (invuuid, invitem.name, invitem.amount),
        )
        cursor.execute(
            "insert into inventorymenu (menuid, invid, unitsrequired) values (%s, %s, %s);",
            (menuid, invuuid, invitem.unitsrequired),
        )


def insert_types(cursor, item_type, desc):
    cursor.execute(
        "INSERT INTO menutypes (typeid, item_type, item_desc) VALUES (%s, %s, %s);",
        (str(uuid.uuid4()), item_type, desc),
    )


def insert_users(cursor, email, fname, sname, phoneno, user_type):
    cursor.execute(
        "insert into users (userid, firstname, lastname, email, phoneno, usetype, password, salt) values (%s, %s, %s, %s, %s, %s, %s, %s);",
        (
            str(uuid.uuid4()),
            fname,
            sname,
            email,
            phoneno,
            user_type,
            "password",
            "salty salt",
        ),
    )


def getTypeUUID(cursor, queryType):
    cursor.execute(f"SELECT typeid FROM menutypes WHERE item_type = '{queryType}';")
    result = cursor.fetchall()
    return result[0][0]

db_name = "testenv"

# Drop tables
try:
    conn = psycopg2.connect(
        database=db_name,
        user="REDACTED",
        password="REDACTED",
        host="www.djpiper28.co.uk",
        port="6445",
    )

    cursor = conn.cursor()
      
    for table in tables.tables:
        print(f"Dropping {table}")
        cursor.execute(f"drop table IF EXISTS {table} cascade;")

    conn.commit()
    conn.close()
except:
    traceback.print_exc()

# Create tables

conn = psycopg2.connect(
    database=db_name,
    user="dev",
    password="REDACTED",
    host="www.djpiper28.co.uk",
    port="6445",
)

cursor = conn.cursor()
f = open("create_db.sql")
cursor.execute(f.read())
f.close()

conn.commit()
conn.close()

# Add test menu
conn = psycopg2.connect(
    database=db_name,
    user="dev",
    password="REDACTED",
    host="www.djpiper28.co.uk",
    port="6445",
)

class InvItem:
    def __init__(self, name, amount, unitsrequired):
        self.name = name
        self.amount = amount
        self.unitsrequired = unitsrequired

cursor = conn.cursor()
cursor.execute(
    "insert into users(userid, firstname, lastname, email, phoneno, usetype, password, salt) values(%s, %s, %s, %s, %s, %s, %s, %s);",
    (
        str(uuid.uuid4()),
        "Firstname",
        "Surname",
        "email@email.test",
        "phoneno",
        0,
        "password",
        "salt",
    ),
)

types = [
    ("unassigned", "very nice"),
    ("taco", "also nice"),
    ("burrito", "vile"),
    ("drink", "also vile"),
    ("quesadilla", "pants"),
    ("side", "wee wee"),
    ("dessert", "my test data is professional"),
]

for type in types:
    print(f"Inserting type {type}")
    insert_types(cursor, *type)

items = [
    ("Soft Taco", "a very soft taco", 1.19, 4, True, [], str(getTypeUUID(cursor, "taco"))),
    (
        "Soft Taco Supreme",
        "a bigger one",
        2.29,
        3,
        True,
        [],
        str(getTypeUUID(cursor, "quesadilla")),
    ),
    (
        "Chips",
        "salted chips with sauce",
        5.99,
        2,
        True,
        [],
        str(getTypeUUID(cursor, "taco")),
    ),
    (
        "McDonalds Cheese Burger",
        "a beef burger",
        0.99,
        4,
        True,
        [],
        str(getTypeUUID(cursor, "taco")),
    ),
    (
        "An inactive item",
        "very exciting",
        180,
        5,
        False,
        [],
        str(getTypeUUID(cursor, "taco")),
    ),
    (
        "not in stock",
        "y",
        190,
        1,
        True,
        [InvItem("test", 50, 100), InvItem("big chungus", 50, 2)],
        str(getTypeUUID(cursor, "taco")),
    ),
    (
        "big chungus",
        "big",
        5,
        3,
        True,
        [InvItem("chungus", 100, 10)],
        str(getTypeUUID(cursor, "taco")),
    ),
    (
        "test2",
        "desc",
        190,
        2,
        True,
        [InvItem("item 1", 500, 50), InvItem("dropped item", 100, 10)],
        str(getTypeUUID(cursor, "taco")),
    ),
]

for item in items:
    print(f"Inserting {item[0]}")
    insert_items(cursor, *item)

CUSTOMER = 0
WAITER = 1
KITCHEN = 2

users = [
    (
        "test email",
        "John",
        "Cena",
        "idk what the phone number for john cena is",
        CUSTOMER,
    ),
]

for user in users:
    print(f"Inserting {user[0]}")
    insert_users(cursor, *user)

conn.commit()
conn.close()
