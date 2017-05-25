import re

file_path = 'yeast.txt'
out_file = open('out.txt', 'w')
for line in open(file_path):
    datas = re.split(' +', line[:-1])[1:]
    # datas = datas[1:] + datas[0:1]
    print datas
    out_file.write('\t'.join(datas))
    out_file.write('\n')

out_file.close()
