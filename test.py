randomnum = [[73.05717, 73.10259], [40.62279, 40.602757]]
d1 = [[1], [1.2], [100.1], [99.9]]
d2 = [[0.9], [1.1], [100.2], [99.8]]

for i in xrange(len(d1)):
    for j in [0, 1]:
        dis = (d1[i][0] - randomnum[j][0]) ** 2 + (d2[i][0] - randomnum[j][1]) ** 2
        print dis
