package main

import (
	"fmt"
	"sync"
	"time"
)

/**
 * Lab 2: Go Concurrency — Goroutines & Channels
 *
 * Logic: "Do not communicate by sharing memory; instead, share memory by communicating."
 * This is the CSP (Communicating Sequential Processes) model.
 */

func main() {
	// 1. Goroutines: Running functions in the background
	fmt.Println("=== 1. Goroutines ===")
	go func() {
		fmt.Println("Hello from a background Goroutine!")
	}()
	time.Sleep(100 * time.Millisecond) // Give it time to run

	// 2. Channels: Signaling between goroutines
	fmt.Println("\n=== 2. Channels (Synchronization) ===")
	stockUpdates := make(chan string)

	go func() {
		fmt.Println("Searching inventory...")
		time.Sleep(1 * time.Second)
		stockUpdates <- "Stock Found: 500 units" // Send data
	}()

	msg := <-stockUpdates // Receive data (BLOCKS until data arrives)
	fmt.Println(msg)

	// 3. Select: Multiplexing multiple channels
	fmt.Println("\n=== 3. Select (Timeout Pattern) ===")
	c1 := make(chan string)
	go func() {
		time.Sleep(2 * time.Second)
		c1 <- "Slow SAP Response"
	}()

	select {
	case res := <-c1:
		fmt.Println("Received:", res)
	case <-time.After(1 * time.Second):
		fmt.Println("TIMEOUT: SAP is too slow, returning cached value.")
	}

	// 4. WaitGroups: Waiting for multiple workers
	fmt.Println("\n=== 4. WaitGroups (Parallel Workers) ===")
	var wg sync.WaitGroup
	for i := 1; i <= 3; i++ {
		wg.Add(1)
		go func(id int) {
			defer wg.Done()
			fmt.Printf("Worker %d: Initializing robot...\n", id)
			time.Sleep(time.Duration(id) * time.Second)
		}(i)
	}
	wg.Wait() // Blocks until all workers call wg.Done()
	fmt.Println("All robots ready!")
}

/*
 * Practical note: When to use Mutex vs Channels?
 * - Channels: Orchestrating complexity, passing ownership of data, distribution of work.
 * - Mutex (sync.Mutex): Small critical sections, shared state inside a struct (like a cache map).
 *
 * Example: Use a Channel to stream orders from a web server to a warehouse processor.
 * Use a Mutex to protect a local 'Memory Cache' of product prices.
 */
