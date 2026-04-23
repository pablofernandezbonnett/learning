package main

import (
	"context"
	"errors"
	"log/slog"
	"net/http"
	"os"
	"os/signal"
	"syscall"
	"time"
)

func main() {
	cfg, err := LoadConfig()
	if err != nil {
		slog.Error("load config", slog.Any("error", err))
		os.Exit(1)
	}

	logger := slog.New(slog.NewTextHandler(os.Stdout, &slog.HandlerOptions{
		Level: slog.LevelInfo,
	}))

	store := NewInMemoryReservationStore()
	inventoryClient := NewInventoryClient(cfg)
	service := NewReservationService(store, inventoryClient)
	router := NewRouter(cfg, logger, service)

	server := &http.Server{
		Addr:              cfg.HTTPAddr,
		Handler:           router,
		ReadHeaderTimeout: 5 * time.Second,
	}

	// This turns SIGINT/SIGTERM into a context cancellation signal for shutdown.
	shutdownSignals, stop := signal.NotifyContext(context.Background(), syscall.SIGINT, syscall.SIGTERM)
	defer stop()

	go func() {
		logger.Info("starting server",
			slog.String("addr", cfg.HTTPAddr),
			slog.String("app_env", cfg.AppEnv),
			slog.String("inventory_mode", cfg.InventoryMode),
		)

		if err := server.ListenAndServe(); err != nil && !errors.Is(err, http.ErrServerClosed) {
			logger.Error("server stopped unexpectedly", slog.Any("error", err))
			stop()
		}
	}()

	<-shutdownSignals.Done()
	logger.Info("shutdown requested")

	// Graceful shutdown stops new traffic first, then waits for in-flight work.
	shutdownCtx, cancel := context.WithTimeout(context.Background(), cfg.ShutdownTimeout)
	defer cancel()

	if err := server.Shutdown(shutdownCtx); err != nil {
		logger.Error("graceful shutdown failed", slog.Any("error", err))
		os.Exit(1)
	}

	logger.Info("server stopped cleanly")
}
