import requests
from bs4 import BeautifulSoup
import pandas as pd
from datetime import datetime, timedelta

# 支持抓取多日
start_date = "2026-01-28"
end_date = "2026-01-28"

def daterange(start_date, end_date):
    start = datetime.strptime(start_date, "%Y-%m-%d")
    end = datetime.strptime(end_date, "%Y-%m-%d")
    for n in range((end - start).days + 1):
        yield (start + timedelta(n)).strftime("%Y-%m-%d")

all_data = []

for date_str in daterange(start_date, end_date):
    url = f"https://data.10jqka.com.cn/ifmarket/lhbggxq/report/{date_str}/"
    print(f"抓取日期: {date_str} -> {url}")

    headers = {
        "User-Agent": "Mozilla/5.0 (Windows NT 10.0; Win64; x64)"
    }
    response = requests.get(url, headers=headers)
    response.encoding = 'gbk'
    html = response.text

    soup = BeautifulSoup(html, "html.parser")
    twrap_div = soup.find("div", class_="twrap")
    if not twrap_div:
        print(f"{date_str} 没有找到数据")
        continue

    table = twrap_div.find("table", class_="m-table")
    tbody = table.find("tbody")

    for tr in tbody.find_all("tr"):
        tds = tr.find_all("td")
        if len(tds) < 7:
            continue

        period = tds[0].get_text(strip=True)
        code = tds[1].get_text(strip=True)
        name = tds[2].get_text(strip=True)
        current_price = tds[3].get_text(strip=True)
        change_percent = tds[4].get_text(strip=True)
        amount = tds[5].get_text(strip=True)
        net_buy = tds[6].get_text(strip=True)

        all_data.append({
            "日期": date_str,
            "周期": period,
            "代码": code,
            "名称": name,
            "现价": current_price,
            "涨跌幅": change_percent,
            "成交金额": amount,
            "净买入额": net_buy
        })

# 转换为 DataFrame
df = pd.DataFrame(all_data)

# 输出 Excel 文件
output_file = "龙虎榜多日数据.xlsx"
df.to_excel(output_file, index=False)
print(f"已生成表格: {output_file}")
