# coding:utf-8
import math


def entropy(prob):
    return -sum([prob[i] * math.log(prob[i], 2) for i in xrange(len(prob))])

probabilities = [0.5] * 2
print entropy(probabilities)

