import os
import time
import json
import hashlib
from functools import wraps
from typing import Dict, Any, List, Optional

import jwt
from flask import Flask, jsonify, request

from sqlalchemy import (
    create_engine, Column, Integer, BigInteger, Text, ForeignKey, Float, UniqueConstraint
)
from sqlalchemy.orm import declarative_base, relationship, sessionmaker, Session

# =========================
# Config
# =========================
DB_URL = os.getenv("DB_URL", "sqlite:///db/data.db")

JWT_SECRET = os.getenv("JWT_SECRET", "dev-secret-change-me")  # в проді тільки через env
JWT_ALG = "HS256"
JWT_TTL_SECONDS = int(os.getenv("JWT_TTL_SECONDS", "3600"))  # 1 година

Base = declarative_base()

engine = create_engine(DB_URL, echo=False, future=True)
SessionLocal = sessionmaker(bind=engine, autoflush=False, autocommit=False, future=True)


# =========================
# Models (відповідають create_db.sql)
# =========================
class User(Base):
    __tablename__ = "users"
    id = Column(Integer, primary_key=True)
    username = Column(Text, nullable=False, unique=True)
    email = Column(Text, nullable=True, unique=True)
    password_sha256 = Column(Text, nullable=False)  # hex 64
    is_active = Column(Integer, nullable=False, default=1)

    courses = relationship("UserCourse", back_populates="user", cascade="all, delete-orphan")


class Course(Base):
    __tablename__ = "courses"
    id = Column(Integer, primary_key=True)
    slug = Column(Text, nullable=False, unique=True)
    title = Column(Text, nullable=False)
    language = Column(Text, nullable=False, default="uk")
    description = Column(Text, nullable=True)

    pages = relationship("CoursePage", back_populates="course", cascade="all, delete-orphan")
    menu_nodes = relationship("CourseMenuNode", back_populates="course", cascade="all, delete-orphan")


class UserCourse(Base):
    __tablename__ = "user_courses"
    user_id = Column(Integer, ForeignKey("users.id", ondelete="CASCADE"), primary_key=True)
    course_id = Column(Integer, ForeignKey("courses.id", ondelete="CASCADE"), primary_key=True)
    role = Column(Text, nullable=False, default="student")  # student|author|admin

    user = relationship("User", back_populates="courses")
    course = relationship("Course")


class CoursePage(Base):
    __tablename__ = "course_pages"
    id = Column(Integer, primary_key=True)
    course_id = Column(Integer, ForeignKey("courses.id", ondelete="CASCADE"), nullable=False)
    slug = Column(Text, nullable=False)
    title = Column(Text, nullable=False)
    sort_order = Column(Integer, nullable=False, default=0)

    __table_args__ = (UniqueConstraint("course_id", "slug", name="uq_course_pages_course_slug"),)

    course = relationship("Course", back_populates="pages")
    blocks = relationship("PageBlock", back_populates="page", cascade="all, delete-orphan")


class CourseMenuNode(Base):
    __tablename__ = "course_menu_nodes"
    id = Column(Integer, primary_key=True)
    course_id = Column(Integer, ForeignKey("courses.id", ondelete="CASCADE"), nullable=False)
    parent_id = Column(Integer, ForeignKey("course_menu_nodes.id", ondelete="CASCADE"), nullable=True)
    title = Column(Text, nullable=False)
    sort_order = Column(Integer, nullable=False, default=0)
    page_id = Column(Integer, ForeignKey("course_pages.id", ondelete="SET NULL"), nullable=True)

    course = relationship("Course", back_populates="menu_nodes")
    parent = relationship("CourseMenuNode", remote_side=[id], backref="children")
    page = relationship("CoursePage")


class PageBlock(Base):
    __tablename__ = "page_blocks"
    id = Column(Integer, primary_key=True)
    page_id = Column(Integer, ForeignKey("course_pages.id", ondelete="CASCADE"), nullable=False)
    position = Column(Integer, nullable=False)  # порядок на сторінці
    type = Column(Text, nullable=False)  # h1,h2,p,img,video,chart

    text = Column(Text, nullable=True)
    url = Column(Text, nullable=True)
    caption = Column(Text, nullable=True)

    autoplay = Column(Integer, nullable=True)       # 0/1
    loop = Column(Integer, nullable=True)           # 0/1
    show_controls = Column(Integer, nullable=True)  # 0/1

    __table_args__ = (UniqueConstraint("page_id", "position", name="uq_page_blocks_page_position"),)

    page = relationship("CoursePage", back_populates="blocks")
    chart = relationship("ChartBlock", back_populates="block", uselist=False, cascade="all, delete-orphan")


class ChartBlock(Base):
    __tablename__ = "chart_blocks"
    block_id = Column(Integer, ForeignKey("page_blocks.id", ondelete="CASCADE"), primary_key=True)
    chart_type = Column(Text, nullable=False)  # line|bar
    title = Column(Text, nullable=True)

    block = relationship("PageBlock", back_populates="chart")
    x_labels = relationship("ChartXLabel", back_populates="chart", cascade="all, delete-orphan")
    series = relationship("ChartSeries", back_populates="chart", cascade="all, delete-orphan")


