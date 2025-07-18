package main

import (
	"encoding/json"
	"fmt"
	"net/http"
	"os"
	"sync"
	"time"
)

type Position struct {
	Latitude  float64 `json:"latitude"`
	Longitude float64 `json:"longitude"`
	Timestamp int64   `json:"timestamp"`
}

type PositionRequest struct {
	BusNumber string  `json:"busNumber"`
	Latitude  float64 `json:"latitude"`
	Longitude float64 `json:"longitude"`
}

type StopSharingRequest struct {
	BusNumber string `json:"busNumber"`
}

var (
	busPositions = make(map[string]Position)
	mutex        = &sync.Mutex{}
)

func main() {
	http.HandleFunc("/api/position", handlePosition)
	http.HandleFunc("/api/position/", handleGetPosition)
	http.HandleFunc("/api/stopSharing", handleStopSharing)

	// Utiliser la variable d’environnement PORT si disponible
	port := os.Getenv("PORT")
	if port == "" {
		port = "10000" // fallback en local
	}

	fmt.Println("Serveur Go en écoute sur le port", port)
	http.ListenAndServe(":"+port, nil)
}

func handlePosition(w http.ResponseWriter, r *http.Request) {
	if r.Method != http.MethodPost {
		http.Error(w, `{"error":"Méthode non autorisée"}`, http.StatusMethodNotAllowed)
		return
	}

	var req PositionRequest
	if err := json.NewDecoder(r.Body).Decode(&req); err != nil || req.BusNumber == "" {
		http.Error(w, `{"error":"Données manquantes"}`, http.StatusBadRequest)
		return
	}

	mutex.Lock()
	busPositions[req.BusNumber] = Position{
		Latitude:  req.Latitude,
		Longitude: req.Longitude,
		Timestamp: time.Now().UnixMilli(),
	}
	mutex.Unlock()

	w.Header().Set("Content-Type", "application/json")
	json.NewEncoder(w).Encode(map[string]string{"message": "Position enregistrée"})
}

func handleGetPosition(w http.ResponseWriter, r *http.Request) {
	if r.Method != http.MethodGet {
		http.Error(w, `{"error":"Méthode non autorisée"}`, http.StatusMethodNotAllowed)
		return
	}

	busNumber := r.URL.Path[len("/api/position/"):]
	if busNumber == "" {
		http.Error(w, `{"error":"Bus manquant"}`, http.StatusBadRequest)
		return
	}

	mutex.Lock()
	position, exists := busPositions[busNumber]
	mutex.Unlock()

	if !exists {
		http.Error(w, `{"error":"Bus introuvable"}`, http.StatusNotFound)
		return
	}

	w.Header().Set("Content-Type", "application/json")
	json.NewEncoder(w).Encode(position)
}

func handleStopSharing(w http.ResponseWriter, r *http.Request) {
	if r.Method != http.MethodPost {
		http.Error(w, `{"error":"Méthode non autorisée"}`, http.StatusMethodNotAllowed)
		return
	}

	var req StopSharingRequest
	if err := json.NewDecoder(r.Body).Decode(&req); err != nil || req.BusNumber == "" {
		http.Error(w, `{"error":"Numéro de bus manquant"}`, http.StatusBadRequest)
		return
	}

	mutex.Lock()
	_, exists := busPositions[req.BusNumber]
	if exists {
		delete(busPositions, req.BusNumber)
	}
	mutex.Unlock()

	if exists {
		w.Header().Set("Content-Type", "application/json")
		json.NewEncoder(w).Encode(map[string]string{"message": "Partage arrêté pour le bus " + req.BusNumber})
	} else {
		http.Error(w, `{"error":"Bus introuvable"}`, http.StatusNotFound)
	}
}
