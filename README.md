# CS2810 Group 5 Project
THIS REPO CONTAINS A CLONE OF MY GROUP PROJECT, TEAM MEMBERS ARE:
Danny Piper, John Costa, Flynn, Nick, Tom, Lim, Dhara and, Sachal

I have forgotten some (all) surnames sorry :(

---


| Element              | Status                                                                                                                                                        |
| -------------------- | ------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| Backend status       | [![Build Status](https://jenkins.djpiper28.co.uk/buildStatus/icon?job=teamproject5scm%2Fmain)](https://jenkins.djpiper28.co.uk/job/teamproject5scm/job/main/) |
| Frontend status      | [![Build Status](https://jenkins.djpiper28.co.uk/buildStatus/icon?job=Frontend+Ci%2Fmain)](https://jenkins.djpiper28.co.uk/job/Frontend%20Ci/job/main/)       |
| Admin Console status | [![Build Status](https://jenkins.djpiper28.co.uk/buildStatus/icon?job=AdminConsoleCI%2Fmain)](https://jenkins.djpiper28.co.uk/job/AdminConsoleCI/job/main/)   |
| Real Time Order Service | [![Build Status](https://jenkins.djpiper28.co.uk/buildStatus/icon?job=Real+Time+Order+Service%2Fmain)](https://jenkins.djpiper28.co.uk/job/Real%20Time%20Order%20Service/job/main/) |

## Useful Links

- [Jenkins CI Server](https://jenkins.djpiper28.co.uk) \*
- [Javadocs](https://jenkins.djpiper28.co.uk/job/teamproject5scm/job/main/javadoc/) \*
- [API Docs](./Api%20Documentation)

\* See Discord for login.

---

# A restaurant management system.

## Backend API Server
(Authentication and the RESTful API for order placement, management and, menu queries.

### Setup

#### Environment Variables

Put in a dotenv file under java-backend/app/.env

init.

| Environment Variable | Usage                                                                           |
| -------------------- | ------------------------------------------------------------------------------- |
| `BIND_ADDR`          | The address that the server binds to                                            |
| `BIND_PORT`          | The port that the api server binds to                                           |
| `DATABASE_URL`       | The database url (in form `jdbc:postgresql://<hostname>:<port>/<databasenbame>` |
| `DATABASE_USENAME`   | The usename for the database                                                    |
| `DATABASE_PASSWORD`  | The password for the database                                                   |
| `JWT_SECRET`         | The JWT secret key                                                              |

#### Requirements

| Requirement                 | Reason                  |
| --------------------------- | ----------------------- |
| python3, pip3 and, pyscop3  | to run the test scripts |
| java jdk                    | compiling               |
| gradle (bundled as gradlew) | the build manager       |

### Start the Server

```bash
# Firstly, make sure you are in the java-backend folder
gradle run
```

This will create an api server on: http://localhost:8009

### Making a Fat Jar

```bash
# Firstly, make sure you are in the java-backend folder
gradle shadowjar
```

---

## Real Time Order Service
A service to provide real time order updates to the kitchen over a websocket connection.

### Setup

#### Environment Variables

Put in a dotenv file under real-timer-order/.env

| Environment Variable | Usage                                                                           |
| -------------------- | ------------------------------------------------------------------------------- |
| `BIND_ADDR`          | The address that the server binds to                                            |
| `BIND_PORT`          | The port that the api server binds to                                           |
| `JWT_SECRET`         | The JWT secret key                                                              |
| `DB_PORT` | The database port |
| `DB_URL` | The database url |
| `DB_NAME` | The database name (`teamdev`) |
| `DB_USERNAME` | The database username |
| `DB_PASSWORD` | The database password |

#### Requirements

| Requirement                 | Reason                  |
| --------------------------- | ----------------------- |
| go | The programming language bundled compiler and, tool chain|

This will create a server (default) https://localhost:8010

### Start The Server
```bash
# Firstly, make sure you are in the real-time-order folder
go build && ./real-time-order
```

---

## Frontend

### Requirements

You'll need two environment variable to connect the frontend to the database. Make a .env file and place it inside the frontend folter.

| Environment Variable | Example                   | Usage                                                    |
| -------------------- | ------------------------- | -------------------------------------------------------- |
| BANKEND_URL          | https://www.rhul-hack.com | Connects to the main backend url, used for static files. |
| BACKEND_API          | http://localhost:8009/api | Connects to the backend api url.                         |

To install all the modules for the frontend project simple run the following commands from a terminal (Please ask John if you need any help on this).

```bash
# Firstly, make sure you are in the frontend folder.
npm install
npm run start
```

This will create a development server on: http://localhost:8008/

---

## Admin Console
Managers need to be able to edit the menu, inventory and, analyse orders that have been placed.

### Building (on unix)

To compile the admin console envter the `admin-console` folder then run the following commands to make
cmake compile an executable.

```bash
# cd admin-console
mkdir -p build
cd build
cmake .. && cmake --build . -j
```

An executable file called `admin-console` is now made. 

## Tests

To execute tests execute `admin-console` with the test argument (`./admin-console test`).

### Requirements

| Requirement | Usage                   |
| ----------- | ----------------------- |
| libpqxx     | Postgresql C/C++ Client |
| qt5         | GUI Framework           |
| cmake       | Build System            |
| g++         | Compiler                |
| openssl     | Provides libcrypto for hashing |

### Configuration

The configuration tab allows for easy configuration of the database settings. If you need to return to the default settings delete admin-console-configuration.json.
