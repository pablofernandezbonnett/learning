package main

import (
	"context"
	"errors"
	"log/slog"
	"net/http"
	"strconv"
	"sync/atomic"
	"time"

	"github.com/gin-gonic/gin"
)

var requestSequence atomic.Uint64

func NewRouter(cfg Config, logger *slog.Logger, service *ReservationService) *gin.Engine {
	if cfg.AppEnv == "local" {
		gin.SetMode(gin.DebugMode)
	} else {
		gin.SetMode(gin.ReleaseMode)
	}

	router := gin.New()
	router.Use(gin.Recovery(), requestIDMiddleware(), accessLogMiddleware(logger))

	registerHealthRoute(router, cfg)
	registerReservationRoutes(router, cfg, service)

	return router
}

func registerHealthRoute(router *gin.Engine, cfg Config) {
	router.GET("/healthz", func(c *gin.Context) {
		c.JSON(http.StatusOK, gin.H{
			"status": "ok",
			"env":    cfg.AppEnv,
		})
	})
}

func registerReservationRoutes(router *gin.Engine, cfg Config, service *ReservationService) {
	api := router.Group("/api/v1")
	api.POST("/reservations", createReservationHandler(cfg, service))
	api.GET("/reservations/:id", getReservationHandler(service))
}

func requestIDMiddleware() gin.HandlerFunc {
	return func(c *gin.Context) {
		requestID := "req-" + strconv.FormatUint(requestSequence.Add(1), 10)
		c.Set("request_id", requestID)
		c.Writer.Header().Set("X-Request-ID", requestID)
		c.Next()
	}
}

func createReservationHandler(cfg Config, service *ReservationService) gin.HandlerFunc {
	return func(c *gin.Context) {
		var req CreateReservationRequest
		if err := c.ShouldBindJSON(&req); err != nil {
			c.JSON(http.StatusBadRequest, gin.H{"error": err.Error()})
			return
		}

		// The timeout is created right before the outbound dependency path.
		ctx, cancel := context.WithTimeout(c.Request.Context(), cfg.InventoryTimeout)
		defer cancel()

		reservation, idempotent, err := service.Reserve(ctx, req)
		if err != nil {
			respondReservationError(c, err)
			return
		}

		status := http.StatusCreated
		if idempotent {
			status = http.StatusOK
		}

		c.JSON(status, gin.H{
			"idempotent":  idempotent,
			"reservation": reservation,
		})
	}
}

func getReservationHandler(service *ReservationService) gin.HandlerFunc {
	return func(c *gin.Context) {
		reservation, err := service.GetReservation(c.Param("id"))
		if err != nil {
			c.JSON(http.StatusNotFound, gin.H{"error": "reservation not found"})
			return
		}
		c.JSON(http.StatusOK, gin.H{"reservation": reservation})
	}
}

func respondReservationError(c *gin.Context, err error) {
	switch {
	case errors.Is(err, ErrReservationConflict):
		c.JSON(http.StatusConflict, gin.H{"error": err.Error()})
	case errors.Is(err, ErrInventoryUnavailable):
		c.JSON(http.StatusConflict, gin.H{"error": "inventory unavailable"})
	case errors.Is(err, context.DeadlineExceeded):
		c.JSON(http.StatusGatewayTimeout, gin.H{"error": "inventory request timed out"})
	default:
		c.JSON(http.StatusBadGateway, gin.H{"error": err.Error()})
	}
}

func accessLogMiddleware(logger *slog.Logger) gin.HandlerFunc {
	return func(c *gin.Context) {
		start := time.Now()
		c.Next()

		logger.Info("http request",
			slog.String("request_id", requestIDFromContext(c)),
			slog.String("method", c.Request.Method),
			slog.String("path", c.FullPath()),
			slog.Int("status", c.Writer.Status()),
			slog.String("client_ip", c.ClientIP()),
			slog.Duration("latency", time.Since(start)),
		)
	}
}

func requestIDFromContext(c *gin.Context) string {
	value, ok := c.Get("request_id")
	if !ok {
		return "missing"
	}
	requestID, ok := value.(string)
	if !ok {
		return "invalid"
	}
	return requestID
}
