package main

import (
	"encoding/json"
	"fmt"
	"github.com/gobwas/ws"
	"github.com/gobwas/ws/wsutil"
	"log"
	"net/http"
	"sync"
	"teamproject/real-time-order/db"
	"teamproject/real-time-order/model"
	"teamproject/real-time-order/utils"
	"time"
)

const OP = 1 // ws op code

func main() {
	log.Println("Reading configuration")
	conf := utils.LoadConfig()

	// Prepare database connections
	conn, err := db.GetConn(conf)
	defer conn.Close()
	if err != nil {
		log.Fatalf("Cannot init connection to database due to %s\n",
			err)
	}

	log.Printf("Connected to the db successfully\n")

	// Load the initial cache
	log.Println("Loading initial cache")
	Cache, err := db.InitCache(conn)

	if err != nil {
		log.Panicf("Error: %s\n", err)
	}

	log.Println("Loaded the initial cache")

	// Start the polling
	LastUpdate := time.Now().UnixNano() // The last updated time, conns compare their copy of
	//this to this to see if they need to send new data
	CacheUpdates := db.CacheUpdates{} // The cache update info
	SendUpdate := false               // This flag is used to stop sending updates when the cache update has no use

	var CounterLock sync.Mutex
	var Readers int
	var WriteLock sync.WaitGroup

	// Updater thread
	go func() {
		for true {
			newCache, err := db.InitCache(conn)

			// On error reset the connection then return to the start of this loop
			if err != nil {
				log.Printf("An error %s occurred when updating the cache\n", err)
				continue
			}

			// Enter the critical region
			WriteLock.Add(1)
			for true {
				CounterLock.Lock()
				if Readers == 0 {
					CounterLock.Unlock()
					break
				}
				CounterLock.Unlock()
			}

			// Update the cache
			CacheUpdates = db.GetCacheUpdates(Cache, newCache)
			SendUpdate = len(CacheUpdates.NewOrders)+len(CacheUpdates.ChangedOrders)+len(CacheUpdates.RemovedOrders)+len(CacheUpdates.RemovedNotifs)+len(CacheUpdates.NewNotifs) > 0
			LastUpdate = time.Now().UnixNano()
			Cache = newCache

			// Exit the critical region
			WriteLock.Done()

			if SendUpdate {
				log.Println("The cache has changed.")
				utils.PrintMemStats()
			}
			time.Sleep(100 * time.Millisecond)
		}

		log.Fatal("Exited updates loop")
	}()

	// Start the server
	utils.PrintMemStats()
	log.Println("Running final startup")

	ServerAddr := fmt.Sprintf("%s:%d",
		conf.BindAddr,
		conf.BindPort)
	log.Printf("Running real time order system on ws://%s\n",
		ServerAddr)

	http.ListenAndServe(ServerAddr, http.HandlerFunc(func(w http.ResponseWriter, r *http.Request) {
		// Get a connection
		dbconn, err := db.GetConn(conf)
		defer dbconn.Close()

		// Check "Authorization" parameter
		Query := r.URL.Query()
		JwtIn := Query.Get("Authorization")
		Claims, err := utils.CheckJwt(JwtIn, conf)

		if err != nil {
			fmt.Fprintln(w, "Invalid jwt in the Authorization header.")
			w.WriteHeader(http.StatusBadRequest)

			log.Printf("Error %s when checking the jwt \n", err)
			return
		}

		usetype, err := db.GetUserType(dbconn, Claims.Subject)
		if err != nil {
			fmt.Fprintln(w, "Invalid user id in the Authorization header.")
			w.WriteHeader(400)

			log.Printf("Error %s when checking the user type\n", err)
			return
		}

		if usetype != model.KITCHEN && usetype != model.WAITER {
			fmt.Fprintln(w, "The user has the wrong account type (not staff)")
			w.WriteHeader(403)

			log.Println("The user is not a member of staff.")
			return
		}

		// JWT claims are valid and, a websocket can now be created.
		conn, _, _, err := ws.UpgradeHTTP(r, w)
		if err != nil {
			log.Println(err)
			return
		}

		// Real time updates
		go func() {
			defer conn.Close()

			// Enter the critical region
			WriteLock.Wait()
			CounterLock.Lock()
			Readers++
			if Readers == 1 {
				WriteLock.Wait()
			}
			CounterLock.Unlock()

			// Send the initial status
			active := db.GetActiveOrders(Cache)
			lUpdate := LastUpdate

			// Exit critical after copy
			CounterLock.Lock()
			Readers--
			CounterLock.Unlock()

			kitchenorders, err := json.Marshal(active)

			if err != nil {
				log.Println("Unable to encode the orders, cutting the connection.")
				log.Println(err)
				return
			}
			
			err = wsutil.WriteServerMessage(conn, OP, kitchenorders)
			log.Println("Send initial cache");
			if err != nil {
				log.Println(err)
				return
			}

			// Loop while auth is valid
			for Claims.Expires > time.Now().Unix() {
				cLastUpdate := LastUpdate // Copying an int is atmoic
				if lUpdate != cLastUpdate {
					lUpdate = cLastUpdate
					var msg []byte

					// Enter critical region

					WriteLock.Wait()
					CounterLock.Lock()
					Readers++
					if Readers == 1 {
						WriteLock.Wait()
					}
					CounterLock.Unlock()

					s := SendUpdate
					copyCacheUpdates := CacheUpdates

					// Exit critical reigon after copy
					CounterLock.Lock()
					Readers--
					CounterLock.Unlock()

					if s {
						msg, err = json.Marshal(copyCacheUpdates)
						if err != nil {
							log.Println("Unable to encode cache updates.")
							log.Println(err)
							return
						}

						log.Println("Sending update")
						err := wsutil.WriteServerMessage(conn, OP, msg)
						if err != nil {
							log.Println(err)
							return // The connection dropped.
						}
					}
				}

				time.Sleep(time.Millisecond)
			}

			log.Println("Token expired")
		}()
	}))
}
