class LhBasicStock:
    """
    龙虎榜基础表格记录实体
    用于存储每行基础数据（标签/代码/名称/rid/现价/涨跌幅）
    """

    def __init__(
            self,
            date: str,
            tag: str,
            code: str,
            rid: str,
            name: str,
            current_price: float,
            change_percent: str,
    ):
        self.date = date
        self.tag = tag
        self.code = code
        self.rid = rid
        self.name = name
        self.current_price = current_price
        self.change_percent = change_percent

    def to_tuple(self):
        """
        转为元组，方便插入数据库或 DataFrame
        """
        return (
            self.date,
            self.tag,
            self.code,
            self.rid,
            self.name,
            self.current_price,
            self.change_percent,
        )

    def __repr__(self):
        return (
            f"StockBaseRecord(date={self.date}, tag={self.tag}, code={self.code}, "
            f"rid={self.rid}, name={self.name}, current_price={self.current_price}, "
            f"change_percent={self.change_percent}"
        )
