class StockRecord:
    """
    龙虎榜股票汇总实体
    """

    def __init__(
            self,
            date: str,
            code: str,
            rid: str,
            desc: str,
            total: float,
            buy: float,
            sell: float,
            net: float
    ):
        self.date = date
        self.code = code
        self.rid = rid
        self.desc = desc
        self.total = total
        self.buy = buy
        self.sell = sell
        self.net = net

    def to_tuple(self):
        return (
            self.date,
            self.code,
            self.rid,
            self.desc,
            self.total,
            self.buy,
            self.sell,
            self.net,
        )
