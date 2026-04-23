package main

import (
	"context"
	"errors"
	"fmt"
	"strings"
	"sync"
	"time"
)

var ErrReservationConflict = errors.New("reservation conflict")
var ErrReservationNotFound = errors.New("reservation not found")

type CreateReservationRequest struct {
	ReservationID string `json:"reservation_id" binding:"required"`
	SKU           string `json:"sku" binding:"required"`
	Quantity      int    `json:"quantity" binding:"required,gte=1,lte=1000"`
}

type Reservation struct {
	ReservationID string    `json:"reservation_id"`
	SKU           string    `json:"sku"`
	Quantity      int       `json:"quantity"`
	State         string    `json:"state"`
	Warehouse     string    `json:"warehouse"`
	Remaining     int       `json:"remaining"`
	ReservedAt    time.Time `json:"reserved_at"`
}

type ReservationStore interface {
	Get(id string) (Reservation, bool)
	Save(reservation Reservation) error
}

type InMemoryReservationStore struct {
	mu    sync.RWMutex
	items map[string]Reservation
}

func NewInMemoryReservationStore() *InMemoryReservationStore {
	return &InMemoryReservationStore{
		items: make(map[string]Reservation),
	}
}

func (s *InMemoryReservationStore) Get(id string) (Reservation, bool) {
	s.mu.RLock()
	defer s.mu.RUnlock()
	reservation, ok := s.items[id]
	return reservation, ok
}

func (s *InMemoryReservationStore) Save(reservation Reservation) error {
	s.mu.Lock()
	defer s.mu.Unlock()
	s.items[reservation.ReservationID] = reservation
	return nil
}

type ReservationService struct {
	store     ReservationStore
	inventory InventoryClient
	now       func() time.Time
}

func NewReservationService(store ReservationStore, inventory InventoryClient) *ReservationService {
	return &ReservationService{
		store:     store,
		inventory: inventory,
		now:       time.Now,
	}
}

func (s *ReservationService) GetReservation(id string) (Reservation, error) {
	reservation, ok := s.store.Get(strings.TrimSpace(id))
	if !ok {
		return Reservation{}, ErrReservationNotFound
	}
	return reservation, nil
}

func (s *ReservationService) Reserve(ctx context.Context, req CreateReservationRequest) (Reservation, bool, error) {
	req.ReservationID = strings.TrimSpace(req.ReservationID)
	req.SKU = strings.ToUpper(strings.TrimSpace(req.SKU))

	// Idempotency belongs in the service rule, not in the HTTP layer.
	if existing, ok := s.store.Get(req.ReservationID); ok {
		if existing.SKU == req.SKU && existing.Quantity == req.Quantity {
			return existing, true, nil
		}
		return Reservation{}, false, fmt.Errorf("%w: reservation_id already used with different payload", ErrReservationConflict)
	}

	inventoryReservation, err := s.inventory.Reserve(ctx, req.SKU, req.Quantity)
	if err != nil {
		return Reservation{}, false, err
	}

	reservation := Reservation{
		ReservationID: req.ReservationID,
		SKU:           req.SKU,
		Quantity:      req.Quantity,
		State:         "reserved",
		Warehouse:     inventoryReservation.Warehouse,
		Remaining:     inventoryReservation.Remaining,
		ReservedAt:    s.now().UTC(),
	}

	if err := s.store.Save(reservation); err != nil {
		return Reservation{}, false, err
	}

	return reservation, false, nil
}
