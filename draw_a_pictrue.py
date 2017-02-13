# coding:utf-8
# 这段代码用来
from matplotlib import pyplot as plt


plt.rcParams['font.sans-serif'] = ['SimHei']
plt.figure(figsize=(2, 3))
labels = [u'集中式数据挖掘', u'分布式数据挖掘']

sizes = [58.62, 41.38]
colors = ['yellowgreen', 'lightskyblue']

explode = (0.05, 0)

patches, l_text, p_text = plt.pie(sizes, explode=explode, labels=labels, colors=colors,
                                  labeldistance=0.3, autopct='%4.2f%%', shadow=False,
                                  startangle=90, pctdistance=0.6)

for t in l_text:
    t.set_size = 30
for t in p_text:
    t.set_size = 20

plt.axis('equal')
plt.legend()
plt.show()


