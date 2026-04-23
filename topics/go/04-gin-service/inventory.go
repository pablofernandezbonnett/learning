package main

import (
	"bytes"
	"context"
	"encoding/json"
	"errors"
	"fmt"
	"net/http"
	"strings"
)

var ErrInventoryUnavailable = errors.New("inventory unavailable")

type InventoryReservation struct {
	Warehouse string `json:"warehouse"`
	Remaining int    `json:"remaining"`
}

type InventoryClient interface {
	Reserve(ctx context.Context, sku string, quantity int) (InventoryReservation, error)
}

type StubInventoryClient struct {
	stockBySKU map[string]int
}

func NewStubInventoryClient() *StubInventoryClient {
	return &StubInventoryClient{
		stockBySKU: map[string]int{
			"UT-WHITE-M":    12,
			"UT-BLACK-L":    8,
			"FLEECE-GREY-M": 4,
		},
	}
}

func (c *StubInventoryClient) Reserve(_ context.Context, sku string, quantity int) (InventoryReservation, error) {
	available, ok := c.stockBySKU[sku]
	if !ok || available < quantity {
		return InventoryReservation{}, ErrInventoryUnavailable
	}

	c.stockBySKU[sku] = available - quantity
	return InventoryReservation{
		Warehouse: "stub-warehouse",
		Remaining: c.stockBySKU[sku],
	}, nil
}

type HTTPInventoryClient struct {
	BaseURL string
	Client  *http.Client
}

type reserveInventoryRequest struct {
	SKU      string `json:"sku"`
	Quantity int    `json:"quantity"`
}

func (c *HTTPInventoryClient) Reserve(ctx context.Context, sku string, quantity int) (InventoryReservation, error) {
	payload, err := json.Marshal(reserveInventoryRequest{
		SKU:      sku,
		Quantity: quantity,
	})
	if err != nil {
		return InventoryReservation{}, fmt.Errorf("marshal inventory request: %w", err)
	}

	req, err := http.NewRequestWithContext(ctx, http.MethodPost, c.BaseURL+"/inventory/reservations", bytes.NewReader(payload))
	if err != nil {
		return InventoryReservation{}, fmt.Errorf("build inventory request: %w", err)
	}
	req.Header.Set("Content-Type", "application/json")

	// The request-scoped context carries timeout and cancellation from the handler.
	resp, err := c.Client.Do(req)
	if err != nil {
		return InventoryReservation{}, err
	}
	defer resp.Body.Close()

	switch resp.StatusCode {
	case http.StatusCreated, http.StatusOK:
		var reservation InventoryReservation
		if err := json.NewDecoder(resp.Body).Decode(&reservation); err != nil {
			return InventoryReservation{}, fmt.Errorf("decode inventory response: %w", err)
		}
		return reservation, nil
	case http.StatusConflict:
		return InventoryReservation{}, ErrInventoryUnavailable
	default:
		var body map[string]any
		_ = json.NewDecoder(resp.Body).Decode(&body)
		return InventoryReservation{}, fmt.Errorf("inventory service returned %d: %v", resp.StatusCode, body)
	}
}

func NewInventoryClient(cfg Config) InventoryClient {
	if cfg.InventoryMode == "http" {
		return &HTTPInventoryClient{
			BaseURL: strings.TrimRight(cfg.InventoryBaseURL, "/"),
			Client:  &http.Client{},
		}
	}
	return NewStubInventoryClient()
}
