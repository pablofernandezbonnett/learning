package main

import (
	"fmt"
	"os"
	"strconv"
	"strings"
	"time"
)

type Config struct {
	AppEnv           string
	HTTPAddr         string
	InventoryMode    string
	InventoryBaseURL string
	InventoryTimeout time.Duration
	ShutdownTimeout  time.Duration
}

func LoadConfig() (Config, error) {
	cfg := Config{
		AppEnv:           getenv("APP_ENV", "local"),
		HTTPAddr:         getenv("HTTP_ADDR", ":8080"),
		InventoryMode:    strings.ToLower(getenv("INVENTORY_MODE", "stub")),
		InventoryBaseURL: strings.TrimRight(os.Getenv("INVENTORY_BASE_URL"), "/"),
	}

	inventoryTimeoutMs, err := getenvInt("INVENTORY_TIMEOUT_MS", 800)
	if err != nil {
		return Config{}, fmt.Errorf("parse INVENTORY_TIMEOUT_MS: %w", err)
	}
	shutdownTimeoutMs, err := getenvInt("SHUTDOWN_TIMEOUT_MS", 5000)
	if err != nil {
		return Config{}, fmt.Errorf("parse SHUTDOWN_TIMEOUT_MS: %w", err)
	}

	cfg.InventoryTimeout = time.Duration(inventoryTimeoutMs) * time.Millisecond
	cfg.ShutdownTimeout = time.Duration(shutdownTimeoutMs) * time.Millisecond

	switch cfg.InventoryMode {
	case "stub":
	case "http":
		if cfg.InventoryBaseURL == "" {
			return Config{}, fmt.Errorf("INVENTORY_BASE_URL is required when INVENTORY_MODE=http")
		}
	default:
		return Config{}, fmt.Errorf("INVENTORY_MODE must be stub or http")
	}

	return cfg, nil
}

func getenv(key string, fallback string) string {
	if value, ok := os.LookupEnv(key); ok && strings.TrimSpace(value) != "" {
		return value
	}
	return fallback
}

func getenvInt(key string, fallback int) (int, error) {
	raw := getenv(key, strconv.Itoa(fallback))
	value, err := strconv.Atoi(raw)
	if err != nil {
		return 0, err
	}
	if value <= 0 {
		return 0, fmt.Errorf("must be positive")
	}
	return value, nil
}
