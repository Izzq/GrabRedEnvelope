from core.HttpClient import HttpClient
from core.HtmlParser import HtmlParser
from core.MoneyUtils import MoneyUtils
from model.StockRecord import StockRecord
from model.DeptRecord import DeptRecord

class LhbSpiderService:

    def fetch_by_date(self, date_str):
        url = f"https://data.10jqka.com.cn/ifmarket/lhbggxq/report/{date_str}/"
        html = HttpClient.get(url)
        soup = HtmlParser.parse(html)

        stocks = soup.select("div.rightcol.fr .stockcont")

        stock_records = []
        dept_records = []

        for stock in stocks:
            code = stock.get("stockcode")
            desc = stock.select_one("p").get_text(strip=True)

            summary = stock.select_one(".cell-cont.cjmx p span")
            spans = stock.select(".cell-cont.cjmx p span")

            if len(spans) >= 3:
                total = MoneyUtils.parse_wan(spans[0].text)
                buy = MoneyUtils.parse_wan(spans[1].text)
                sell = MoneyUtils.parse_wan(spans[2].text)
                net = buy - sell
            else:
                continue

            stock_records.append(
                StockRecord(date_str, code, desc, total, buy, sell, net)
            )

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
                            date_str,
                            code,
                            rank,
                            tds[0].text.strip(),
                            MoneyUtils.parse_wan(tds[1].text),
                            MoneyUtils.parse_wan(tds[2].text),
                            MoneyUtils.parse_wan(tds[3].text),
                        )
                    )

        return stock_records, dept_records
