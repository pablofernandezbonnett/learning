package main

import (
	"bytes"
	"encoding/json"
	"fmt"
	"net/http"
	"net/http/httptest"
	"strings"
)

// Lab 3: HTTP + JSON in Go
//
// Mental bridge for Java/Kotlin backend engineers:
// - net/http gives you the standard server pieces directly.
// - You decode JSON explicitly instead of relying on framework magic.
// - Validation and error responses stay local and obvious.

type Product struct {
	ID       string `json:"id"`
	SKU      string `json:"sku"`
	Name     string `json:"name"`
	PriceJPY int    `json:"price_jpy"`
	Active   bool   `json:"active"`
}

type ProductCreate struct {
	SKU      string `json:"sku"`
	Name     string `json:"name"`
	PriceJPY int    `json:"price_jpy"`
}

type errorResponse struct {
	Error string `json:"error"`
}

type productStore struct {
	byID map[string]Product
}

func newProductStore() *productStore {
	return &productStore{
		byID: map[string]Product{
			"p-1": {
				ID:       "p-1",
				SKU:      "UT-WHITE-M",
				Name:     "UT Crew Neck T-Shirt",
				PriceJPY: 2990,
				Active:   true,
			},
		},
	}
}

func (s *productStore) listProducts(w http.ResponseWriter, r *http.Request) {
	products := make([]Product, 0, len(s.byID))
	for _, product := range s.byID {
		products = append(products, product)
	}
	writeJSON(w, http.StatusOK, products)
}

func (s *productStore) createProduct(w http.ResponseWriter, r *http.Request) {
	var body ProductCreate
	if err := json.NewDecoder(r.Body).Decode(&body); err != nil {
		writeJSON(w, http.StatusBadRequest, errorResponse{Error: "invalid JSON body"})
		return
	}

	body.SKU = strings.TrimSpace(strings.ToUpper(body.SKU))
	body.Name = strings.TrimSpace(body.Name)

	if body.SKU == "" || body.Name == "" || body.PriceJPY <= 0 {
		writeJSON(w, http.StatusBadRequest, errorResponse{Error: "sku, name, and positive price_jpy are required"})
		return
	}

	for _, product := range s.byID {
		if product.SKU == body.SKU {
			writeJSON(w, http.StatusConflict, errorResponse{Error: "duplicate SKU"})
			return
		}
	}

	id := fmt.Sprintf("p-%d", len(s.byID)+1)
	created := Product{
		ID:       id,
		SKU:      body.SKU,
		Name:     body.Name,
		PriceJPY: body.PriceJPY,
		Active:   true,
	}
	s.byID[id] = created
	writeJSON(w, http.StatusCreated, created)
}

func writeJSON(w http.ResponseWriter, status int, body any) {
	w.Header().Set("Content-Type", "application/json")
	w.WriteHeader(status)
	if err := json.NewEncoder(w).Encode(body); err != nil {
		http.Error(w, `{"error":"failed to encode JSON"}`, http.StatusInternalServerError)
	}
}

func exercise(handler http.Handler, method string, path string, body any) {
	var payload *bytes.Reader
	if body == nil {
		payload = bytes.NewReader(nil)
	} else {
		raw, err := json.Marshal(body)
		if err != nil {
			panic(err)
		}
		payload = bytes.NewReader(raw)
	}

	req := httptest.NewRequest(method, path, payload)
	req.Header.Set("Content-Type", "application/json")
	rec := httptest.NewRecorder()
	handler.ServeHTTP(rec, req)

	fmt.Printf("%s %s -> %d\n", method, path, rec.Code)
	fmt.Println(strings.TrimSpace(rec.Body.String()))
	fmt.Println()
}

func main() {
	store := newProductStore()

	mux := http.NewServeMux()
	mux.HandleFunc("/products", func(w http.ResponseWriter, r *http.Request) {
		switch r.Method {
		case http.MethodGet:
			store.listProducts(w, r)
		case http.MethodPost:
			store.createProduct(w, r)
		default:
			writeJSON(w, http.StatusMethodNotAllowed, errorResponse{Error: "method not allowed"})
		}
	})

	fmt.Println("=== 1. List products ===")
	exercise(mux, http.MethodGet, "/products", nil)

	fmt.Println("=== 2. Create product ===")
	exercise(mux, http.MethodPost, "/products", ProductCreate{
		SKU:      "fleece-grey-m",
		Name:     "Fleece Jacket",
		PriceJPY: 4990,
	})

	fmt.Println("=== 3. Duplicate SKU ===")
	exercise(mux, http.MethodPost, "/products", ProductCreate{
		SKU:      "UT-WHITE-M",
		Name:     "Duplicate Tee",
		PriceJPY: 2990,
	})
}
