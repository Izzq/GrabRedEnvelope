import re

class MoneyUtils:
    """
    金额解析工具
    """

    @staticmethod
    def parse_wan(text: str) -> float:
        """
        金额字符串 → 万
        """
        if not text:
            return 0.0

        text = text.replace(',', '').strip()
        m = re.search(r'-?[\d.]+', text)
        if not m:
            return 0.0

        num = float(m.group())
        if '亿' in text:
            return num * 10000
        if '万' in text:
            return num
        return num / 10000

    @staticmethod
    def parse_yuan(text: str) -> float:
        """
        金额字符串 → 元
        """
        return MoneyUtils.parse_wan(text) * 10000
