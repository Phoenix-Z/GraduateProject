# coding:utf-8
import numpy as np
import matplotlib.pyplot as plt
plt.rcParams['font.sans-serif'] = ['SimHei']
n_groups = 4

# 运行时间
# means_paillier = (2.122, 1.941, 10.236, 24.956)
# means_01encode = (0.121, 0.215, 3.491, 10.478)

# 平方误差
means_paillier = (16.44, 3908.48, 10.83, 3213.73)
means_01encode = (7.95, 1622.82, 10.91, 2009.56)

fig, ax = plt.subplots()
index = np.arange(n_groups)
bar_width = 0.35

opacity = 0.5
rects1 = plt.bar(index, means_paillier, bar_width, alpha=opacity, color='b', label='Paillier')
rects2 = plt.bar(index + bar_width, means_01encode, bar_width, alpha=opacity, color='r', label='01Encode')

plt.xlabel(u'数据集')
# plt.ylabel(u'运行时间/s')
plt.ylabel(u'平方误差')
plt.xticks(index + bar_width, ('Iris', 'Wine', 'Yeast', 'Wine Quality'))
fig.tight_layout()
plt.legend()

# plt.savefig('k-means.jpg')
plt.show()
