import requests
from bs4 import BeautifulSoup

html_page = requests.get("https://movie.douban.com/top250?start={page}&filter=&type=")
s = BeautifulSoup(html_page, 'html.parser')
print s.find('li', attrs={'class': 'title'})

