import re
from core.HttpClient import HttpClient
from core.HtmlParser import HtmlParser
from core.MoneyUtils import MoneyUtils
from model.StockRecord import StockRecord
from model.DeptRecord import DeptRecord
from model.LhBasicStock import LhBasicStock


class LhbSpiderService:

    def fetch_by_date(self, date_str):
        url = f"https://data.10jqka.com.cn/ifmarket/lhbggxq/report/{date_str}/"
        html = HttpClient.get(url)
        soup = HtmlParser.parse(html)

        records = []         # 基础表格记录
        stock_records = []   # 龙虎榜股票记录
        dept_records = []    # 营业部买卖记录
        records_map = {}     # rid -> StockBaseRecord

        # =========================
        # 解析基础表格数据
        twrap_div = soup.find("div", class_="twrap")
        if twrap_div:
            table = twrap_div.find("table", class_="m-table")
            tbody = table.find("tbody")
            for tr in tbody.find_all("tr"):
                tds = tr.find_all("td")
                if len(tds) < 4:
                    continue

                tag = tds[0].get_text(strip=True)
                code = tds[1].get_text(strip=True)
                a_tag = tds[2].find("a")
                name = a_tag.get_text(strip=True) if a_tag else tds[2].get_text(strip=True)
                rid = a_tag.get("rid") if a_tag else ""
                current_price = float(tds[3].get_text(strip=True))
                change_percent = tds[4].get_text(strip=True)

                record = LhBasicStock(
                    date=date_str,
                    tag=tag,
                    code=code,
                    rid=rid,
                    name=name,
                    current_price=round(current_price, 2),
                    change_percent=change_percent,
                )

                # 添加到列表和 Map
                records.append(record)
                if rid:
                    records_map[rid] = record
        else:
            print(f"{date_str} twrap 没有找到数据")


        # =========================
        # 解析右侧龙虎榜详细数据
        stocks = soup.select("div.rightcol.fr .stockcont")
        for stock in stocks:
            code = stock.get("stockcode")
            rid = stock.get("rid")
            desc_tag = stock.select_one("p")
            desc = desc_tag.get_text(strip=True) if desc_tag else ""

            # 从 records_map 获取基础信息
            base_record = records_map.get(rid)
            if not base_record:
                continue
            tag = base_record.tag
            current_price = base_record.current_price
            change_percent = base_record.change_percent

            # =========================
            # 排除3日标签龙湖榜数据
            if tag == "3日":
                continue   # 跳过当前循环，进入下一次 for

            # =========================
            # 解析前5营业部买入/卖出
            tables = stock.select("table.m-table")
            for table in tables:
                th = table.select_one("thead th")
                if not th:
                    continue

                header = th.text
                if "买入金额最大的前5名营业部" in header:
                    rank = "买入前5"
                elif "卖出金额最大的前5名营业部" in header:
                    rank = "卖出前5"
                else:
                    continue

                for tr in table.select("tbody tr"):
                    tds = tr.find_all("td")
                    if len(tds) < 4:
                        continue

                    dept_records.append(
                        DeptRecord(
                            date=date_str,
                            code=code,
                            rid=rid,
                            tag=tag,
                            rank_type=rank,
                            name=tds[0].text.strip(),
                            buy=round(MoneyUtils.parse_wan(tds[1].text), 2),
                            sell=round(MoneyUtils.parse_wan(tds[2].text), 2),
                            net=round(MoneyUtils.parse_wan(tds[3].text), 2),
                        )
                    )


            # =========================
            # 解析成交额/买入/卖出/净额（支持 span 外单位）

            # TODO: 注意此处解释单位可能包含亿、万，并且单位不连在一起
            # 解析成交额/买入/卖出/净额（支持 span 外单位）
            p_tag = stock.select_one(".cell-cont.cjmx p")
            total = buy = sell = net = 0.0
            if p_tag:
                text = p_tag.get_text(strip=True)
                total_match = re.search(r"成交额：([\d.]+)(亿|万)?元", text)
                buy_match = re.search(r"合计买入：([\d.]+)(亿|万)?", text)
                sell_match = re.search(r"合计卖出：([\d.]+)(亿|万)?", text)
                net_match = re.search(r"净额：([\d.-]+)(亿|万)?", text)

                total = MoneyUtils.parse_str_with_unit(total_match)
                buy = MoneyUtils.parse_str_with_unit(buy_match)
                sell = MoneyUtils.parse_str_with_unit(sell_match)
                net = MoneyUtils.parse_str_with_unit(net_match)

            # 添加 StockRecord
            stock_records.append(
                StockRecord(
                    date=date_str,
                    code=code,
                    rid=rid,
                    tag=tag,
                    desc=desc,
                    current_price=round(current_price, 2),
                    change_percent=change_percent,
                    total=round(total, 2),
                    buy=round(buy, 2),
                    sell=round(sell, 2),
                    net=round(net, 2)
                )
            )
        # =========================
        return stock_records, dept_records
