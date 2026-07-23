#!/usr/bin/env python3
"""Tiny local AURA backend for emulator testing.

Run from the repo root:
    python3 tools/local_backend.py

Point the Android app at:
    http://10.0.2.2:8080/
"""

from __future__ import annotations

import json
import math
import time
from http.server import BaseHTTPRequestHandler, ThreadingHTTPServer
from urllib.parse import parse_qs, urlparse


HOST = "0.0.0.0"
PORT = 8080

notifications_by_phone: dict[str, dict[str, dict]] = {}
invite_responses: dict[str, dict] = {}
safety_sessions: dict[str, dict] = {}
safety_updates: dict[str, dict] = {}
reports: dict[str, dict] = {}


def normalize_phone(value: str | None) -> str:
    digits = "".join(ch for ch in (value or "") if ch.isdigit())
    return digits[-10:] if len(digits) > 10 else digits


def now_millis() -> int:
    return int(time.time() * 1000)


def parse_float(value: str | None) -> float | None:
    if value is None:
        return None
    try:
        return float(value)
    except ValueError:
        return None


def parse_int(value: str | None) -> int | None:
    if value is None:
        return None
    try:
        return int(value)
    except ValueError:
        return None


def distance_meters(
    origin_latitude: float,
    origin_longitude: float,
    target_latitude: float,
    target_longitude: float,
) -> int:
    earth_radius_meters = 6_371_000
    origin_lat = math.radians(origin_latitude)
    target_lat = math.radians(target_latitude)
    delta_lat = math.radians(target_latitude - origin_latitude)
    delta_lng = math.radians(target_longitude - origin_longitude)
    haversine = (
        math.sin(delta_lat / 2) ** 2
        + math.cos(origin_lat) * math.cos(target_lat) * math.sin(delta_lng / 2) ** 2
    )
    return round(earth_radius_meters * 2 * math.atan2(math.sqrt(haversine), math.sqrt(1 - haversine)))


def report_to_alert(report_id: str, report: dict, distance: int | None = None) -> dict:
    occurred_at = int(report.get("occurredAtMillis") or now_millis())
    description = report.get("description") or "Reporte ciudadano sincronizado desde AURA."
    return {
        "id": f"alert:{report_id}",
        "reportId": report_id,
        "type": report.get("type") or "OTHER",
        "severity": report.get("severity") or "MEDIUM",
        "status": "UNVERIFIED",
        "latitude": report.get("latitude"),
        "longitude": report.get("longitude"),
        "locationPrecision": report.get("locationPrecision") or "APPROXIMATE",
        "summary": description,
        "distanceMeters": distance,
        "reportedAtMillis": occurred_at,
    }


def nearby_alerts(query: dict[str, list[str]]) -> list[dict]:
    latitude = parse_float(query.get("lat", [None])[0])
    longitude = parse_float(query.get("lng", [None])[0])
    radius = parse_int(query.get("radius", [None])[0])
    alerts = []

    for report_id, report in reports.items():
        report_latitude = parse_float(str(report.get("latitude")))
        report_longitude = parse_float(str(report.get("longitude")))
        if report_latitude is None or report_longitude is None:
            continue

        distance = None
        if latitude is not None and longitude is not None:
            distance = distance_meters(latitude, longitude, report_latitude, report_longitude)
            if radius is not None and distance > radius:
                continue

        alerts.append(report_to_alert(report_id, report, distance))

    alerts.sort(key=lambda item: item.get("reportedAtMillis", 0), reverse=True)
    return alerts


def store_notification(phone_number: str | None, notification: dict) -> None:
    key = normalize_phone(phone_number)
    if not key:
        return
    notifications_by_phone.setdefault(key, {})[notification["id"]] = notification


