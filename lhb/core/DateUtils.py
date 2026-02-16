from datetime import datetime, timedelta

class DateUtils:
    """
    日期工具类
    """

    @staticmethod
    def range(start: str, end: str, fmt="%Y-%m-%d"):
        s = datetime.strptime(start, fmt)
        e = datetime.strptime(end, fmt)
        if e < s:
            return
        for i in range((e - s).days + 1):
            yield (s + timedelta(days=i)).strftime(fmt)

    @staticmethod
    def shift(date_str: str, days: int, fmt="%Y-%m-%d") -> str:
        """
        日期偏移
        """
        d = datetime.strptime(date_str, fmt)
        return (d + timedelta(days=days)).strftime(fmt)

    @staticmethod
    def today(fmt="%Y-%m-%d") -> str:
        return datetime.now().strftime(fmt)
