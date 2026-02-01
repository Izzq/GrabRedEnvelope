import time
import re
import pandas as pd
from bs4 import BeautifulSoup
from selenium import webdriver
from selenium.webdriver.common.by import By
from selenium.webdriver.chrome.service import Service
from webdriver_manager.chrome import ChromeDriverManager

URL = "https://data.10jqka.com.cn/ifmarket/lhbtable/report/2026-01-27/tab/jgcy/field/STOCKCODE/sort/asc/"

# ========= 启动浏览器 =========
options = webdriver.ChromeOptions()
options.add_argument("--start-maximized")
options.add_argument("--disable-blink-features=AutomationControlled")

driver = webdriver.Chrome(service=Service(ChromeDriverManager().install()), options=options)
driver.get(URL)

time.sleep(5)  # 等页面加载

# ========= 工具函数 =========
def clean_num(x):
    if not x:
        return 0.0
    x = x.replace(',', '').strip()
    m = re.search(r'-?[\d.]+', x)
    return float(m.group()) if m else 0.0

# ========= 获取左侧股票列表 =========
stocks = driver.find_elements(By.CSS_SELECTOR, ".twrap .stock")
print(f"发现股票数量：{len(stocks)}")

all_data = []

for i in range(len(stocks)):
    try:
        stocks = driver.find_elements(By.CSS_SELECTOR, ".twrap .stock")
        stock = stocks[i]

        stock_name = stock.text.strip()
        print(f"正在抓取：{stock_name}")

        driver.execute_script("arguments[0].click();", stock)
        time.sleep(2)

        soup = BeautifulSoup(driver.page_source, "lxml")

        # ===== 顶部统计 =====
        summary_p = soup.select_one(".cell-cont.cjmx > p")
        if summary_p:
            summary_text = summary_p.get_text(" ", strip=True)
        else:
            summary_text = ""

        def get_val(pattern):
            m = re.search(pattern, summary_text)
            return clean_num(m.group(1)) if m else 0.0

        total_amount = get_val(r"成交额：([\d.]+)")
        buy_total = get_val(r"合计买入：([\d.]+)")
        sell_total = get_val(r"合计卖出：([\d.]+)")
        net_total = get_val(r"净额：(-?[\d.]+)")

        # ===== 买卖明细表 =====
        tables = soup.select("table.m-table")
        for table in tables:
            rows = table.select("tbody tr")
            for tr in rows:
                tds = tr.find_all("td")
                if len(tds) < 5:
                    continue

                dept = tds[0].get_text(strip=True)
                buy_amt = clean_num(tds[1].get_text())
                buy_pct = clean_num(tds[2].get_text())
                sell_amt = clean_num(tds[3].get_text())
                sell_pct = clean_num(tds[4].get_text())
                net_amt = buy_amt - sell_amt

                all_data.append([
                    stock_name,
                    total_amount,
                    buy_total,
                    sell_total,
                    net_total,
                    dept,
                    buy_amt,
                    buy_pct,
                    sell_amt,
                    sell_pct,
                    net_amt
                ])

    except Exception as e:
        print("跳过异常股票:", e)

# ========= 保存 =========
df = pd.DataFrame(all_data, columns=[
    "股票", "成交额", "合计买入", "合计卖出", "净额",
    "营业部", "买入额", "买入占比%", "卖出额", "卖出占比%", "营业部净额"
])

df.to_excel("龙虎榜明细.xlsx", index=False)

print("✅ 抓取完成，共", len(df), "条数据")
driver.quit()
