package db

import (
	"database/sql"
	"errors"
	"fmt"
	_ "github.com/lib/pq"
	"log"
	"teamproject/real-time-order/model"
	"teamproject/real-time-order/utils"
)

func GetConn(config utils.Config) (*sql.DB, error) {
	ConnStr := fmt.Sprintf("host=%s user=%s password=%s dbname=%s port=%d sslmode=disable TimeZone=Europe/London",
		config.DbUrl,
		config.DbUserName,
		config.DbPassword,
		config.DbName,
		config.DbPort)
	log.Printf("Connection string %s\n", ConnStr)

	db, err := sql.Open("postgres", ConnStr)

	if err == nil {
		log.Println("Successfully started gorm db connection")
	} else {
		log.Printf("An errors %s occurred connecting to the database\n",
			err)
	}
	return db, err
}

const GET_MENU_ITEMS = "select menuid, name, description from menuitems;"
const GET_ORDER_LINES = "select menuid, quantity, requests from orderlines where orderid = $1;"
const GET_ORDERS = "select orderid, customerid, tableno, status, cast(extract(epoch from ordertime) as bigint), cast(extract(epoch from lastchangetime) as bigint) from orders where status <> 3 and status <> 2;" // Show non-cancelled, non-complete orders
const GET_USER_TYPE = "select usetype from users where userid = $1;"

func GetActiveOrders(cache DbCache) ActiveOrders {
	return ActiveOrders{MenuItems: cache.MenuidToMenuItemMap, OrdersArr: cache.OrdersArr, NotifArr: cache.NotifArr}
}

func GetUserType(db *sql.DB, userid string) (int, error) {
	stmt, err := db.Prepare(GET_USER_TYPE)
	if err != nil {
		log.Printf("An error %s occurred when trying to select the user id\n",
			err)
		return -1, err
	}
	defer stmt.Close()

	rows, err := stmt.Query(userid)
	if err != nil {
		return -1, err
	}
	defer rows.Close()

	found := rows.Next()
	if !found {
		return -1, errors.New("Cannot find the user")
	}

	var ret int
	rows.Scan(&ret)

	return ret, nil
}

/*
 * InitCache will get a cache from the database and return it to you.
 *
 * @return a cache struct which stores the database snapshot,
 * err
 */
func InitCache(db *sql.DB) (DbCache, error) { 
	// Create a menu item map
	menuMap := make(map[string]model.MenuItem)
	rows, err := db.Query(GET_MENU_ITEMS)
	if err != nil {
		log.Printf("An error %s occurred when trying to select the menu items for the initial cache load\n",
			err)
		return DbCache{}, err
	}
	defer rows.Close()

	i := 0
	for rows.Next() {
		var menuid string
		var name string
		var desc string

		rows.Scan(&menuid, &name, &desc)
		item := model.MenuItem{Name: name,
			Description: desc}

		menuMap[menuid] = item
		i++
	}

	// Create the list of orders
	orders := make([]model.Order, 0)
	custidToOrderids := make(map[string][]string)
	orderIdToOrder := make(map[string]model.Order)

	rows, err = db.Query(GET_ORDERS)
	if err != nil {
		log.Printf("An error %s occurred when trying to select the orders for the initial cache load\n",
			err)
		return DbCache{}, err
	}
	defer rows.Close()

	// Get the orders and put them into the list and, maps
	j := 0
	i = 0
	for rows.Next() {
		var orderid string
		var customerid string
		var tableno int
		var status int
		var ordertime uint64
		var lastchangetime uint64

		rows.Scan(&orderid,
			&customerid,
			&tableno,
			&status,
			&ordertime,
			&lastchangetime)

		// Get the order lines
		orderlines := make([]model.OrderLine, 0)

		// Add the order id to the list
		custidToOrderids[customerid] = append(custidToOrderids[customerid], orderid)
		stmt, err := db.Prepare(GET_ORDER_LINES)
		if err != nil {
			log.Printf("An error %s occurred when preparing the statement to get the order lines\n",
				err)
			return DbCache{}, err
		}
		defer stmt.Close()

		irows, err := stmt.Query(orderid)
		defer irows.Close()
		if err != nil {
			log.Printf("An error %s occurred when execute the statement to get the order lines\n",
				err)
			return DbCache{}, err
		}
		defer irows.Close()

		for irows.Next() {
			var menuid string
			var quantity int
			var requests string

			irows.Scan(&menuid,
				&quantity,
				&requests)

			orderline := model.OrderLine{MenuId: menuid,
				Quantity:        quantity,
				SpecialRequests: requests}

			orderlines = append(orderlines, orderline)
			j++
		}

		// Add the order to the cache
		order := model.Order{OrderId: orderid,
			CustomerId:      customerid,
			TableNumber:     tableno,
			Status:          status,
			PlacedTime:      ordertime,
			LastUpdatedTime: lastchangetime,
			OrderLines:      orderlines}
		orders = append(orders, order)
		orderIdToOrder[orderid] = order
		i++
	}

	// Get notifications
	rows, err = db.Query("select notif_id, customerid, cast(extract(epoch from added_at) as bigint), body, table_no from notifications where deleted = false;")
	if err != nil {
		log.Printf("An error %s when getting notifs\n", err)
		return DbCache{}, err
	}
	defer rows.Close()

	notifs := make([]model.Notification, 0)
	for rows.Next() {
		var notifid string
		var customerid string
		var addedat uint64
		var body string
		var tableno int

		rows.Scan(&notifid, &customerid, &addedat, &body, &tableno)
		notif := model.Notification{Id: notifid,
  		CustomerId: customerid,
  		Body: body,
  		AddedAt: addedat,
  		TableNumber: tableno}

		notifs = append(notifs, notif)
	}

  cache := DbCache{OrdersArr: orders, 
    CustidToOrderidMap: custidToOrderids, 
    OrderidToOrderMap: orderIdToOrder, 
    MenuidToMenuItemMap: menuMap,
	  NotifArr: notifs}
  return cache, err
}

