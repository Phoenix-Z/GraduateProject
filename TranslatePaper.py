# coding:utf-8
import requests
import re
import CopeWithJS
import win32clipboard
import sys
import pythoncom

__author__ = "Pheonix-Z"
__date__ = "2017/03/01"

# 写入文档时需要设置编码
reload(sys)
sys.setdefaultencoding('utf8')


def translate(text):
    # 获取tk参数值
    tk_val = CopeWithJS.get_tk(text)
    # 设置GET请求参数，其中q是我们需要翻译的英文句子
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
    # 谷歌翻译的url
    url = 'http://translate.google.cn/translate_a/single'
    # 伪装成浏览器的行为，该请求头是通过wireshark截获数据包中得到的真实请求头
    header = {'User-Agent': "Mozilla/5.0 (Windows NT 6.1; Win64; x64) AppleWebKit/537.36 (KHTML,"
                            " like Gecko) Chrome/52.0.2743.116 Safari/537.36"}
    # 使用requests模块访问url，并获取响应
    response = requests.get(url, params=values, headers=header)

    # 这部分是对谷歌翻译的返回结果进行处理，以得到我们需要的翻译结果
    # 谷歌翻译是先对提交的大段文字分成一个个句子(以".","?","!")，然后进行翻译，并以类似嵌套数组的形式返回。
    # 例如对于"How are you? Fine, thank you. And you?"这个句子进行翻译后的返回结果是：
    # [[["你好吗？","How are you?",,,1],["好，谢谢。","Fine, thank you.",,,3],["你呢？","And you?"
    # ,,,1],[,,"Nǐ hǎo ma? Hǎo, xièxiè. Nǐ ne?"]],,"en",,,[["How are you?",,[["你好吗？",1000,
    # true,false],["你怎么样？",1000,true,false],["你好吗？",1000,true,false],["你好?",1000,true,
    # false]],[[0,12]],"How are you?",0,0],["Fine, thank you.",,[["好，谢谢。",0,true,false],
    # ["很好，谢谢你。",0,true,false]],[[0,16]],"Fine, thank you.",0,0],["And you?",,[["你呢？",
    # 1000,true,false],["和你？",1000,true,false],["和你？",1000,true,false]],[[0,8]],"And you?",
    # 0,0]],0.46840021,,[["en"],,[0.46840021],["en"]]]

    # fragments表示对大段文字分段后的句子的个数(不包括空字符串)
    fragments = len(filter(lambda x: x, re.split('[?.!]', text.strip())))
    result, i = '', 0
    # 首先要对返回的结果进行切割(split),然后使用切片截取出真正需要的中文结果
    for part in response.text.split("\",", fragments * 2 - 1):
        if i % 2 == 0:
            begin = part.rindex("\"")
            result += part[begin + 1:]
        i += 1
    return result
    # return response.text


def delete_rn(text):
    # 由于复制pdf文件的文字时会多出需要多余的换行符，所以需要将它们替换成空格
    return text.replace('\r\n', ' ')

if __name__ == "__main__":
    # print translate("How are you? Fine, thank you. And you?")

    # 使用win32clipboard模块可以获取剪切板里的文字，然后作为需要翻译的文字传入translate中
    win32clipboard.OpenClipboard()
    source = win32clipboard.GetClipboardData()
    win32clipboard.CloseClipboard()

    # 由于剪切板里的文字默认是gbk格式的编码，所以需要先解码，然后再转换为utf-8编码的字符串
    source = delete_rn(source).decode('gbk')

    # 将翻译结果写入文件中
    with open("C:\\Users\\Phoenix-Z\\Desktop\\test.doc", 'a') as f:
        output = translate(source)
        f.write(output)
        f.write("\r\n")

    print "done!"
