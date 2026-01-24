import sqlite3
from pathlib import Path

DB_PATH = "data.db"
SQL_PATHES = ["db_definition.sql", "seed_data.sql"]

def main():
    for sql_path in SQL_PATHES:
        # Перевіряємо, що SQL файл існує
        if not Path(sql_path).exists():
            raise FileNotFoundError(f"Не знайдено файл: {sql_path}")

        # Читаємо SQL-скрипт
        with open(sql_path, "r", encoding="utf-8") as f:
            sql_script = f.read()

        # Створюємо (або відкриваємо) базу
        conn = sqlite3.connect(DB_PATH)

        try:
            conn.executescript(sql_script)
            conn.commit()
            print(f"✅ Скрипт '{sql_path}' успішно виконано на БД '{DB_PATH}'.")
        except Exception as e:
            print("❌ Помилка при виконанні SQL:")
            print(e)
        finally:
            conn.close()


if __name__ == "__main__":
    main()
