import requests
from bs4 import BeautifulSoup
import pandas as pd

# 定义获取龙虎榜数据的函数
def fetch_lhb_data(date):
    # 同花顺龙虎榜页面URL，date是查询的日期（格式：YYYYMMDD）
    url = f"http://data.10jqka.com.cn/market/longhu/{date}/"

    # 发起请求，获取页面内容
    headers = {
        "User-Agent": "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/91.0.4472.124 Safari/537.36"
    }
    response = requests.get(url, headers=headers)

    if response.status_code != 200:
        print(f"请求失败，状态码：{response.status_code}")
        return None

    # 使用BeautifulSoup解析页面内容
    soup = BeautifulSoup(response.content, 'html.parser')

    # 找到表格
    table = soup.find("table", class_="table_data")

    if not table:
        print("未找到龙虎榜数据表格")
        return None

    # 解析表格行数据
    rows = table.find_all("tr")[1:]  # 跳过表头

    data = []
    for row in rows:
        cols = row.find_all("td")
        cols = [col.get_text(strip=True) for col in cols]

        if len(cols) == 7:  # 根据数据的列数进行判断
            stock_code = cols[1]
            stock_name = cols[2]
            price = cols[3]
            change = cols[4]
            buy_amount = cols[5]
            sell_amount = cols[6]

            # 数据清洗：去除万/亿单位，转换为数值
            buy_amount = convert_to_number(buy_amount)
            sell_amount = convert_to_number(sell_amount)
            net_buy = buy_amount - sell_amount  # 净买入 = 买入金额 - 卖出金额

            # 添加到数据列表
            data.append([stock_code, stock_name, price, change, buy_amount, sell_amount, net_buy])

    # 将数据转换为DataFrame格式
    columns = ["股票代码", "股票名称", "价格", "涨跌幅", "买入金额", "卖出金额", "净买入"]
    df = pd.DataFrame(data, columns=columns)

    return df

# 数据清洗：去除单位并转换为数值
def convert_to_number(value):
    if '亿' in value:
        return float(value.replace('亿', '').strip()) * 1e8
    elif '万' in value:
        return float(value.replace('万', '').strip()) * 1e4
    else:
        return float(value.strip())  # 返回普通数字

# 示例：获取某日的龙虎榜数据
date = "20260130"  # 你可以替换为你需要查询的日期
lhb_data = fetch_lhb_data(date)

if lhb_data is not None:
    print(lhb_data)

