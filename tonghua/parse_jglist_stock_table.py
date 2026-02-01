import requests
from bs4 import BeautifulSoup
import re
import pandas as pd

# ========= 方式选择 =========
USE_ONLINE = True   # True=在线抓取  False=读取本地HTML

URL = "https://data.10jqka.com.cn/ifmarket/lhbtable/report/2026-01-28/tab/jgcy/field/STOCKCODE/sort/asc/"  # ← 换成真实网址
LOCAL_FILE = "page.html"

# ========= 获取HTML =========
if USE_ONLINE:
    headers = {"User-Agent": "Mozilla/5.0"}
    html = requests.get(URL, headers=headers, timeout=10).text
else:
    with open(LOCAL_FILE, encoding="utf-8") as f:
        html = f.read()

soup = BeautifulSoup(html, "lxml")

# ========= 工具函数 =========
def parse_money(text):
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

# ========= 解析表格 =========
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
    turnover = parse_money(tds[5].get_text(strip=True))
    net_buy = parse_money(tds[6].get_text(strip=True))

    data.append([tag, code, name, price, change_pct, turnover, net_buy])

# ========= 保存结果 =========
df = pd.DataFrame(data, columns=[
    "标签", "代码", "名称", "现价", "涨跌幅%", "成交金额(元)", "净买入额(元)"
])

df.to_excel("股票数据.xlsx", index=False)
df.to_csv("股票数据.csv", index=False, encoding="utf-8-sig")

print("✅ 解析完成，共", len(df), "条数据")
print("文件已生成：股票数据.xlsx / 股票数据.csv")
