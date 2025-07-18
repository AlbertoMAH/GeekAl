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

// Middleware CORS pour autoriser les requêtes depuis le navigateur
func corsMiddleware(next http.HandlerFunc) http.HandlerFunc {
	return func(w http.ResponseWriter, r *http.Request) {
		// Ajouter les headers CORS
		w.Header().Set("Access-Control-Allow-Origin", "*")
		w.Header().Set("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS")
		w.Header().Set("Access-Control-Allow-Headers", "Content-Type, Authorization, X-Requested-With")
		w.Header().Set("Access-Control-Expose-Headers", "Content-Length, Content-Type")
		w.Header().Set("Access-Control-Max-Age", "86400")
		
		// Gérer les requêtes OPTIONS (preflight)
		if r.Method == "OPTIONS" {
			w.WriteHeader(http.StatusOK)
			return
		}
		
		// Appeler le handler suivant
		next.ServeHTTP(w, r)
	}
}

func main() {
	// Appliquer le middleware CORS à toutes les routes
	http.HandleFunc("/api/position", corsMiddleware(handlePosition))
	http.HandleFunc("/api/position/", corsMiddleware(handleGetPosition))
	http.HandleFunc("/api/stopSharing", corsMiddleware(handleStopSharing))
	
	// Route pour la racine (optionnel)
	http.HandleFunc("/", corsMiddleware(handleHome))

	// Utiliser la variable d'environnement PORT si disponible
	port := os.Getenv("PORT")
	if port == "" {
		port = "10000" // fallback en local
	}

	fmt.Println("Serveur Go en écoute sur le port", port)
	fmt.Println("API disponible sur: http://localhost:" + port + "/api/position/{busNumber}")
	http.ListenAndServe(":"+port, nil)
}

// Handler pour la racine
func handleHome(w http.ResponseWriter, r *http.Request) {
	w.Header().Set("Content-Type", "application/json")
	response := map[string]interface{}{
		"message": "API de suivi de bus en temps réel",
		"version": "1.0",
		"status":  "active",
		"endpoints": map[string]string{
			"get_position":  "GET /api/position/{busNumber}",
			"post_position": "POST /api/position",
			"stop_sharing":  "POST /api/stopSharing",
		},
	}
	json.NewEncoder(w).Encode(response)
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
	
	fmt.Printf("Position enregistrée pour le bus %s: lat=%f, lng=%f\n", req.BusNumber, req.Latitude, req.Longitude)
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
	
	fmt.Printf("Position demandée pour le bus %s: lat=%f, lng=%f\n", busNumber, position.Latitude, position.Longitude)
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
		fmt.Printf("Partage arrêté pour le bus %s\n", req.BusNumber)
	} else {
		http.Error(w, `{"error":"Bus introuvable"}`, http.StatusNotFound)
	}
}
