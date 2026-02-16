class StockRecord:
    """
    龙虎榜股票汇总实体

    用途：
    - 存储每只上榜股票的详细汇总信息
    - 包含基础表格信息和龙虎榜明细数据
    - 可直接用于数据库入库或数据分析

    字段说明：
    - date: 上榜日期，例如 '2026-02-17'
    - code: 股票代码，例如 '000001'
    - rid: 龙虎榜唯一标识
    - tag: 榜单标签，例如 '当日', '5日'
    - desc: 股票说明/提示信息
    - current_price: 当前股价（元）
    - change_percent: 涨跌幅（百分比数值）
    - total: 成交总额（万元）
    - buy: 买入总额（万元）
    - sell: 卖出总额（万元）
    - net: 净买入额（万元），buy - sell
    - inst_net: 机构净买入额（仅“买入前5 + 卖出前5”里的机构总和）
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
            net: float,
            inst_net: float,
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
        self.inst_net = inst_net

    def to_tuple(self):
        """
        转换为元组，方便数据库插入或 DataFrame 构造
        :return: tuple(date, code, rid, tag, desc, current_price, change_percent, total, buy, sell, net, inst_net)
        """
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
            self.inst_net,
        )

    def __repr__(self):
        """
        调试打印显示
        """
        return (
            f"StockRecord(date={self.date}, code={self.code}, rid={self.rid}, tag={self.tag}, "
            f"desc={self.desc}, current_price={self.current_price}, change_percent={self.change_percent}, "
            f"total={self.total}, buy={self.buy}, sell={self.sell}, net={self.net}, inst_net={self.inst_net})"
        )
