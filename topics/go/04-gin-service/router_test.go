package main

import (
	"bytes"
	"encoding/json"
	"io"
	"log/slog"
	"net/http"
	"net/http/httptest"
	"sync/atomic"
	"testing"
	"time"
)

func TestCreateReservationSuccessAndFetch(t *testing.T) {
	var upstreamCalls atomic.Int64
	upstream := httptest.NewServer(http.HandlerFunc(func(w http.ResponseWriter, r *http.Request) {
		upstreamCalls.Add(1)
		if r.Method != http.MethodPost || r.URL.Path != "/inventory/reservations" {
			t.Fatalf("unexpected upstream request: %s %s", r.Method, r.URL.Path)
		}
		writeJSONResponse(t, w, http.StatusCreated, InventoryReservation{
			Warehouse: "tokyo-a",
			Remaining: 7,
		})
	}))
	defer upstream.Close()

	router := testRouter(t, Config{
		AppEnv:           "test",
		InventoryMode:    "http",
		InventoryBaseURL: upstream.URL,
		InventoryTimeout: 200 * time.Millisecond,
	}, nil)

	createBody := map[string]any{
		"reservation_id": "res-1",
		"sku":            "ut-white-m",
		"quantity":       2,
	}
	rec := performJSONRequest(t, router, http.MethodPost, "/api/v1/reservations", createBody)
	if rec.Code != http.StatusCreated {
		t.Fatalf("expected 201, got %d: %s", rec.Code, rec.Body.String())
	}
	if upstreamCalls.Load() != 1 {
		t.Fatalf("expected 1 upstream call, got %d", upstreamCalls.Load())
	}

	fetch := performJSONRequest(t, router, http.MethodGet, "/api/v1/reservations/res-1", nil)
	if fetch.Code != http.StatusOK {
		t.Fatalf("expected 200, got %d: %s", fetch.Code, fetch.Body.String())
	}
}

func TestCreateReservationIdempotentDuplicate(t *testing.T) {
	var upstreamCalls atomic.Int64
	upstream := httptest.NewServer(http.HandlerFunc(func(w http.ResponseWriter, r *http.Request) {
		upstreamCalls.Add(1)
		writeJSONResponse(t, w, http.StatusCreated, InventoryReservation{
			Warehouse: "osaka-b",
			Remaining: 5,
		})
	}))
	defer upstream.Close()

	router := testRouter(t, Config{
		AppEnv:           "test",
		InventoryMode:    "http",
		InventoryBaseURL: upstream.URL,
		InventoryTimeout: 200 * time.Millisecond,
	}, nil)

	body := map[string]any{
		"reservation_id": "res-2",
		"sku":            "UT-BLACK-L",
		"quantity":       1,
	}

	first := performJSONRequest(t, router, http.MethodPost, "/api/v1/reservations", body)
	if first.Code != http.StatusCreated {
		t.Fatalf("expected first request to return 201, got %d", first.Code)
	}

	second := performJSONRequest(t, router, http.MethodPost, "/api/v1/reservations", body)
	if second.Code != http.StatusOK {
		t.Fatalf("expected duplicate request to return 200, got %d: %s", second.Code, second.Body.String())
	}
	if upstreamCalls.Load() != 1 {
		t.Fatalf("expected duplicate request not to call upstream twice, got %d", upstreamCalls.Load())
	}
}

func TestCreateReservationConflictOnDifferentPayload(t *testing.T) {
	upstream := httptest.NewServer(http.HandlerFunc(func(w http.ResponseWriter, r *http.Request) {
		writeJSONResponse(t, w, http.StatusCreated, InventoryReservation{
			Warehouse: "nagoya-c",
			Remaining: 9,
		})
	}))
	defer upstream.Close()

	router := testRouter(t, Config{
		AppEnv:           "test",
		InventoryMode:    "http",
		InventoryBaseURL: upstream.URL,
		InventoryTimeout: 200 * time.Millisecond,
	}, nil)

	first := map[string]any{
		"reservation_id": "res-3",
		"sku":            "UT-WHITE-M",
		"quantity":       1,
	}
	second := map[string]any{
		"reservation_id": "res-3",
		"sku":            "UT-WHITE-M",
		"quantity":       3,
	}

	if rec := performJSONRequest(t, router, http.MethodPost, "/api/v1/reservations", first); rec.Code != http.StatusCreated {
		t.Fatalf("expected 201, got %d", rec.Code)
	}
	if rec := performJSONRequest(t, router, http.MethodPost, "/api/v1/reservations", second); rec.Code != http.StatusConflict {
		t.Fatalf("expected 409, got %d: %s", rec.Code, rec.Body.String())
	}
}

func TestCreateReservationTimeout(t *testing.T) {
	upstream := httptest.NewServer(http.HandlerFunc(func(w http.ResponseWriter, r *http.Request) {
		time.Sleep(80 * time.Millisecond)
		writeJSONResponse(t, w, http.StatusCreated, InventoryReservation{
			Warehouse: "slow-zone",
			Remaining: 1,
		})
	}))
	defer upstream.Close()

	router := testRouter(t, Config{
		AppEnv:           "test",
		InventoryMode:    "http",
		InventoryBaseURL: upstream.URL,
		InventoryTimeout: 20 * time.Millisecond,
	}, nil)

	body := map[string]any{
		"reservation_id": "res-timeout",
		"sku":            "UT-WHITE-M",
		"quantity":       1,
	}

	rec := performJSONRequest(t, router, http.MethodPost, "/api/v1/reservations", body)
	if rec.Code != http.StatusGatewayTimeout {
		t.Fatalf("expected 504, got %d: %s", rec.Code, rec.Body.String())
	}
}

func TestValidationError(t *testing.T) {
	router := testRouter(t, Config{
		AppEnv:           "test",
		InventoryMode:    "stub",
		InventoryTimeout: 200 * time.Millisecond,
	}, NewStubInventoryClient())

	body := map[string]any{
		"reservation_id": "res-invalid",
		"sku":            "UT-WHITE-M",
		"quantity":       0,
	}

	rec := performJSONRequest(t, router, http.MethodPost, "/api/v1/reservations", body)
	if rec.Code != http.StatusBadRequest {
		t.Fatalf("expected 400, got %d: %s", rec.Code, rec.Body.String())
	}
}

func testRouter(t *testing.T, cfg Config, inventory InventoryClient) http.Handler {
	t.Helper()
	if inventory == nil {
		inventory = NewInventoryClient(cfg)
	}

	logger := slog.New(slog.NewTextHandler(io.Discard, nil))
	store := NewInMemoryReservationStore()
	service := NewReservationService(store, inventory)
	return NewRouter(cfg, logger, service)
}

func performJSONRequest(t *testing.T, handler http.Handler, method string, path string, body any) *httptest.ResponseRecorder {
	t.Helper()

	var reader *bytes.Reader
	if body == nil {
		reader = bytes.NewReader(nil)
	} else {
		raw, err := json.Marshal(body)
		if err != nil {
			t.Fatalf("marshal body: %v", err)
		}
		reader = bytes.NewReader(raw)
	}

	req := httptest.NewRequest(method, path, reader)
	req.Header.Set("Content-Type", "application/json")
	rec := httptest.NewRecorder()
	handler.ServeHTTP(rec, req)
	return rec
}

func writeJSONResponse(t *testing.T, w http.ResponseWriter, status int, body any) {
	t.Helper()
	w.Header().Set("Content-Type", "application/json")
	w.WriteHeader(status)
	if err := json.NewEncoder(w).Encode(body); err != nil {
		t.Fatalf("encode response: %v", err)
	}
}
