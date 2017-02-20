import requests
from bs4 import BeautifulSoup

r = requests.get('http://docs.python-requests.org/en/master/')
parser = BeautifulSoup(r.content, 'html.parser')
body = parser.body
print body
