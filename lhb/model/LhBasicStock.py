class LhBasicStock:
    """
    龙虎榜基础表格记录实体

    说明：
    - 存储龙虎榜“基础表格”每行数据
    - 对应每只股票在某日期的上榜情况
    - 主要用于：
        1. 构建基础 records_map（rid -> record）
        2. 提供股票现价、涨跌幅等基础信息
        3. 后续解析龙虎榜详细数据时关联使用
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
        """
        :param date: 上榜日期，例如 '2026-02-17'
        :param tag: 榜单标签，例如 '当日'、'3日'
        :param code: 股票代码，例如 '000001'
        :param rid: 龙虎榜唯一标识，用于关联详细数据
        :param name: 股票名称
        :param current_price: 当前价格（元）
        :param change_percent: 涨跌幅（带 % 符号的字符串）
        """
        self.date = date
        self.tag = tag
        self.code = code
        self.rid = rid
        self.name = name
        self.current_price = current_price
        self.change_percent = change_percent

    def to_tuple(self):
        """
        转换为元组
        - 用于数据库插入或 DataFrame 构造
        :return: tuple(date, tag, code, rid, name, current_price, change_percent)
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
        """
        调试打印显示
        """
        return (
            f"LhBasicStock(date={self.date}, tag={self.tag}, code={self.code}, "
            f"rid={self.rid}, name={self.name}, current_price={self.current_price}, "
            f"change_percent={self.change_percent})"
        )
