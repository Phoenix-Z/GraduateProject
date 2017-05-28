# coding:utf-8
import random
import collections
import requests
from matplotlib import pyplot as plt
import numpy as np
import threading

plt.rcParams['font.sans-serif'] = ['SimHei']


def kclusters(rows, k=4):
    # 计算每个属性的取值范围
    ranges = [(min([row[i] for row in rows]), max([row[i] for row in rows]))
              for i in xrange(len(rows[0]))]

    # 随机生成k个聚类中心，i表示列号
    clusters = [[random.uniform(ranges[i][0], ranges[i][1]) for i in xrange(len(rows[0]))]
                for _ in xrange(k)]

    # 每个元组所属的聚类的标号
    cluster_belonged = [0] * len(rows)

    # 判断聚类中心是否有变化
    prev = [[0] * len(rows[0])] * k
    while criterion(clusters, prev):
        prev = clusters[:]
        for i in xrange(len(rows)):
            min_distance = sum([(rows[i][r] - clusters[0][r]) ** 2 for r in xrange(len(rows[i]))])
            for j in xrange(1, k):
                distance = sum([(rows[i][r] - clusters[j][r]) ** 2 for r in xrange(len(rows[i]))])
                if distance < min_distance:
                    cluster_belonged[i] = j
                    min_distance = distance

        # 更新聚类中心
        sum_of_values = [[0.0] * len(rows[0]) for _ in xrange(k)]
        for i in xrange(len(rows)):
            cluster_index = cluster_belonged[i]
            for j in xrange(len(rows[i])):
                sum_of_values[cluster_index][j] += rows[i][j]

        dt = collections.Counter(cluster_belonged)

        for key in dt.keys():
            times = dt.get(key)
            clusters[key] = [value / times for value in sum_of_values[key]]

    return clusters


def criterion(now, prev):
    for i in xrange(len(now)):
        for j in xrange(len(now[i])):
            if abs(now[i][j] - prev[i][j]) > 10e-10:
                return True
    return False


a = 5 * np.random.random((100, 2))
b = 5 * np.random.random((100, 2)) + 20
test_rows = a.tolist() + b.tolist()
lastclusters = kclusters(test_rows, 2)
x = [row[0] for row in test_rows]
y = [row[1] for row in test_rows]
plt.scatter(x, y, marker='o', c='r', s=5, label=u'样本点')
cx = [cluster[0] for cluster in lastclusters]
cy = [cluster[1] for cluster in lastclusters]
plt.scatter(cx, cy, marker='x', c='g', s=20, label=u'聚类中心')
plt.legend()
plt.show()
# if __name__ == "__main__":
#     response = requests.get("http://archive.ics.uci.edu/ml/machine-learning-databases/wine/wine.data")
#     rows = []
#     for line in response.text.split("\n"):
#         if line:
#             rows.append(map(float, line.split(",")[1:]))
#     last = kclusters(rows, 3)
#     for res in last:
#         print res

