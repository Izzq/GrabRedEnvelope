import requests
from bs4 import BeautifulSoup
import pandas as pd
import re
import time
import random
from datetime import datetime, timedelta

HEADERS = {"User-Agent": "Mozilla/5.0"}

# 日总龙虎榜买入卖出席位数据


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

def fetch_lhb_by_date(date_str):
    """抓取某一天龙虎榜数据"""
    url = f"https://data.10jqka.com.cn/ifmarket/lhbggxq/report/{date_str}/"
    resp = requests.get(url, headers=HEADERS, timeout=10)
    resp.encoding = resp.apparent_encoding

    soup = BeautifulSoup(resp.text, "lxml")
    stock_data = []
    dept_data = []

    stocks = soup.select("div.rightcol.fr .stockcont")
    for stock in stocks:
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

        stock_data.append([
            date_str, stockcode, description,
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
                dept_data.append([
                    date_str, stockcode, rank_type, name,
                    round(buy_amt, 2),
                    round(sell_amt, 2),
                    round(net_amt, 2)
                ])
    return stock_data, dept_data

# =====================
# 时间段设置（可修改）
start_date = "2026-01-10"
end_date = "2026-01-28"
date_list = pd.date_range(start=start_date, end=end_date).strftime("%Y-%m-%d").tolist()

all_stock_data = []
all_dept_data = []

for date_str in date_list:
    print(f"抓取日期: {date_str} ...")
    try:
        stock_data, dept_data = fetch_lhb_by_date(date_str)
        all_stock_data.extend(stock_data)
        all_dept_data.extend(dept_data)
    except Exception as e:
        print(f"⚠️ {date_str} 抓取失败: {e}")

    # 防爬：随机等待 2~5 秒
    wait_time = random.uniform(2, 5)
    print(f"等待 {wait_time:.2f} 秒...")
    time.sleep(wait_time)

# 保存 Excel
df_stock = pd.DataFrame(all_stock_data, columns=[
    "日期", "股票代码", "说明", "成交额(万)", "买入合计(万)", "卖出合计(万)", "净额(万)"
])
df_dept = pd.DataFrame(all_dept_data, columns=[
    "日期", "股票代码", "榜单类型", "营业部", "买入额(万)", "卖出额(万)", "净额(万)"
])

df_stock.to_excel("龙虎榜_股票汇总_多日.xlsx", index=False, engine='openpyxl', float_format="%.2f")
df_dept.to_excel("龙虎榜_营业部明细_多日.xlsx", index=False, engine='openpyxl', float_format="%.2f")

print(f"✅ 完成，股票汇总：{len(df_stock)}条，营业部明细：{len(df_dept)}条")
