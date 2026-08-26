import json
from pathlib import Path

from fastapi import FastAPI
from pydantic import BaseModel
from typing import Dict

app = FastAPI()

analytics_events: list["AnalyticsEvent"] = []
CONFIG_PATH = Path(__file__).resolve().parent / "pokemon_ui.json"


class AnalyticsEvent(BaseModel):
    event: str
    params: Dict[str, str] | None = None
    timestamp: int


@app.get("/health")
def health_check():
    return {"status": "ok"}


@app.get("/ui/pokemon")
def get_pokemon_ui():
    return json.loads(CONFIG_PATH.read_text(encoding="utf-8"))


@app.post("/analytics/events")
def post_analytics_event(event: AnalyticsEvent):
    analytics_events.append(event)
    print(
        f"[analytics] event={event.event} timestamp={event.timestamp} params={event.params or {}}"
    )
    return {"status": "ok", "received": len(analytics_events)}
