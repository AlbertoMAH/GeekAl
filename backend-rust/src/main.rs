use axum::{
    extract::{Path, State},
    http::{Method, StatusCode},
    response::IntoResponse,
    routing::{get, post},
    Json, Router,
};
use serde::{Deserialize, Serialize};
use std::{
    collections::HashMap,
    env,
    net::SocketAddr,
    sync::{Arc, Mutex},
};
use tower_http::cors::{Any, CorsLayer};
use chrono::Utc;

#[derive(Debug, Serialize, Deserialize, Clone)]
struct Position {
    latitude: f64,
    longitude: f64,
    timestamp: i64,
}

#[derive(Debug, Deserialize)]
struct PositionRequest {
    bus_number: String,
    latitude: f64,
    longitude: f64,
}

#[derive(Debug, Deserialize)]
struct StopSharingRequest {
    bus_number: String,
}

type SharedState = Arc<Mutex<HashMap<String, Position>>>;

#[tokio::main]
async fn main() {
    dotenvy::dotenv().ok();
    let port = env::var("PORT").unwrap_or_else(|_| "10000".into());
    let addr = format!("0.0.0.0:{}", port).parse::<SocketAddr>().unwrap();

    let state: SharedState = Arc::new(Mutex::new(HashMap::new()));

    let cors = CorsLayer::new()
        .allow_origin(Any)
        .allow_methods([Method::GET, Method::POST, Method::OPTIONS])
        .allow_headers(Any);

    let app = Router::new()
        .route("/", get(home))
        .route("/api/position", post(save_position))
        .route("/api/position/:bus_number", get(get_position))
        .route("/api/stopSharing", post(stop_sharing))
        .with_state(state)
        .layer(cors);

    println!("Serveur Rust en écoute sur http://{}", addr);
    axum::Server::bind(&addr)
        .serve(app.into_make_service())
        .await
        .unwrap();
}

async fn home() -> impl IntoResponse {
    let response = serde_json::json!({
        "message": "API de suivi de bus en temps réel",
        "version": "1.0",
        "status": "active",
        "endpoints": {
            "get_position": "GET /api/position/{bus_number}",
            "post_position": "POST /api/position",
            "stop_sharing": "POST /api/stopSharing"
        }
    });
    (StatusCode::OK, Json(response))
}

async fn save_position(
    State(state): State<SharedState>,
    Json(payload): Json<PositionRequest>,
) -> impl IntoResponse {
    let mut map = state.lock().unwrap();

    let position = Position {
        latitude: payload.latitude,
        longitude: payload.longitude,
        timestamp: Utc::now().timestamp_millis(),
    };

    map.insert(payload.bus_number.clone(), position);

    println!(
        "Position enregistrée pour le bus {}",
        payload.bus_number
    );

    (StatusCode::OK, Json(serde_json::json!({"message": "Position enregistrée"})))
}

async fn get_position(
    Path(bus_number): Path<String>,
    State(state): State<SharedState>,
) -> impl IntoResponse {
    let map = state.lock().unwrap();

    if let Some(position) = map.get(&bus_number) {
        println!("Position demandée pour le bus {}", bus_number);
        (StatusCode::OK, Json(position.clone())).into_response()
    } else {
        (
            StatusCode::NOT_FOUND,
            Json(serde_json::json!({"error": "Bus introuvable"})),
        )
            .into_response()
    }
}

async fn stop_sharing(
    State(state): State<SharedState>,
    Json(payload): Json<StopSharingRequest>,
) -> impl IntoResponse {
    let mut map = state.lock().unwrap();

    if map.remove(&payload.bus_number).is_some() {
        println!("Partage arrêté pour le bus {}", payload.bus_number);
        (
            StatusCode::OK,
            Json(serde_json::json!({
                "message": format!("Partage arrêté pour le bus {}", payload.bus_number)
            })),
        )
    } else {
        (
            StatusCode::NOT_FOUND,
            Json(serde_json::json!({"error": "Bus introuvable"})),
        )
    }
}
