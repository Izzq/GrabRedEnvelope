import time
import random

class SpiderUtils:
    """
    爬虫工具类（防爬、限速）
    """

    @staticmethod
    def sleep(min_sec: float = 2.0, max_sec: float = 5.0):
        """
        随机休眠，防止被封 IP
        """
        sec = random.uniform(min_sec, max_sec)
        print(f"⏳ 休眠 {sec:.2f} 秒")
        time.sleep(sec)