class ChartXLabel(Base):
    __tablename__ = "chart_x_labels"
    block_id = Column(Integer, ForeignKey("chart_blocks.block_id", ondelete="CASCADE"), primary_key=True)
    idx = Column(Integer, primary_key=True)
    label = Column(Text, nullable=False)

    chart = relationship("ChartBlock", back_populates="x_labels")


class ChartSeries(Base):
    __tablename__ = "chart_series"
    id = Column(Integer, primary_key=True)
    block_id = Column(Integer, ForeignKey("chart_blocks.block_id", ondelete="CASCADE"), nullable=False)
    name = Column(Text, nullable=False)
    sort_order = Column(Integer, nullable=False, default=0)

    chart = relationship("ChartBlock", back_populates="series")
    values = relationship("ChartSeriesValue", back_populates="series", cascade="all, delete-orphan")


class ChartSeriesValue(Base):
    __tablename__ = "chart_series_values"
    series_id = Column(Integer, ForeignKey("chart_series.id", ondelete="CASCADE"), primary_key=True)
    x_idx = Column(Integer, primary_key=True)
    y_value = Column(Float, nullable=False)

    series = relationship("ChartSeries", back_populates="values")


# =========================
# JWT helpers
# =========================
def create_access_token(sub: str) -> str:
    now = int(time.time())
    payload = {"sub": sub, "iat": now, "exp": now + JWT_TTL_SECONDS}
    return jwt.encode(payload, JWT_SECRET, algorithm=JWT_ALG)


def decode_token(token: str) -> dict:
    return jwt.decode(token, JWT_SECRET, algorithms=[JWT_ALG])


def require_auth(fn):
    @wraps(fn)
    def wrapper(*args, **kwargs):
        auth = request.headers.get("Authorization", "")
        if not auth.startswith("Bearer "):
            return jsonify({"error": "Missing or invalid Authorization header"}), 401

        token = auth.removeprefix("Bearer ").strip()
        if not token:
            return jsonify({"error": "Missing token"}), 401

        try:
            claims = decode_token(token)
            request.jwt_claims = claims
        except jwt.ExpiredSignatureError:
            return jsonify({"error": "Token expired"}), 401
        except jwt.InvalidTokenError:
            return jsonify({"error": "Invalid token"}), 401

        return fn(*args, **kwargs)
    return wrapper


# =========================
# Utils
# =========================
def sha256_hex(s: str) -> str:
    return hashlib.sha256(s.encode("utf-8")).hexdigest()


def get_db() -> Session:
    return SessionLocal()


def bool_from_int(x: Optional[int]) -> Optional[bool]:
    if x is None:
        return None
    return bool(int(x))


# =========================
# JSON assembly (DB -> Android JSON)
# =========================
def build_menu_tree(nodes: List[CourseMenuNode]) -> List[Dict[str, Any]]:
    """
    Перетворюємо adjacency list в вкладений menu[].
    Leaf node => {"title": ..., "pageId": "<page_slug>"}
    Parent node => {"title": ..., "children": [...]}
    """
    # index by parent_id
    by_parent: Dict[Optional[int], List[CourseMenuNode]] = {}
    for n in nodes:
        by_parent.setdefault(n.parent_id, []).append(n)

    # sort children
    for pid in by_parent:
        by_parent[pid].sort(key=lambda x: (x.sort_order, x.id))

    def to_json(node: CourseMenuNode) -> Dict[str, Any]:
        children = by_parent.get(node.id, [])
        if children:
            return {
                "title": node.title,
                "children": [to_json(ch) for ch in children]
            }
        # leaf
        page_slug = node.page.slug if node.page else None
        if page_slug is None:
            # “порожній” leaf без сторінки — все одно повертаємо як секцію
            return {"title": node.title, "children": []}
        return {"title": node.title, "pageId": page_slug}

    roots = by_parent.get(None, []) + by_parent.get(0, [])
    return [to_json(r) for r in roots]


def build_pages_map(pages: List[CoursePage]) -> Dict[str, Any]:
    """
    pages: Map<slug, {title, blocks:[...]}>
    blocks: id (int), type, text/url/caption, autoplay/loop/showControls,
            chartType/title/xLabels/series[]
    """
    result: Dict[str, Any] = {}

    # ensure blocks ordered
    for p in pages:
        blocks_sorted = sorted(p.blocks, key=lambda b: (b.position, b.id))

        blocks_out: List[Dict[str, Any]] = []
        for b in blocks_sorted:
            out: Dict[str, Any] = {
                "id": int(b.position),  # стабільний id в рамках сторінки
                "type": b.type
            }

            if b.text is not None:
                out["text"] = b.text
            if b.url is not None:
                out["url"] = b.url
            if b.caption is not None:
                out["caption"] = b.caption

            # video options (тільки якщо є)
            if b.type == "video":
                if b.autoplay is not None:
                    out["autoplay"] = bool_from_int(b.autoplay)
                if b.loop is not None:
                    out["loop"] = bool_from_int(b.loop)
                if b.show_controls is not None:
                    out["showControls"] = bool_from_int(b.show_controls)

            # chart options
            if b.type == "chart" and b.chart is not None:
                cb = b.chart
                out["chartType"] = cb.chart_type
                if cb.title is not None:
                    out["title"] = cb.title

                labels = sorted(cb.x_labels, key=lambda x: x.idx)
                if labels:
                    out["xLabels"] = [x.label for x in labels]

                series_sorted = sorted(cb.series, key=lambda s: (s.sort_order, s.id))
                series_out = []
                for s in series_sorted:
                    vals = sorted(s.values, key=lambda v: v.x_idx)
                    series_out.append({
                        "name": s.name,
                        "values": [float(v.y_value) for v in vals]
                    })
                out["series"] = series_out

            blocks_out.append(out)

        result[p.slug] = {"title": p.title, "blocks": blocks_out}

    return result


