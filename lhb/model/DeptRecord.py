class DeptRecord:
    """
    龙虎榜营业部（或机构）明细记录

    一条记录对应：
    - 某一天
    - 某只股票
    - 某个上榜原因（rid）
    - 某个榜单类型（买入前5 / 卖出前5）
    - 某个营业部或机构
    """

    def __init__(self, date, code, rid, tag, rank_type, name, buy, sell, net):
        """
        :param date: 上榜日期（YYYY-MM-DD）
        :param code: 股票代码（如 000001）
        :param rid: 龙虎榜唯一标识（用于关联基础表、汇总表）
        :param tag: 榜单标签（如：当日 / 3日）
        :param rank_type: 排名类型（"买入前5" 或 "卖出前5"）
        :param name: 营业部名称（如：机构专用 / 某某证券XX营业部）
        :param buy: 买入金额（单位：万元）
        :param sell: 卖出金额（单位：万元）
        :param net: 净买入金额（buy - sell，单位：万元）
        """
        self.date = date
        self.code = code
        self.rid = rid
        self.tag = tag
        self.rank_type = rank_type
        self.name = name
        self.buy = buy
        self.sell = sell
        self.net = net

    def to_tuple(self):
        """
        转换为元组
        - 方便批量插入数据库
        - 或直接构造 pandas.DataFrame
        """
        return (
            self.date,
            self.code,
            self.rid,
            self.tag,
            self.rank_type,
            self.name,
            self.buy,
            self.sell,
            self.net,
        )
