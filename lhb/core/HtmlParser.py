from bs4 import BeautifulSoup

class HtmlParser:

    @staticmethod
    def parse(html: str):
        return BeautifulSoup(html, "lxml")