func Contains(m map[string]model.Order, key string) bool {
	_, cont := m[key]
	return cont
}

func ContainsSB(m map[string]bool, key string) bool {
	_, cont := m[key]
	return cont
}

func GetCacheUpdates(OldCache DbCache, NewCache DbCache) CacheUpdates {
	ret := CacheUpdates{MenuidToMenuItemMap: NewCache.MenuidToMenuItemMap,
		NewOrders:     make([]model.Order, 0),
		ChangedOrders: make([]model.Order, 0),
		RemovedOrders: make([]string, 0)}

	// Order updates
	i := 0
	for i < len(OldCache.OrdersArr) {
		order := OldCache.OrdersArr[i]
		if Contains(NewCache.OrderidToOrderMap, order.OrderId) {
			// Get changed orders
			newOrder := NewCache.OrderidToOrderMap[order.OrderId]
			if !model.CmpOrder(order, newOrder) {
				ret.ChangedOrders = append(ret.ChangedOrders, newOrder)
			}
		} else {
			// Get removed orders (items in old but not new)
			if !Contains(NewCache.OrderidToOrderMap, order.OrderId) {
				ret.RemovedOrders = append(ret.RemovedOrders, order.OrderId)
			}
		}

		i++
	}

	i = 0
	for i < len(NewCache.OrdersArr) {
		order := NewCache.OrdersArr[i]
		if !Contains(OldCache.OrderidToOrderMap, order.OrderId) {
			// Get new orders (items in new but not old)
			ret.NewOrders = append(ret.NewOrders, order)
		}

		i++
	}

	// Waiter updates
	// Create map of id to new cache notifs, the values are never used so a bool can be used to save on ram
	notifMapOld := make(map[string]bool)
	i = 0
	for i < len(OldCache.NotifArr) {
		notifMapOld[OldCache.NotifArr[i].Id] = true
		i++
	}

	notifMapNew := make(map[string]bool)
	i = 0
	for i < len(NewCache.NotifArr) {
		notifMapNew[NewCache.NotifArr[i].Id] = true
		i++
	}

	// Iterate over the last cache
	removedNotifs := make([]string, 0)

	i = 0
	for i < len(OldCache.NotifArr) {
		// If the notif is not in the new cache map mark it as deleted
		if !ContainsSB(notifMapNew, OldCache.NotifArr[i].Id) {
			removedNotifs = append(removedNotifs, OldCache.NotifArr[i].Id)
		}
		i++
	}

	// Iterate over the new cache
	addedNotifs := make([]model.Notification, 0)

	i = 0
	for i < len(NewCache.NotifArr) {
		// If the notif is not in the map mark it as new
		if !ContainsSB(notifMapOld, NewCache.NotifArr[i].Id) {
			addedNotifs = append(addedNotifs, NewCache.NotifArr[i])
		}
		i++
	}

	ret.NewNotifs = addedNotifs
	ret.RemovedNotifs = removedNotifs

	return ret
}
