# coding:utf-8
import urllib


base_url = 'http://en.academicresearch.net//pages/yok_(79)/Files/volkan_kaya_selim_202736/' \
           'volkan_kaya_selim_202736.pages/page_00'
for i in xrange(1, 52):
    if i < 10:
        url = base_url + '0' + str(i) + '.pdf'
    else:
        url = base_url + str(i) + '.pdf'
    urllib.urlretrieve(url, 'C:\Users\Phoenix-Z\Desktop\pdf\\' + str(i) + '.pdf')

print 'done!'
