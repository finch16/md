import json
import sys
from urllib import request, error
from urllib.parse import quote


BASE_URL = "http://127.0.0.1:5000"
USERNAME = "admin"
PASSWORD = "admin"

COURSE_SLUG = None 


def http_get(url: str, headers: dict | None = None, timeout: int = 10) -> tuple[int, dict]:
    req = request.Request(url, headers=headers or {}, method="GET")
    try:
        with request.urlopen(req, timeout=timeout) as resp:
            body = resp.read().decode("utf-8")
            return resp.status, json.loads(body)
    except error.HTTPError as e:
        body = e.read().decode("utf-8", errors="replace")
        try:
            return e.code, json.loads(body)
        except Exception:
            return e.code, {"raw": body}
    except Exception as e:
        raise RuntimeError(f"GET {url} failed: {e}") from e


def http_post_json(url: str, payload: dict, headers: dict | None = None, timeout: int = 10) -> tuple[int, dict]:
    data = json.dumps(payload).encode("utf-8")
    h = {"Content-Type": "application/json"}
    if headers:
        h.update(headers)
    req = request.Request(url, data=data, headers=h, method="POST")
    try:
        with request.urlopen(req, timeout=timeout) as resp:
            body = resp.read().decode("utf-8")
            return resp.status, json.loads(body)
    except error.HTTPError as e:
        body = e.read().decode("utf-8", errors="replace")
        try:
            return e.code, json.loads(body)
        except Exception:
            return e.code, {"raw": body}
    except Exception as e:
        raise RuntimeError(f"POST {url} failed: {e}") from e


def main():
    base = BASE_URL.rstrip("/")
    print(f"== Checking API at {base} ==")

    # 1) health
    code, data = http_get(f"{base}/health")
    if code != 200:
        print(f"[FAIL] /health -> {code}: {data}")
        sys.exit(1)
    print(f"[OK]   /health -> {data}")

    # 2) login
    code, data = http_post_json(
        f"{base}/auth/login",
        {"username": USERNAME, "password": PASSWORD}
    )
    if code != 200:
        print(f"[FAIL] /auth/login -> {code}: {data}")
        sys.exit(1)

    token = data.get("access_token")
    if not token:
        print(f"[FAIL] /auth/login returned no access_token: {data}")
        sys.exit(1)

    print(f"[OK]   /auth/login -> token received, expires_in={data.get('expires_in')}")

    headers = {"Authorization": f"Bearer {token}"}

    # 3) courses
    code, courses = http_get(f"{base}/courses", headers=headers)
    if code != 200:
        print(f"[FAIL] /courses -> {code}: {courses}")
        sys.exit(1)

    items = courses.get("items") or []
    if not isinstance(items, list):
        print(f"[FAIL] /courses invalid format: {courses}")
        sys.exit(1)

    if len(items) == 0:
        print("[FAIL] /courses -> empty list. User has no доступных курсов.")
        sys.exit(1)

    # pick course
    chosen = None
    if COURSE_SLUG:
        for c in items:
            if c.get("slug") == COURSE_SLUG:
                chosen = c
                break
        if not chosen:
            print(f"[FAIL] COURSE_SLUG='{COURSE_SLUG}' not found in /courses items")
            print("Available slugs:", [c.get("slug") for c in items])
            sys.exit(1)
    else:
        chosen = items[0]

    slug = chosen.get("slug")
    title = chosen.get("title")
    if not slug:
        print(f"[FAIL] /courses item missing slug: {chosen}")
        sys.exit(1)

    print(f"[OK]   /courses -> {len(items)} course(s). Using: slug='{slug}', title='{title}'")

    # 4) content
    code, content = http_get(f"{base}/content?course={quote(slug)}", headers=headers)
    if code != 200:
        print(f"[FAIL] /content -> {code}: {content}")
        sys.exit(1)

    # Validate shape
    missing = [k for k in ("appTitle", "menu", "pages") if k not in content]
    if missing:
        print(f"[FAIL] /content missing keys: {missing}")
        print("Response:", content)
        sys.exit(1)

    pages = content.get("pages") or {}
    menu = content.get("menu") or []
    print(f"[OK]   /content -> appTitle='{content.get('appTitle')}', pages={len(pages)}, menuTop={len(menu)}")

    # Save full response
    safe_slug = "".join(ch if ch.isalnum() or ch in ("-", "_") else "_" for ch in slug)
    out_file = f"content_dump_{safe_slug}.json"
    with open(out_file, "w", encoding="utf-8") as f:
        json.dump(content, f, ensure_ascii=False, indent=2)
    print(f"[OK]   Saved full JSON to {out_file}")

    print("== DONE ==")


if __name__ == "__main__":
    main()
