class DeptRecord:
    def __init__(self, date, code, rank_type, name, buy, sell, net):
        self.date = date
        self.code = code
        self.rank_type = rank_type
        self.name = name
        self.buy = buy
        self.sell = sell
        self.net = net

    def to_tuple(self):
        return (
            self.date,
            self.code,
            self.rank_type,
            self.name,
            self.buy,
            self.sell,
            self.net,
        )
