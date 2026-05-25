package main

import (
	"errors"
	"fmt"
)

/**
 * Lab 1: Go Basics for Java/Kotlin Developers
 *
 * Key Differences:
 * - No classes, only Structs + Methods.
 * - Error handling is explicit (no exceptions).
 * - Pointers exist but are safe (no pointer arithmetic).
 *
 * Backend note:
 * This file is only the syntax warmup.
 * The real backend learning starts when the later examples add HTTP, JSON,
 * context propagation, package shape, and explicit boundary design.
 */

// 1. Structs (Like Data Classes)
type Product struct {
	ID    string
	Name  string
	Price int
}

// 2. Methods (Defined outside the struct)
func (p Product) String() string {
	return fmt.Sprintf("%s (¥%d)", p.Name, p.Price)
}

// 3. Functions with multiple returns (The Go Standard)
func divideStock(total int, boxes int) (int, error) {
	if boxes == 0 {
		return 0, errors.New("cannot divide by zero boxes")
	}
	return total / boxes, nil
}

func main() {
	fmt.Println("=== 1. Variables & Loops ===")
	// Typed but inferred
	company := "Acme Commerce"
	fmt.Printf("Welcome to %s\n", company)

	// Loops: Only 'for' exists in Go
	for i := 0; i < 3; i++ {
		fmt.Printf("Scanning... %d\n", i)
	}

	// Range-based loop (Iterables)
	brands := []string{"Acme Store", "North Hub", "Outlet"}
	for index, name := range brands {
		fmt.Printf("Brand %d: %s\n", index, name)
	}

	fmt.Println("\n=== 2. Structs & Methods ===")
	shirt := Product{ID: "U001", Name: "Heattech", Price: 1500}
	fmt.Println(shirt.String())

	fmt.Println("\n=== 3. Error Handling ===")
	items, err := divideStock(100, 4)
	if err != nil {
		fmt.Println("Error:", err)
	} else {
		fmt.Printf("Items per box: %d\n", items)
	}

	// This explicit "value or error" flow is one of the biggest backend
	// differences from Java/Kotlin. Later examples build HTTP and service
	// boundaries around this style instead of exceptions.

	// 4. Pointers (The '*' and '&')
	// Used to modify an object without copying it
	p := &shirt
	p.Price = 1200 // Modifies original shirt
	fmt.Printf("Discounted price: ¥%d\n", shirt.Price)
}
