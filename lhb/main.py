
from core.DateUtils import DateUtils
from core.SpiderUtils import SpiderUtils
from dao.DBManager import DBManager
from dao.StockDao import StockDao
from dao.DeptDao import DeptDao
from service.LhbSpiderService import LhbSpiderService

if __name__ == "__main__":
    db = DBManager("lhb.db")
    stock_dao = StockDao(db)
    dept_dao = DeptDao(db)

    service = LhbSpiderService()

    for date_str in DateUtils.range("2026-02-12", "2026-02-13"):
        print("抓取日期:", date_str)
        stocks, depts = service.fetch_by_date(date_str)
        stock_dao.batch_insert(stocks)
        dept_dao.batch_insert(depts)
        SpiderUtils.sleep()

    db.close()
    print("✅ 完成")
