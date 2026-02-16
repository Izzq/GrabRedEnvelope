class StockDao:

    CREATE_SQL = """
    CREATE TABLE IF NOT EXISTS lhb_stock (
        id INTEGER PRIMARY KEY AUTOINCREMENT,
        trade_date TEXT,
        stock_code TEXT,
        rid TEXT,
        tag TEXT,
        description TEXT,
        current_price REAL,
        change_percent REAL,
        total_amount REAL,
        buy_amount REAL,
        sell_amount REAL,
        net_amount REAL,
        UNIQUE(trade_date, rid)
    )
    """

    INSERT_SQL = """
    INSERT OR IGNORE INTO lhb_stock
    (trade_date, stock_code, rid, tag, description, current_price, change_percent, total_amount, buy_amount, sell_amount, net_amount)
    VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
    """

    def __init__(self, db):
        self.db = db
        self.db.execute(self.CREATE_SQL)

    def batch_insert(self, records):
        self.db.executemany(
            self.INSERT_SQL,
            [r.to_tuple() for r in records]
        )
