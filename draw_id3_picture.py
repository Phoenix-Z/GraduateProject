# coding:utf-8
import matplotlib.pyplot as plt
plt.rcParams['font.sans-serif'] = ['SimHei']
x = range(1, 10)
y = [0.386, 0.406, 0.515, 0.623, 0.805, 0.819, 0.831, 0.932, 0.95]

plt.xlabel(u'数据库个数(个)')
plt.ylabel(u'运行时间(s)')
plt.plot(x, y, color='green', linestyle='solid', marker='o', markerfacecolor='blue',
         markersize=5, antialiased=True)
# plt.savefig('out.jpg')
plt.show()
