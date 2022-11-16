package model

type Notification struct {
	Id          string `json:"id"`
	CustomerId  string `json:"cust-id"`
	Body        string `json:"body"`
	AddedAt     uint64 `json:"added-at"`
	TableNumber int    `json:"table-no"`
}

func CmpNotif(a Notification, b Notification) bool {
	return a.Id == b.Id // These are the only fields in the notifications that we care about
}