def build_course_content_json(db: Session, course_slug: str) -> Dict[str, Any]:
    course: Course | None = db.query(Course).filter(Course.slug == course_slug).one_or_none()
    if course is None:
        raise ValueError(f"Course not found: {course_slug}")

    # load menu nodes with page relationship
    nodes = (
        db.query(CourseMenuNode)
        .filter(CourseMenuNode.course_id == course.id)
        .all()
    )

    # load pages + blocks + charts in a simple way (SQLite, so keep it straightforward)
    pages = (
        db.query(CoursePage)
        .filter(CoursePage.course_id == course.id)
        .all()
    )
    # Touch relationships to ensure loaded (not strictly necessary, but keeps it simple)
    for p in pages:
        for b in p.blocks:
            _ = b.chart
            if b.chart:
                _ = b.chart.x_labels
                _ = b.chart.series
                for s in b.chart.series:
                    _ = s.values

    return {
        "appTitle": course.title,
        "menu": build_menu_tree(nodes),
        "pages": build_pages_map(pages)
    }


# =========================
# Flask app
# =========================
app = Flask(__name__)


@app.get("/health")
def health():
    return jsonify({"status": "ok"})


@app.post("/auth/login")
def login():
    """
    Body JSON:
    { "username": "...", "password": "..." }
    """
    data = request.get_json(silent=True) or {}
    username = (data.get("username") or "").strip()
    password = (data.get("password") or "").strip()

    if not username or not password:
        return jsonify({"error": "username and password required"}), 400

    db = get_db()
    try:
        user = db.query(User).filter(User.username == username).one_or_none()
        if user is None or int(user.is_active) != 1:
            return jsonify({"error": "Invalid credentials"}), 401

        if user.password_sha256 != sha256_hex(password):
            return jsonify({"error": "Invalid credentials"}), 401

        token = create_access_token(sub=user.username)
        return jsonify({
            "access_token": token,
            "token_type": "Bearer",
            "expires_in": JWT_TTL_SECONDS
        })
    finally:
        db.close()


@app.get("/courses")
@require_auth
def list_courses():
    """
    Возвращает курсы, к которым у пользователя есть доступ (user_courses).
    """
    username = getattr(request, "jwt_claims", {}).get("sub")
    if not username:
        return jsonify({"error": "Invalid token claims"}), 401

    db = get_db()
    try:
        user = db.query(User).filter(User.username == username).one_or_none()
        if user is None:
            return jsonify({"error": "User not found"}), 401

        rows = (
            db.query(Course)
            .join(UserCourse, UserCourse.course_id == Course.id)
            .filter(UserCourse.user_id == user.id)
            .all()
        )

        return jsonify({
            "items": [
                {"slug": c.slug, "title": c.title, "language": c.language}
                for c in rows
            ]
        })
    finally:
        db.close()


@app.get("/content")
@require_auth
def get_content():
    """
    GET /content?course=<course_slug>
    Header: Authorization: Bearer <JWT>
    """
    course_slug = (request.args.get("course") or "").strip()
    if not course_slug:
        return jsonify({"error": "Missing query param: course"}), 400

    username = getattr(request, "jwt_claims", {}).get("sub")
    if not username:
        return jsonify({"error": "Invalid token claims"}), 401

    db = get_db()
    try:
        user = db.query(User).filter(User.username == username).one_or_none()
        if user is None:
            return jsonify({"error": "User not found"}), 401

        course = db.query(Course).filter(Course.slug == course_slug).one_or_none()
        if course is None:
            return jsonify({"error": "Course not found"}), 404

        # Access check (many-to-many)
        access = (
            db.query(UserCourse)
            .filter(UserCourse.user_id == user.id, UserCourse.course_id == course.id)
            .one_or_none()
        )
        if access is None:
            return jsonify({"error": "Forbidden"}), 403

        content_json = build_course_content_json(db, course_slug=course_slug)
        return jsonify(content_json)
    except ValueError as e:
        return jsonify({"error": str(e)}), 404
    finally:
        db.close()


if __name__ == "__main__":
    # Flask dev server (не прод)
    app.run(host="0.0.0.0", port=5000, debug=True)
