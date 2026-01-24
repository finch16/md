PRAGMA foreign_keys = ON;

BEGIN;

-- =========================
-- USER
-- username: admin
-- password: admin
-- sha256("admin") = 8c6976e5b5410415bde908bd4dee15dfb167a9c873fc4bb8a81f6f2ab448a918
-- =========================
INSERT INTO users (username, email, password_sha256)
VALUES ('admin', 'admin@example.com', '8c6976e5b5410415bde908bd4dee15dfb167a9c873fc4bb8a81f6f2ab448a918');


-- =========================
-- COURSE
-- =========================
INSERT INTO courses (slug, title, description)
VALUES (
  'android-kotlin-course',
  'Повний курс: Android-розробка на Kotlin',
  'Інтерактивний курс з текстом, медіа, відео та графіками'
);

-- =========================
-- USER ACCESS TO COURSE
-- =========================
INSERT INTO user_courses (user_id, course_id, role)
VALUES (1, 1, 'student');


-- =========================
-- PAGES
-- =========================
INSERT INTO course_pages (course_id, slug, title, sort_order) VALUES
(1, 'intro_plan', 'Про урок і план', 1),
(1, 'setup_environment', 'Підготовка середовища', 2),
(1, 'kotlin_basics', 'Основи Kotlin', 3),
(1, 'compose_basics', 'Jetpack Compose', 4),
(1, 'network_json', 'REST API та JSON', 5),
(1, 'charts', 'Графіки', 6);


-- =========================
-- MENU
-- =========================
-- root nodes
INSERT INTO course_menu_nodes (course_id, title, sort_order) VALUES
(1, 'Вступ', 1),
(1, 'Розробка', 2);

-- children of "Вступ"
INSERT INTO course_menu_nodes (course_id, parent_id, title, page_id, sort_order) VALUES
(1, 1, 'Про урок і план', 1, 1),
(1, 1, 'Підготовка середовища', 2, 2);

-- children of "Розробка"
INSERT INTO course_menu_nodes (course_id, parent_id, title, page_id, sort_order) VALUES
(1, 2, 'Основи Kotlin', 3, 1),
(1, 2, 'Jetpack Compose', 4, 2),
(1, 2, 'REST API та JSON', 5, 3),
(1, 2, 'Графіки', 6, 4);


-- =========================
-- BLOCKS (page_blocks)
-- =========================

-- PAGE 1: intro_plan
INSERT INTO page_blocks (page_id, position, type, text) VALUES
(1, 1, 'h1', 'Повноцінний урок з Android-розробки на Kotlin'),
(1, 2, 'p', 'Ти навчишся Kotlin, Compose, API, медіа та архітектурі.');

INSERT INTO page_blocks (page_id, position, type, caption) VALUES
(1, 3, 'chart', 'Розподіл часу на теми');

-- chart meta
INSERT INTO chart_blocks (block_id, chart_type, title)
VALUES (3, 'bar', 'Орієнтовний розподіл часу');

-- labels
INSERT INTO chart_x_labels (block_id, idx, label) VALUES
(3, 0, 'Kotlin'),
(3, 1, 'Compose'),
(3, 2, 'API'),
(3, 3, 'Практика');

-- series
INSERT INTO chart_series (block_id, name, sort_order)
VALUES (3, 'Хвилини', 1);

-- values
INSERT INTO chart_series_values (series_id, x_idx, y_value) VALUES
(1, 0, 30),
(1, 1, 40),
(1, 2, 25),
(1, 3, 60);


-- =========================
-- PAGE 2: setup_environment
-- =========================
INSERT INTO page_blocks (page_id, position, type, text) VALUES
(2, 1, 'h1', 'Налаштування Android Studio'),
(2, 2, 'p', 'Встанови Android Studio, SDK та емулятор.');

INSERT INTO page_blocks (page_id, position, type, url, caption)
VALUES
(2, 3, 'img', 'https://upload.wikimedia.org/wikipedia/commons/thumb/9/95/Android_Studio_Icon_3.6.svg/960px-Android_Studio_Icon_3.6.svg.png', 'Android Studio');


-- =========================
-- PAGE 3: kotlin_basics
-- =========================
INSERT INTO page_blocks (page_id, position, type, text) VALUES
(3, 1, 'h1', 'Основи Kotlin'),
(3, 2, 'p', 'Kotlin — сучасна мова для Android.');

-- line chart
INSERT INTO page_blocks (page_id, position, type, caption)
VALUES (3, 3, 'chart', 'Крива навчання');

INSERT INTO chart_blocks (block_id, chart_type, title)
VALUES (6, 'line', 'Прогрес навчання');

INSERT INTO chart_x_labels VALUES
(6, 0, 'День 1'),
(6, 1, 'День 2'),
(6, 2, 'День 3'),
(6, 3, 'День 4');

INSERT INTO chart_series (block_id, name, sort_order)
VALUES (6, 'Впевненість', 1);

INSERT INTO chart_series_values VALUES
(2, 0, 2.0),
(2, 1, 3.5),
(2, 2, 5.0),
(2, 3, 6.5);


-- =========================
-- PAGE 4: compose_basics
-- =========================
INSERT INTO page_blocks (page_id, position, type, text) VALUES
(4, 1, 'h1', 'Jetpack Compose'),
(4, 2, 'p', 'UI як функція від state.');


-- =========================
-- PAGE 5: network_json
-- =========================
INSERT INTO page_blocks (page_id, position, type, text) VALUES
(5, 1, 'h1', 'REST API та JSON'),
(5, 2, 'p', 'Використовуй Ktor для запитів.');

INSERT INTO page_blocks (page_id, position, type, url, caption, autoplay, loop, show_controls)
VALUES
(5, 3, 'video',
 'https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/BigBuckBunny.mp4',
 'Демо-відео',
 0, 0, 1);


-- =========================
-- PAGE 6: charts
-- =========================
INSERT INTO page_blocks (page_id, position, type, text) VALUES
(6, 1, 'h1', 'Графіки у застосунку'),
(6, 2, 'p', 'Line та Bar графіки допомагають візуалізувати дані.');

COMMIT;
