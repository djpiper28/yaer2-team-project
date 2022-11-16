package model

type OrderLine struct {
	MenuId          string `json:"menu-id"`
	SpecialRequests string `json:"requests"`
	Quantity        int    `json:"quantity"`
}

type Order struct {
	OrderId         string      `json:"order-id"`
	OrderLines      []OrderLine `json:"order-lines"`
	CustomerId      string      `json:"customer-id"`
	TableNumber     int         `json:"table-no"`
	Status          int         `json:"status"`
	PlacedTime      uint64      `json:"placed-time"`
	LastUpdatedTime uint64      `json:"last-updated"`
}

func CmpOrder(Order1 Order, Order2 Order) bool {
	// Compare the order details
	ret := Order1.OrderId == Order2.OrderId && Order1.CustomerId == Order2.CustomerId && Order1.TableNumber == Order2.TableNumber && Order1.PlacedTime == Order2.PlacedTime && Order1.LastUpdatedTime == Order2.LastUpdatedTime && Order1.Status == Order2.Status

	if !ret {
		return false
	}

	// Compare the order lines, this is lazy as they are immutable in the backend
	return len(Order1.OrderLines) == len(Order2.OrderLines)
}
