package utils

import (
	"errors"
	"fmt"
	"github.com/golang-jwt/jwt"
	"testing"
	"time"
)

type TestJwt struct {
	Type    string `json:"type"`
	Subject string `json:"subject"`
	Expires string `json:"expires"`
}

func (c TestJwt) Valid() error {
	if c.Subject == "" || c.Type == "" || c.Expires == "" {
		return errors.New("Bad claims")
	}

	return nil
}
func TestCheckJwt(t *testing.T) {
	conf := LoadConfig()

	// Create a test JWT
	// HS512 is the standard we use.
	claims := TestJwt{Type: "access",
		Subject: "i am a user id, I promise you",
		Expires: fmt.Sprintf("%d", time.Now().Unix()+100)}
	token := jwt.NewWithClaims(jwt.SigningMethodHS512, claims)
	signedToken, err := token.SignedString([]byte(conf.JwtSecret))

	t.Log(time.Now().Unix())
	t.Log(claims)

	if err != nil {
		t.Log("Cannot sign jwt")
		t.Fail()
	}

	_, err = CheckJwt(signedToken, conf)
	if err != nil {
		t.Log("Cannot verify known good jwt")
		t.Fail()
	}
}
