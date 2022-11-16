dot_env = """DB_URL=www.djpiper28.co.uk
DB_PORT=6445
DB_USERNAME=dev
DB_PASSWORD=REDACTED
DB_NAME=teamdev
BIND_ADDR=127.0.0.1
BIND_PORT=8010
JWT_SECRET=REDACTED
"""

f = open(".env", "w")
f.write(dot_env)
f.close()

f = open("utils/.env", "w")
f.write(dot_env)
f.close()
