-- create_db.sql (SQLite)
-- Нормалізована схема для:
-- - users (password_sha256 як hex SHA-256)
-- - courses
-- - user_courses (many-to-many)
-- - menu tree
-- - pages
-- - blocks (h1/h2/p/img/video/chart)
-- - charts (meta + x_labels + series + values)

PRAGMA foreign_keys = ON;

BEGIN;

-- =========================
-- 1) USERS
-- =========================
CREATE TABLE IF NOT EXISTS users (
  id              INTEGER PRIMARY KEY AUTOINCREMENT,
  username        TEXT NOT NULL UNIQUE,
  email           TEXT UNIQUE,
  password_sha256 TEXT NOT NULL
    CHECK (length(password_sha256) = 64),
  is_active       INTEGER NOT NULL DEFAULT 1
    CHECK (is_active IN (0, 1)),
  created_at      TEXT NOT NULL DEFAULT (datetime('now')),
  updated_at      TEXT NOT NULL DEFAULT (datetime('now'))
);

CREATE INDEX IF NOT EXISTS idx_users_username ON users(username);

-- =========================
-- 2) COURSES
-- =========================
CREATE TABLE IF NOT EXISTS courses (
  id          INTEGER PRIMARY KEY AUTOINCREMENT,
  slug        TEXT NOT NULL UNIQUE,
  title       TEXT NOT NULL,
  language    TEXT NOT NULL DEFAULT 'uk',
  description TEXT,
  created_at  TEXT NOT NULL DEFAULT (datetime('now')),
  updated_at  TEXT NOT NULL DEFAULT (datetime('now'))
);

CREATE INDEX IF NOT EXISTS idx_courses_slug ON courses(slug);

-- =========================
-- 3) USER <-> COURSE (M2M)
-- =========================
CREATE TABLE IF NOT EXISTS user_courses (
  user_id    INTEGER NOT NULL,
  course_id  INTEGER NOT NULL,
  role       TEXT NOT NULL DEFAULT 'student'
    CHECK (role IN ('student', 'author', 'admin')),
  granted_at TEXT NOT NULL DEFAULT (datetime('now')),
  PRIMARY KEY (user_id, course_id),
  FOREIGN KEY (user_id)  REFERENCES users(id)   ON DELETE CASCADE,
  FOREIGN KEY (course_id) REFERENCES courses(id) ON DELETE CASCADE
);

CREATE INDEX IF NOT EXISTS idx_user_courses_course ON user_courses(course_id);

-- =========================
-- 4) PAGES
-- =========================
CREATE TABLE IF NOT EXISTS course_pages (
  id         INTEGER PRIMARY KEY AUTOINCREMENT,
  course_id  INTEGER NOT NULL,
  slug       TEXT NOT NULL,
  title      TEXT NOT NULL,
  sort_order INTEGER NOT NULL DEFAULT 0,
  created_at TEXT NOT NULL DEFAULT (datetime('now')),
  updated_at TEXT NOT NULL DEFAULT (datetime('now')),
  UNIQUE (course_id, slug),
  FOREIGN KEY (course_id) REFERENCES courses(id) ON DELETE CASCADE
);

CREATE INDEX IF NOT EXISTS idx_pages_course_slug ON course_pages(course_id, slug);

-- =========================
-- 5) MENU NODES (TREE)
-- adjacency list: parent_id -> id
-- leaf node can reference page_id
-- =========================
CREATE TABLE IF NOT EXISTS course_menu_nodes (
  id         INTEGER PRIMARY KEY AUTOINCREMENT,
  course_id  INTEGER NOT NULL,
  parent_id  INTEGER,
  title      TEXT NOT NULL,
  sort_order INTEGER NOT NULL DEFAULT 0,
  page_id    INTEGER,
  created_at TEXT NOT NULL DEFAULT (datetime('now')),
  FOREIGN KEY (course_id) REFERENCES courses(id) ON DELETE CASCADE,
  FOREIGN KEY (parent_id) REFERENCES course_menu_nodes(id) ON DELETE CASCADE,
  FOREIGN KEY (page_id)   REFERENCES course_pages(id) ON DELETE SET NULL
);

