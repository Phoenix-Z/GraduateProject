# coding:utf-8
# 这段代码用来画饼状图。
from matplotlib import pyplot as plt


plt.rcParams['font.sans-serif'] = ['SimHei']
plt.figure(figsize=(2, 3))
labels = [u'分类', u'聚类', u'关联规则']

sizes = [17.24, 10.34, 13.80]
colors = ['red', 'yellow', 'blue']

explode = (0, 0, 0)

patches, l_text, p_text = plt.pie(sizes, explode=explode, labels=labels, colors=colors,
                                  labeldistance=0.4, autopct='%4.2f%%', shadow=False,
                                  startangle=90, pctdistance=0.6)

for t in l_text:
    t.set_size = 30
for t in p_text:
    t.set_size = 20

plt.axis('equal')
plt.legend()
plt.show()


