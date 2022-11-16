package utils

import (
	"fmt"
	"github.com/joho/godotenv"
	"log"
	"os"
	"strconv"
)

type Config struct {
	DbUrl      string
	DbPort     int
	DbUserName string
	DbPassword string
	DbName     string
	BindAddr   string
	BindPort   int
	JwtSecret  string
}

func PrintConfHelp() {
	fmt.Println("Create a .env file in the working directory which has the following defined:")
	fmt.Println("DB_URL, DB_PORT, DB_USERNAME, DB_PASSWORD, BIND_ADDR, BIND_PORT, JWT_SECRET")
	fmt.Println("See ../README.md for more help.")
}

func getEnvVar(EnvVar string) string {
	ret := os.Getenv(EnvVar)
	if ret == "" {
		PrintConfHelp()
		log.Fatalf("Error loading .env file: %s is undefined\n", EnvVar)
	}

	return ret
}

func getEnvVarInt(EnvVar string) int {
	tmp := getEnvVar(EnvVar)
	ret, err := strconv.Atoi(tmp)

	if err != nil {
		PrintConfHelp()
		log.Fatalf("Error loading .env file: %s must be an integer not a string\n", EnvVar)
	}

	return ret
}

func LoadConfig() Config {
	err := godotenv.Load()
	if err != nil {
		PrintConfHelp()
		log.Fatal("Error loading .env file.")
	}

	ret := Config{DbUrl: getEnvVar("DB_URL"),
		DbPort:     getEnvVarInt("DB_PORT"),
		DbUserName: getEnvVar("DB_USERNAME"),
		DbName:     getEnvVar("DB_NAME"),
		DbPassword: getEnvVar("DB_PASSWORD"),
		BindAddr:   getEnvVar("BIND_ADDR"),
		BindPort:   getEnvVarInt("BIND_PORT"),
		JwtSecret:  getEnvVar("JWT_SECRET")}
	log.Println("Loaded .env file")

	return ret
}
