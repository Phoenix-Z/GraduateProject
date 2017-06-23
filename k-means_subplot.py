# coding:utf-8
import numpy as np
import matplotlib.pyplot as plt
plt.rcParams['font.sans-serif'] = ['SimHei']

bar_width = 0.35
index = np.arange(0, 1, 0.5)

fig, axes = plt.subplots(nrows=2, ncols=2)
ax0, ax1, ax2, ax3 = axes.flatten()

ax0.set_title('Iris')
ax0_data = [16.44, 7.95]
ax0.bar(index, ax0_data, bar_width, color=['k', 'r'])
ax0.set_xlim(-0.35, 0.85)
ax0.set_xticks(index)
ax0.set_xticklabels(('Paillier', '01Encode'))
ax0.set_ylabel(u'平方误差')

ax1_data = [10.83, 10.91]
ax1.set_title('Yeast')
ax1.bar(index, ax1_data, bar_width, color=['k', 'r'])
ax1.set_xlim(-0.35, 0.85)
ax1.set_xticks(index)
ax1.set_xticklabels(('Paillier', '01Encode'))
ax1.set_ylabel(u'平方误差')

ax2_data = [3908.48, 1622.82]
ax2.set_title('Wine')
ax2.bar(index, ax2_data, bar_width, color=['k', 'r'])
ax2.set_xlim(-0.35, 0.85)
ax2.set_xticks(index)
ax2.set_xticklabels(('Paillier', '01Encode'))
ax2.set_ylabel(u'平方误差')

ax3_data = [3213.73, 2009.56]
ax3.set_title('Wine Quality')
ax3.bar(index, ax3_data, bar_width, color=['k', 'r'])
ax3.set_xlim(-0.35, 0.85)
ax3.set_xticks(index)
ax3.set_xticklabels(('Paillier', '01Encode'))
ax3.set_ylabel(u'平方误差')

fig.tight_layout()
plt.savefig('k-means.jpg')
plt.show()
