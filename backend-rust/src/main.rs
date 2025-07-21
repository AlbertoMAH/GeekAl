use axum::{
    extract::{Path, State},
    http::{Method, StatusCode},
    response::IntoResponse,
    routing::{get, post},
    Json, Router,
};
use chrono::Utc;
use geojson::{Feature, FeatureCollection, GeoJson, Geometry, Value};
use serde::{Deserialize, Serialize};
use std::{
    collections::{HashMap, HashSet},
    env,
    fs::File,
    io::Read,
    net::SocketAddr,
    sync::{Arc, Mutex},
};
use tokio::net::TcpListener;
use tower_http::cors::{Any, CorsLayer};

#[derive(Debug, Serialize, Deserialize, Clone)]
struct Position {
    latitude: f64,
    longitude: f64,
    timestamp: i64,
}

#[derive(Debug, Deserialize)]
struct PositionRequest {
    #[serde(rename = "busNumber")]
    bus_number: String,
    latitude: f64,
    longitude: f64,
}

#[derive(Debug, Deserialize)]
struct StopSharingRequest {
    #[serde(rename = "busNumber")]
    bus_number: String,
}

// État partagé
type SharedState = Arc<Mutex<HashMap<String, Position>>>;
type GeoJsonData = Arc<Vec<Feature>>;

#[tokio::main]
async fn main() {
    dotenvy::dotenv().ok();
    let port = env::var("PORT").unwrap_or_else(|_| "10000".into());
    let addr = format!("0.0.0.0:{}", port).parse::<SocketAddr>().unwrap();

    // Charger light.geojson une fois au démarrage
    let lignes_geojson = load_geojson("light.geojson").expect("Erreur de chargement GeoJSON");

    let state: SharedState = Arc::new(Mutex::new(HashMap::new()));
    let lines_data = Arc::new(lignes_geojson);

    let cors = CorsLayer::new()
        .allow_origin(Any)
        .allow_methods([Method::GET, Method::POST, Method::OPTIONS])
        .allow_headers(Any);

    let app = Router::new()
        .route("/", get(home))
        .route("/api/position", post(save_position))
        .route("/api/position/:bus_number", get(get_position))
        .route("/api/stopSharing", post(stop_sharing))
        .route("/api/line/:bus_number", get(get_line)) // 🆕 nouvelle route
        .with_state((state, lines_data)) // ← Tu partages deux états ici
        .layer(cors);

    println!("✅ Serveur Rust en écoute sur http://{}", addr);
    println!("🌍 Accessible publiquement : https://testrust-4io8.onrender.com");

    let listener = TcpListener::bind(&addr).await.unwrap();
    axum::serve(listener, app).await.unwrap();
}

fn load_geojson(path: &str) -> Result<Vec<Feature>, Box<dyn std::error::Error>> {
    let mut file = File::open(path)?;
    let mut content = String::new();
    file.read_to_string(&mut content)?;
    let geojson: GeoJson = content.parse()?;

    if let GeoJson::FeatureCollection(fc) = geojson {
        Ok(fc.features)
    } else {
        Err("Format incorrect".into())
    }
}

async fn home() -> impl IntoResponse {
    let response = serde_json::json!({
        "message": "API de suivi de bus en temps réel",
        "version": "1.0",
        "status": "active",
        "endpoints": {
            "get_position": "GET /api/position/{bus_number}",
            "post_position": "POST /api/position",
            "stop_sharing": "POST /api/stopSharing",
            "get_line": "GET /api/line/{bus_number}"
        },
        "url_publique": "https://testrust-4io8.onrender.com"
    });
    (StatusCode::OK, Json(response))
}

async fn save_position(
    State((state, _)): State<(SharedState, GeoJsonData)>,
    Json(payload): Json<PositionRequest>,
) -> impl IntoResponse {
    let mut map = state.lock().unwrap();
    let position = Position {
        latitude: payload.latitude,
        longitude: payload.longitude,
        timestamp: Utc::now().timestamp_millis(),
    };
    map.insert(payload.bus_number.clone(), position);

    println!("✅ Position enregistrée pour le bus {}", payload.bus_number);

    (StatusCode::OK, Json(serde_json::json!({"message": "Position enregistrée"})))
}

async fn get_position(
    Path(bus_number): Path<String>,
    State((state, _)): State<(SharedState, GeoJsonData)>,
) -> impl IntoResponse {
    let map = state.lock().unwrap();
    if let Some(position) = map.get(&bus_number) {
        println!("📡 Position demandée pour le bus {}", bus_number);
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
    State((state, _)): State<(SharedState, GeoJsonData)>,
    Json(payload): Json<StopSharingRequest>,
) -> impl IntoResponse {
    let mut map = state.lock().unwrap();
    if map.remove(&payload.bus_number).is_some() {
        println!("🛑 Partage arrêté pour le bus {}", payload.bus_number);
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

async fn get_line(
    Path(bus_number): Path<String>,
    State((_, lines_data)): State<(SharedState, GeoJsonData)>,
) -> impl IntoResponse {
    let code_recherche = match bus_number.parse::<i64>() {
        Ok(num) => num,
        Err(_) => {
            return (
                StatusCode::BAD_REQUEST,
                Json(serde_json::json!({ "error": "Code ligne invalide" })),
            )
                .into_response();
        }
    };

    for feature in lines_data.iter() {
        if let Some(props) = &feature.properties {
            if let Some(code) = props.get("code") {
                if code.as_i64() == Some(code_recherche) {
                    return (StatusCode::OK, Json(feature)).into_response();
                }
            }
        }
    }

    (
        StatusCode::NOT_FOUND,
        Json(serde_json::json!({ "error": "Ligne non trouvée" })),
    )
        .into_response()
}
