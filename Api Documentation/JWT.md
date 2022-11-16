# JWTs
## JWT Payload Format
The payload contains the following

| Tag | Value Meaning
|---|---|
| `expires` | A unix time stamp (in seconds) of when the JWT expires |
| `subject` | The "subject" of the JWT, this is the UUID of the user |
| `type` | The type of the token, see the authorisation and, authentication diagram | 

```json
{
  "expires": "1674323995294",
  "subject": "c1b1e693-3558-4c83-b51c-daaa552bfadc",
  "type": "refresh"
}

```
