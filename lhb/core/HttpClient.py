import requests

class HttpClient:
    DEFAULT_HEADERS = {
        "User-Agent": "Mozilla/5.0 (Windows NT 10.0; Win64; x64)"
    }

    @staticmethod
    def get(url, headers=None, timeout=10, encoding=None):
        h = HttpClient.DEFAULT_HEADERS.copy()
        if headers:
            h.update(headers)

        resp = requests.get(url, headers=h, timeout=timeout)
        if encoding:
            resp.encoding = encoding
        else:
            resp.encoding = resp.apparent_encoding
        return resp.text
