class StockRecord:
    def __init__(self, date, code, desc, total, buy, sell, net):
        self.date = date
        self.code = code
        self.desc = desc
        self.total = total
        self.buy = buy
        self.sell = sell
        self.net = net

    def to_tuple(self):
        return (
            self.date,
            self.code,
            self.desc,
            self.total,
            self.buy,
            self.sell,
            self.net,
        )
