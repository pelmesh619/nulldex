from fastapi import FastAPI
from pydantic import BaseModel
from typing import Dict

app = FastAPI()

analytics_events: list["AnalyticsEvent"] = []


class UIComponent(BaseModel):
    id: str
    type: str
    label: str | None = None
    action: dict | None = None
    analytics: dict | None = None

class PokemonUIConfig(BaseModel):
    components: list[UIComponent]


class AnalyticsEvent(BaseModel):
    event: str
    params: Dict[str, str] | None = None
    timestamp: int


@app.get("/health")
def health_check():
    return {"status": "ok"}

@app.get("/ui/pokemon", response_model=PokemonUIConfig)
def get_pokemon_ui():
    return PokemonUIConfig(
        components=[
            UIComponent(
                id="title",
                type="title",
            ),

            UIComponent(
                id="sprite",
                type="sprite",
                analytics={"impressionEvent": "pokemon_sprite_impression"}
            ),
            UIComponent(
                id="number",
                type="number",
            ),
            UIComponent(
                id="types",
                type="type_badges"
            ),
            UIComponent(
                id="abilities",
                type="abilities",
                label="Abilities",
                action={"type": "show_toast", "payload": {"message": "Abilities shown"}},
                analytics={
                    "impressionEvent": "pokemon_abilities_impression",
                    "clickEvent": "pokemon_abilities_shown"
                }
            ),
            UIComponent(id="divider", type="divider"),
            UIComponent(
                id="height",
                type="stat",
                label="Height",
            ),
            UIComponent(
                id="weight",
                type="stat",
                label="Weight",
            ),
            UIComponent(
                id="base_experience",
                type="stat",
                label="Base experience",
            ),
        ]
    )


@app.post("/analytics/events")
def post_analytics_event(event: AnalyticsEvent):
    analytics_events.append(event)
    print(f"[analytics] event={event.event} timestamp={event.timestamp} params={event.params or {}}")
    return {"status": "ok", "received": len(analytics_events)}
