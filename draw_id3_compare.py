# coding:utf-8
import matplotlib.pyplot as plt

plt.rcParams['font.sans-serif'] = ['SimHei']
# plt.figure(figsize=(5, 5))
x = range(1000, 31000, 1000)
y_one_database = [0.045, 0.074, 0.108, 0.078, 0.096, 0.105, 0.111, 0.124, 0.133, 0.141,
                  0.158, 0.173, 0.198, 0.21, 0.223, 0.240, 0.253, 0.271, 0.289, 0.31,
                  0.331, 0.359, 0.388, 0.401, 0.423, 0.449, 0.471, 0.497, 0.523, 0.538]
y_two_database = [0.067, 0.086, 0.084, 0.082, 0.101, 0.117, 0.123, 0.132, 0.157, 0.136,
                  0.14, 0.145, 0.162, 0.183, 0.208, 0.235, 0.281, 0.235, 0.277, 0.342,
                  0.37, 0.328, 0.371, 0.394, 0.431, 0.403, 0.445, 0.444, 0.428, 0.462]
ax = plt.subplot(1, 1, 1)
plt.xlabel(u'实体个数(个)')
plt.ylabel(u'运行时间(s)')

# plt.plot(x, y_one_database, '-go', x, y_two_database, '--m^')
p1, = ax.plot(x, y_one_database, color='green', linestyle='solid', marker='o', markersize=5,
              markerfacecolor='yellow', label=u'集中式')
p2, = ax.plot(x, y_two_database, color='blue', linestyle='dashed', marker='^', markersize=5,
              markerfacecolor='magenta', label=u'分布式')
plt.legend()
# plt.savefig('compare.jpg')
plt.show()
