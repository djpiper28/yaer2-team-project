# API Errors
Whenever the api server has an error a json object is returned.

## Errors
| code | meaning |
|---|---|
| 400 | bad api request |
| 5xx | internal server error |

## Example Error

```json
{
  "error": "error message"
}
```