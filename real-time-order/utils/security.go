package utils

import (
	"errors"
	"github.com/golang-jwt/jwt"
	"log"
	"strconv"
	"time"
)

/*has subject, type and, expires. See the api docs for help.*/
type connectedUserTmp struct {
	Type    string `json:"type"`
	Subject string `json:"subject"`
	Expires string `json:"expires"`
}

type ConnectedUser struct {
	Type    string
	Subject string
	Expires int64
}

func (c connectedUserTmp) Valid() error {
	if c.Subject == "" || c.Type == "" || c.Expires == "" {
		return errors.New("Bad claims")
	}

	return nil
}

func CheckJwt(JwtIn string, conf Config) (ConnectedUser, error) {
	claims := connectedUserTmp{}
	_, err := jwt.ParseWithClaims(
		JwtIn,
		&claims,
		func(token *jwt.Token) (interface{}, error) {
			return []byte(conf.JwtSecret), nil
		},
	)

	if err != nil {
		log.Printf("An error %s occurred when checking %s\n", err, JwtIn)
		return ConnectedUser{}, err
	}

	ret := ConnectedUser{Subject: claims.Subject,
		Type: claims.Type}
	ret.Expires, _ = strconv.ParseInt(claims.Expires, 10, 64)

	if ret.Expires <= time.Now().Unix() {
		log.Println(ret)
		log.Println("The jwt is expired")
		return ConnectedUser{}, errors.New("The jwt is expired")
	}

	if ret.Type != "access" {
		log.Println("The jwt is not an access token, this is forbidden")
		return ConnectedUser{}, errors.New("The jwt is not an access token. This is forbidden")
	}
	return ret, nil
}
