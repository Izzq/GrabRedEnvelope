import sqlite3

class DBManager:

    def __init__(self, db_path: str):
        self.conn = sqlite3.connect(db_path)
        self.cursor = self.conn.cursor()

    def execute(self, sql: str, params=()):
        self.cursor.execute(sql, params)
        self.conn.commit()

    def executemany(self, sql: str, params_list: list):
        self.cursor.executemany(sql, params_list)
        self.conn.commit()

    def close(self):
        self.conn.close()
