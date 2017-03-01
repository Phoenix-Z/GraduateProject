# coding:utf-8
import requests
import re
import CopeWithJS


def translate(text):
    # 获取tk参数值
    tk_val = CopeWithJS.get_tk(text)
    # 设置GET请求参数
    values = {'client': 't',
              'sl': 'en',
              'tl': 'zh-CN',
              'hl': 'z-CN',
              'dt': ['at', 'bd', 'ex', 'ld', 'md', 'qca', 'rw', 'rm', 'ss', 't'],
              'ie': 'UTF-8',
              'oe': 'UTF-8',
              'otf': '1',
              'ssel': '0',
              'tsel': '0',
              'kc': '1',
              'tk': tk_val,
              'q': text}
    url = 'http://translate.google.cn/translate_a/single'
    header = {'User-Agent': "Mozilla/5.0 (Windows NT 6.1; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/52.0.2743.116 Safari/537.36" }
    response = requests.get(url, params=values, headers=header)

    fragments = len(filter(lambda x: x, re.split('[?.!]', text.strip())))
    result, i = '', 0
    for part in response.text.split("\",", fragments * 2 - 1):
        if i % 2 == 0:
            begin = part.rindex("\"")
            result += part[begin + 1:]
        i += 1
    return result

if __name__ == "__main__":
    # text_1 原文
    # text_1=open('c:\\text.txt','r').read()
    text_list = ['Hello, my name is Derek. Nice to meet you! ', 'hello world. Try again',
                 'How are you? Fine, Thank you. And you ?']
    for text_1 in text_list:
        print 'The input text: %s' % text_1

        text_2 = translate(text_1)
        print 'The output text: %s' % text_2

