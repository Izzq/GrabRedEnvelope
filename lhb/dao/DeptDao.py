class DeptDao:

    CREATE_SQL = """
    CREATE TABLE IF NOT EXISTS lhb_dept (
        id INTEGER PRIMARY KEY AUTOINCREMENT,
        trade_date TEXT,
        stock_code TEXT,
        rank_type TEXT,
        dept_name TEXT,
        buy_amount REAL,
        sell_amount REAL,
        net_amount REAL
    )
    """

    INSERT_SQL = """
    INSERT INTO lhb_dept
    (trade_date, stock_code, rank_type, dept_name, buy_amount, sell_amount, net_amount)
    VALUES (?, ?, ?, ?, ?, ?, ?)
    """

    def __init__(self, db):
        self.db = db
        self.db.execute(self.CREATE_SQL)

    def batch_insert(self, records):
        self.db.executemany(
            self.INSERT_SQL,
            [r.to_tuple() for r in records]
        )
