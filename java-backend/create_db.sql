CREATE TABLE users(
    userid      uuid PRIMARY KEY,
    firstname   varchar(255) NOT NULL,
    lastname    varchar(255) NOT NULL,
    email       varchar(255) NOT NULL UNIQUE,
    phoneno     varchar(255) NOT NULL UNIQUE,
    usetype     integer NOT NULL,
    password    varchar(255) NOT NULL,
    salt        varchar(255) NOT NULL
);

create index u_emails on users(email);
create index u_password on users(password);
create index u_salt on users(salt);
create index u_type on users(usetype);

CREATE TABLE menutypes(
    typeid      uuid PRIMARY KEY,
    item_type   varchar(255) UNIQUE NOT NULL,
    item_desc   varchar(255) not null,
    item_image  varchar(255) not null default('/cdn/default_image.jpg')
);

create index mt_type on menutypes(item_type);
create index mt_image on menutypes(item_image);
create index mt_desc on menutypes(item_desc);

CREATE TABLE menuitems(
    menuid      uuid PRIMARY KEY,
    name        varchar(255) not null,
    description varchar(255) not null,
    price       money not null check (menuitems.price >= '0.00'),
    imageuri    varchar(255) not null default('/cdn/default_image.jpg'),
    active      boolean not null default true,
    prep_time   integer NOT NULL check (menuitems.prep_time >= 0),
    typeid      UUID REFERENCES menutypes(typeid) NOT NULL,
    added_at    timestamp not null default CURRENT_TIMESTAMP
);

create index m_name on menuitems(name);
create index m_desc on menuitems(description);
create index m_price on menuitems(price);
create index m_imageuri on menuitems(imageuri);
create index m_active on menuitems(active);
create index m_type on menuitems(typeid);
create index m_added on menuitems(added_at);
create index m_prep_time on menuitems(prep_time);

CREATE TABLE inventoryitems(
    invid       uuid primary key,
    itemname    varchar(255) not null,
    amount      integer not NULL check(inventoryitems.amount >= 0)
);

create index ii_name on inventoryitems(itemname);
create index ii_amount on inventoryitems(amount);

CREATE TABLE inventorymenu(
    menuid      uuid references menuitems(menuid) not null,
    invid       uuid references inventoryitems(invid) not null,
    unitsrequired integer not NULL check(inventorymenu.unitsrequired > 0),
    unique(menuid, invid)
);

create index im_invid on inventorymenu(invid);
create index im_menuid on inventorymenu(menuid);
create index im_units on inventorymenu(unitsrequired);

CREATE TABLE orders(
    orderid    uuid  PRIMARY KEY,
    customerid uuid NOT NULL REFERENCES users(userid),
    tableno    integer  NOT NULL,
    status     integer NOT NULL,
    ordertime  timestamp NOT NULL,
    lastchangetime timestamp NOT NULL
);

create index o_orderid on orders(orderid);
create index o_custid on orders(customerid);
create index o_tableno on orders(tableno);
create index o_status on orders(status);
create index o_ordertime on orders(ordertime);
create index o_lastchangedtime on orders(lastchangetime);

CREATE TABLE orderlines(
    orderid     uuid references orders(orderid) not null,
    menuid      uuid references menuitems(menuid) not null,
    quantity    integer  not null check (orderlines.quantity > 0),
    requests    varchar(255) not null,
    unique(orderid, menuid, requests)
);

create index ol_orderid on orderlines(orderid);
create index ol_menuid on orderlines(menuid);
create index ol_requests on orderlines(requests);
create index ol_quantity on orderlines(quantity);

CREATE TABLE tips (
    orderid uuid references orders(orderid),
    amount money check (tips.amount > '0.00')
);

create index t_id on tips(orderid);
create index t_amount on tips(amount);

CREATE TABLE allergens(
    allergenid UUID PRIMARY KEY,
    allergen_name varchar(25) UNIQUE NOT NULL
);

CREATE TABLE allergen_map(
    menuid UUID REFERENCES menuitems(menuid) NOT NULL,
    allergenid UUID REFERENCES allergens(allergenid) NOT NULL
);

create table notifications(
	  notif_id uuid primary key,
    customerid uuid references users(userid) not null,
    added_at timestamp not null default CURRENT_TIMESTAMP,
    body varchar(256) not null,
    table_no integer not null,
    deleted boolean not null
);

create index n_id on notifications(notif_id);
create index n_cust_id on notifications(customerid);
create index n_added_ad on notifications(added_at);
create index n_table_no on notifications(table_no);