CREATE INDEX IF NOT EXISTS idx_menu_course_parent_sort
  ON course_menu_nodes(course_id, parent_id, sort_order);

-- =========================
-- 6) PAGE BLOCKS
-- One row per block in JSON `blocks[]`
-- =========================
CREATE TABLE IF NOT EXISTS page_blocks (
  id            INTEGER PRIMARY KEY AUTOINCREMENT,
  page_id       INTEGER NOT NULL,
  position      INTEGER NOT NULL, -- order within page
  type          TEXT NOT NULL
    CHECK (type IN ('h1','h2','p','img','video','chart')),

  -- For text blocks:
  text          TEXT,

  -- For img/video:
  url           TEXT,

  -- Shared:
  caption       TEXT,

  -- Video options:
  autoplay      INTEGER CHECK (autoplay IN (0,1)),
  loop          INTEGER CHECK (loop IN (0,1)),
  show_controls INTEGER CHECK (show_controls IN (0,1)),

  created_at    TEXT NOT NULL DEFAULT (datetime('now')),

  UNIQUE (page_id, position),
  FOREIGN KEY (page_id) REFERENCES course_pages(id) ON DELETE CASCADE
);

CREATE INDEX IF NOT EXISTS idx_blocks_page_position ON page_blocks(page_id, position);
CREATE INDEX IF NOT EXISTS idx_blocks_type ON page_blocks(type);

-- =========================
-- 7) CHART BLOCKS (meta)
-- only for blocks where type='chart'
-- =========================
CREATE TABLE IF NOT EXISTS chart_blocks (
  block_id    INTEGER PRIMARY KEY,
  chart_type  TEXT NOT NULL
    CHECK (chart_type IN ('line','bar')),
  title       TEXT,
  FOREIGN KEY (block_id) REFERENCES page_blocks(id) ON DELETE CASCADE
);

-- x-axis labels as normalized rows: (block_id, idx) -> label
CREATE TABLE IF NOT EXISTS chart_x_labels (
  block_id INTEGER NOT NULL,
  idx      INTEGER NOT NULL,
  label    TEXT NOT NULL,
  PRIMARY KEY (block_id, idx),
  FOREIGN KEY (block_id) REFERENCES chart_blocks(block_id) ON DELETE CASCADE
);

-- series per chart: (block_id) -> many series
CREATE TABLE IF NOT EXISTS chart_series (
  id         INTEGER PRIMARY KEY AUTOINCREMENT,
  block_id   INTEGER NOT NULL,
  name       TEXT NOT NULL,
  sort_order INTEGER NOT NULL DEFAULT 0,
  FOREIGN KEY (block_id) REFERENCES chart_blocks(block_id) ON DELETE CASCADE
);

CREATE INDEX IF NOT EXISTS idx_chart_series_block ON chart_series(block_id, sort_order);

-- values per series: (series_id, x_idx) -> y_value
CREATE TABLE IF NOT EXISTS chart_series_values (
  series_id INTEGER NOT NULL,
  x_idx     INTEGER NOT NULL,
  y_value   REAL NOT NULL,
  PRIMARY KEY (series_id, x_idx),
  FOREIGN KEY (series_id) REFERENCES chart_series(id) ON DELETE CASCADE
);

CREATE INDEX IF NOT EXISTS idx_chart_values_series ON chart_series_values(series_id);

COMMIT;

-- Notes:
-- - `password_sha256` stores HEX SHA-256 (64 chars).
-- - SQLite doesn't compute SHA-256 by default; hash on backend and store the hex string here.
-- - If you need auto-updated updated_at timestamps, do it in application code
--   or add triggers (optional).
