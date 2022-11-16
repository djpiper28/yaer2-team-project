# Api Server
The API server runs on port 8009 with HTTP by default but is hidden from WAN.

## Authentication and, Authorisation
A kerberos like authentication and, authorisation system.

### Authentication
#### Getting a Refresh Token
 - Client -> Server: [/api/getnonce](./api/getnonce.md)
 - Server -> Client: nonce
 - Client -> Server: [/api/login](./api/login.md) (sending the email, password and, nonce)
 - Server -> Client: refresh token

#### Using the Refresh Token
 - Client -> Server: [/api/getnonce](./api/getnonce.md)
 - Server -> Client: nonce
 - Client -> Server: [/api/refresh](./api/refresh.md) (sending the refresh token and, nonce)
 - Server -> Client: access token

---
### Authorisation
#### Using the Access Token
All privelleged enpoints require the access token to be sent, if the token has expired or is invalid then an error is returned and no action is taken.
#### Expired Access Tokens
Once the access token has expired use your refresh token to get a new one.

---
# Frontend Server
The frontend serer runs on port 8008 with HTTP by default but is hidden from WAN
