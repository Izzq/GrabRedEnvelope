import requests
from bs4 import BeautifulSoup
import pandas as pd
import re

URL = "https://data.10jqka.com.cn/ifmarket/lhbggxq/report/2026-01-28/"
HEADERS = {"User-Agent": "Mozilla/5.0"}

# 请求网页
resp = requests.get(URL, headers=HEADERS, timeout=10)

# 自动识别编码，避免中文乱码
resp.encoding = resp.apparent_encoding

print("状态码:", resp.status_code)
print("响应头:", resp.headers)

soup = BeautifulSoup(resp.text, "lxml")

def parse_money(text):
    """金额字符串 → 浮点数(单位万)"""
    if not text:
        return 0.0
    text = text.replace(',', '').strip()
    m = re.search(r'-?[\d.]+', text)
    if not m:
        return 0.0
    num = float(m.group())
    if '亿' in text:
        num *= 10000  # 亿 → 万
    return num

all_stock_data = []
all_dept_data = []

# 获取右侧股票明细
stocks = soup.select("div.rightcol.fr .stockcont")

for i, stock in enumerate(stocks, start=1):
    # print(f"====== 股票 {i} ======")
    # print(stock.prettify()[:1000])  # 打印前1000字符检查
    # print("\n")

    stockcode = stock.get("stockcode")
    description = stock.select_one("p").get_text(strip=True) if stock.select_one("p") else ""

    # 成交额/买入/卖出/净额（从span里抓）
    summary = stock.select_one(".cell-cont.cjmx p")
    total_amount = buy_total = sell_total = net_total = 0.0
    if summary:
        spans = summary.find_all("span")
        if len(spans) >= 3:
            total_amount = parse_money(spans[0].get_text())
            buy_total = parse_money(spans[1].get_text())
            sell_total = parse_money(spans[2].get_text())
            net_total = buy_total - sell_total  # 净额自己算

    all_stock_data.append([
        stockcode, description,
        round(total_amount, 2),
        round(buy_total, 2),
        round(sell_total, 2),
        round(net_total, 2)
    ])

    # 营业部表格
    tables = stock.select("table.m-table")
    for table in tables:
        header_text = table.select_one("thead th").get_text(strip=True) if table.select_one("thead th") else ""
        if "买入金额最大的前5名营业部" in header_text:
            rank_type = "买入前5"
        elif "卖出金额最大的前5名营业部" in header_text:
            rank_type = "卖出前5"
        else:
            continue

        for tr in table.select("tbody tr"):
            tds = tr.find_all("td")
            if len(tds) < 4:
                continue
            name = tds[0].get_text(strip=True)
            buy_amt = parse_money(tds[1].get_text())
            sell_amt = parse_money(tds[2].get_text())
            net_amt = parse_money(tds[3].get_text())
            all_dept_data.append([
                stockcode, rank_type, name,
                round(buy_amt, 2),
                round(sell_amt, 2),
                round(net_amt, 2)
            ])

# 保存 Excel，浮点数保留两位小数
df_stock = pd.DataFrame(all_stock_data, columns=[
    "股票代码", "说明", "成交额(万)", "买入合计(万)", "卖出合计(万)", "净额(万)"
])
df_dept = pd.DataFrame(all_dept_data, columns=[
    "股票代码", "榜单类型", "营业部", "买入额(万)", "卖出额(万)", "净额(万)"
])

df_stock.to_excel("龙虎榜_股票汇总.xlsx", index=False, engine='openpyxl', float_format="%.2f")
df_dept.to_excel("龙虎榜_营业部明细.xlsx", index=False, engine='openpyxl', float_format="%.2f")

print(f"✅ 完成，股票汇总：{len(df_stock)}条，营业部明细：{len(df_dept)}条")