class AuraLocalBackend(BaseHTTPRequestHandler):
    server_version = "AuraLocalBackend/0.1"

    def do_GET(self) -> None:
        parsed = urlparse(self.path)
        query = parse_qs(parsed.query)

        if parsed.path == "/health":
            self.respond_json({"ok": True, "timeMillis": now_millis()})
            return

        if parsed.path == "/alerts/nearby":
            self.respond_json(nearby_alerts(query))
            return

        if parsed.path == "/guardian-notifications":
            phone_number = query.get("phoneNumber", [""])[0]
            notifications = list(notifications_by_phone.get(normalize_phone(phone_number), {}).values())
            notifications.sort(key=lambda item: item.get("createdAtMillis", 0), reverse=True)
            self.respond_json(notifications)
            return

        self.respond_json({"error": "Not found", "path": parsed.path}, status=404)

    def do_POST(self) -> None:
        parsed = urlparse(self.path)
        body = self.read_json()

        if parsed.path == "/reports":
            report_id = body.get("clientId") or f"report-{now_millis()}"
            reports[report_id] = body
            self.respond_json({"id": report_id, "syncedAtMillis": now_millis()})
            return

        if parsed.path.startswith("/reports/") and parsed.path.endswith("/evidence"):
            self.respond_json({"remoteUrl": f"local://evidence/{body.get('clientId', now_millis())}", "syncedAtMillis": now_millis()})
            return

        if parsed.path.startswith("/reports/") and parsed.path.endswith("/verifications"):
            self.respond_json({"ok": True, "syncedAtMillis": now_millis()})
            return

        if parsed.path == "/safety-sessions":
            session_id = body.get("clientId") or f"session-{now_millis()}"
            safety_sessions[session_id] = body
            self.respond_json({"ok": True, "sessionId": session_id})
            return

        if parsed.path.startswith("/safety-sessions/") and parsed.path.endswith("/updates"):
            update_id = body.get("clientId") or f"update-{now_millis()}"
            safety_updates[update_id] = body
            self.respond_json({"ok": True, "updateId": update_id})
            return

        if parsed.path.startswith("/safety-sessions/") and parsed.path.endswith("/sos-notifications"):
            self.handle_sos_notifications(body)
            return

        if parsed.path == "/guardian-invitations":
            self.handle_guardian_invitation(body)
            return

        if parsed.path.startswith("/guardian-notifications/") and parsed.path.endswith("/response"):
            notification_id = parsed.path.split("/")[-2]
            invite_responses[notification_id] = body
            self.respond_json({"ok": True, "notificationId": notification_id, "syncedAtMillis": now_millis()})
            return

        self.respond_json({"error": "Not found", "path": parsed.path}, status=404)

    def handle_guardian_invitation(self, body: dict) -> None:
        contact_id = body.get("contactId") or f"contact-{now_millis()}"
        invitee_phone = body.get("inviteePhoneNumber")
        inviter_name = body.get("inviterName") or "Red Guardian"
        inviter_phone = body.get("inviterPhoneNumber")
        created_at = int(body.get("createdAtMillis") or now_millis())
        notification = {
            "id": f"guardian-invite:{contact_id}",
            "type": "GUARDIAN_INVITE",
            "senderName": inviter_name,
            "senderPhoneNumber": inviter_phone,
            "senderPhotoUri": None,
            "message": body.get("message") or f"{inviter_name} te agrego a su Red Guardian. Acepta para formar parte.",
            "sessionId": None,
            "latitude": None,
            "longitude": None,
            "createdAtMillis": created_at,
        }
        store_notification(invitee_phone, notification)
        self.respond_json({"ok": True, "notification": notification})

    def handle_sos_notifications(self, body: dict) -> None:
        session_id = body.get("sessionId")
        update_id = body.get("updateId") or f"sos-{now_millis()}"
        created_at = int(body.get("createdAtMillis") or now_millis())
        stored = []
        for contact in body.get("contacts", []):
            contact_phone = contact.get("phoneNumber")
            notification = {
                "id": f"guardian-sos:{update_id}:{normalize_phone(contact_phone)}",
                "type": "SOS_ALERT",
                "senderName": "AURA SOS",
                "senderPhoneNumber": None,
                "senderPhotoUri": None,
                "message": body.get("message") or "SOS activado",
                "sessionId": session_id,
                "latitude": body.get("latitude"),
                "longitude": body.get("longitude"),
                "createdAtMillis": created_at,
            }
            store_notification(contact_phone, notification)
            stored.append(notification)
        self.respond_json({"ok": True, "notifications": stored})

    def read_json(self) -> dict:
        length = int(self.headers.get("content-length", "0"))
        if length == 0:
            return {}
        raw = self.rfile.read(length).decode("utf-8")
        return json.loads(raw) if raw else {}

    def respond_json(self, payload: dict | list, status: int = 200) -> None:
        encoded = json.dumps(payload).encode("utf-8")
        self.send_response(status)
        self.send_header("content-type", "application/json; charset=utf-8")
        self.send_header("content-length", str(len(encoded)))
        self.end_headers()
        self.wfile.write(encoded)

    def log_message(self, fmt: str, *args) -> None:
        print(f"{self.log_date_time_string()} {self.address_string()} {fmt % args}")


if __name__ == "__main__":
    server = ThreadingHTTPServer((HOST, PORT), AuraLocalBackend)
    print(f"AURA local backend listening on http://{HOST}:{PORT}/")
    print("Emulator base URL: http://10.0.2.2:8080/")
    server.serve_forever()
