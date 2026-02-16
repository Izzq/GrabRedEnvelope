class StockRecord:
    """
    龙虎榜股票汇总实体
    """

    def __init__(
            self,
            date: str,
            code: str,
            rid: str,
            tag: str,
            desc: str,
            current_price: float,
            change_percent: float,
            total: float,
            buy: float,
            sell: float,
            net: float
    ):
        self.date = date
        self.code = code
        self.rid = rid
        self.tag = tag
        self.desc = desc
        self.current_price = current_price
        self.change_percent = change_percent
        self.total = total
        self.buy = buy
        self.sell = sell
        self.net = net

    def to_tuple(self):
        return (
            self.date,
            self.code,
            self.rid,
            self.tag,
            self.desc,
            self.current_price,
            self.change_percent,
            self.total,
            self.buy,
            self.sell,
            self.net,
        )
