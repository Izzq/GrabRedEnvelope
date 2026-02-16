import re

class MoneyUtils:

    @staticmethod
    def parse_wan(text):
        """原有 parse_wan"""
        if not text:
            return 0.0
        text = text.strip().replace(',', '')
        num = float(re.findall(r'-?[\d.]+', text)[0])
        if '亿' in text:
            num *= 10000
        return num

    @staticmethod
    def parse_str_with_unit(match_obj):
        """传入 re.Match 对象，返回单位为万的浮点数"""
        num = float(match_obj.group(1))
        unit = match_obj.group(2)
        if unit == '亿':
            num *= 10000
        elif unit == '万' or unit is None:
            num *= 1
        return num

