package db

import (
	"teamproject/real-time-order/model"
)

type DbCache struct {
	OrdersArr           []model.Order
	CustidToOrderidMap  map[string][]string
	OrderidToOrderMap   map[string]model.Order
	MenuidToMenuItemMap map[string]model.MenuItem
	NotifArr            []model.Notification
}

type ActiveOrders struct {
	MenuItems map[string]model.MenuItem `json:"menu-items"`
	OrdersArr []model.Order             `json:"orders"`
	NotifArr  []model.Notification      `json:"notifications"`
}

// Kitchen and waiter updates to orders
type CacheUpdates struct {
	MenuidToMenuItemMap map[string]model.MenuItem `json:"menu-items"`
	NewOrders           []model.Order             `json:"new-orders"`
	ChangedOrders       []model.Order             `json:"changed-orders"`
	RemovedOrders       []string                  `json:"removed-orders"`
	/* This is a string because the rest of the details of the order are not needed */
	NewNotifs     []model.Notification `json:"new-notifs"`
	RemovedNotifs []string             `json:"removed-notifs"`
	/* This is a string because the rest of the details of the notification is not needed */
}
