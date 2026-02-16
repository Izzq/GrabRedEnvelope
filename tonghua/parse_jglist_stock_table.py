import requests
from bs4 import BeautifulSoup
import re
import pandas as pd
import time
import random


# 日机构龙虎榜数据


# =======================
# 时间段设置
start_date = "2026-02-10"
end_date = "2026-02-13"
date_list = pd.date_range(start=start_date, end=end_date).strftime("%Y-%m-%d").tolist()

# =======================
# 在线抓取配置
HEADERS = {"User-Agent": "Mozilla/5.0"}
URL_TEMPLATE = "https://data.10jqka.com.cn/ifmarket/lhbtable/report/{date}/tab/jgcy/field/STOCKCODE/sort/asc/"

# =======================
# 工具函数
def parse_money(text):
    """解析金额，返回单位：元"""
    text = text.strip().replace(',', '')
    if not text:
        return 0.0
    num = float(re.findall(r'-?[\d.]+', text)[0])
    if '亿' in text:
        return num * 1e8
    if '万' in text:
        return num * 1e4
    return num

def parse_pct(text):
    return float(text.replace('%', '').strip())

def fetch_stock_data(date_str):
    url = URL_TEMPLATE.format(date=date_str)
    resp = requests.get(url, headers=HEADERS, timeout=10)
    resp.encoding = resp.apparent_encoding
    soup = BeautifulSoup(resp.text, "lxml")

    rows = soup.select("div.twrap > table.m-table > tbody > tr")
    data = []

    for tr in rows:
        tds = tr.find_all("td")
        if len(tds) < 7:
            continue
        tag = tds[0].get_text(strip=True)
        code = tds[1].get_text(strip=True)
        name = tds[2].get_text(strip=True)
        price = float(tds[3].get_text(strip=True))
        change_pct = parse_pct(tds[4].get_text(strip=True))
        turnover = parse_money(tds[5].get_text(strip=True)) / 1e4  # 元 → 万
        net_buy = parse_money(tds[6].get_text(strip=True)) / 1e4    # 元 → 万

        data.append([date_str, tag, code, name, price, change_pct,
                     round(turnover, 2), round(net_buy, 2)])

    return data

# =======================
# 多日抓取
all_data = []

for date_str in date_list:
    print(f"抓取日期: {date_str} ...")
    try:
        daily_data = fetch_stock_data(date_str)
        all_data.extend(daily_data)
        print(f"  ✅ 完成，抓取 {len(daily_data)} 条")
    except Exception as e:
        print(f"  ⚠️ {date_str} 抓取失败: {e}")

    # 防爬：随机等待 2~5 秒
    wait_time = random.uniform(2, 5)
    print(f"  等待 {wait_time:.2f} 秒...")
    time.sleep(wait_time)

# =======================
# 保存 Excel / CSV
df = pd.DataFrame(all_data, columns=[
    "日期", "标签", "代码", "名称", "现价", "涨跌幅%", "成交金额(万)", "净买入额(万)"
])
df.to_excel("股票数据_多日.xlsx", index=False, engine='openpyxl')
df.to_csv("股票数据_多日.csv", index=False, encoding="utf-8-sig")

print(f"\n✅ 完成，总计抓取 {len(df)} 条数据")
print("文件已生成：股票数据_机构_多日.xlsx / 股票数据_机构_多日.csv")
