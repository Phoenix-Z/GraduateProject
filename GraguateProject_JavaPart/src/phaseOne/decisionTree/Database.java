package phaseOne.decisionTree;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Database extends Thread {

	/*
	 * @param id 用于标记数据库
	 * 
	 * @param features 表示数据库拥有的数据的属性集
	 * 
	 * @param used 表示属性集中的属性是否已经使用过了，避免重复选择某一个属性
	 * 
	 * @param datas 表示数据库中的所有数据，其中最后一列表示决策变量的值
	 * 
	 * @param featruesNum 表示数据库中属性的个数，那么数据库中的列数就为featruesNum+1(最后一列为决策变量)
	 * 
	 * @param entities 表示数据库中元组的个数
	 * 
	 * @param candidate 表示每个数据库选取的具有最大信息增益的属性，以供TTP来查询
	 * 
	 * @param databases 表示数据库的个数
	 */
	private int id;
	private String[] features = null;
	private boolean[] used = null;
	private String[][] datas = null;
	private int featuresNum;
	private int entities;
	public static Map<Integer, Tuple> candidate = new HashMap<>();
	public static int databases = 0;

	public Database(int id, String[] features, String[][] datas) {
		this.id = id;
		databases++;
		this.features = features;
		this.featuresNum = features.length;
		this.used = new boolean[this.featuresNum];
		this.datas = datas;
		this.entities = datas.length;
	}

	public double getImpurity(int[] group) {
		double impurity = 0;
		Map<String, Integer> map = new HashMap<>();
		for (int index : group) {
			String key = datas[index][this.featuresNum];
			map.put(key, map.getOrDefault(key, 0) + 1);
		}
		for (Map.Entry<String, Integer> entry : map.entrySet()) {
			// System.out.println(entry.getKey()+ ": " + entry.getValue());
			double posibility = entry.getValue() * 1.0 / group.length;
			impurity += posibility * (Math.log(posibility) / Math.log(2));
		}
		return -impurity;
	}

	public Map<String, List<Integer>> getArrange(int featureIndex, int[] group) {
		Map<String, List<Integer>> map = new HashMap<>();
		for (int i = 0; i < group.length; i++) {
			String key = datas[group[i]][featureIndex];
			List<Integer> subGroup = null;
			if (map.containsKey((key))) {
				subGroup = map.get(key);
			} else {
				subGroup = new ArrayList<>();
			}
			subGroup.add(group[i]);
			map.put(key, subGroup);
		}
		return map;
	}
	
	public double getGain(int featureIndex, int[] group, double totalImpurity) {
		double impurity = 0;
		Map<String, List<Integer>> map = getArrange(featureIndex, group);
		for (Map.Entry<String, List<Integer>> entry : map.entrySet()) {
			// System.out.println(entry.getKey());
			int count = entry.getValue().size();
			int[] subGroup = new int[count];
			for (int i = 0; i < count; i++) {
				subGroup[i] = entry.getValue().get(i);
			}
			double posibility = count * 1.0 / group.length;

			impurity += posibility * getImpurity(subGroup);
			// System.out.println("impurity:" + impurity);
		}
		return totalImpurity - impurity;
	}

	public Tuple chooseBestFeature(int[] group) {
		// best表示信息增益最高的属性对应的索引
		int best = -1;
		// bestNow表示最高的信息增益
		double bestNow = 0;
		double totalImpurity = getImpurity(group);
		for (int i = 0; i < featuresNum; i++) {
			if (!used[i]) {
				double now = getGain(i, group, totalImpurity);
				if (Math.abs(now - 1.0) >10e-10 && now > bestNow) {
					best = i;
					bestNow = now;
				}
			}
		}
		// best等于1有两种情况，一是没有候选属性，二是只有一种决策变量值了
		// 对于第一种情况，选择决策变量值出现次数多的那一个
		if(best == -1) {
			return getDefaultTuple(group);
		}
		return new Tuple(features[best], bestNow, getArrange(best, group));
	}
	
	public Tuple getDefaultTuple(int[] group) {
		String feature = "";
		int count = 0;
		Map<String, Integer> map = new HashMap<>();
		for (int i = 0; i < group.length; i++) {
			map.put(datas[group[i]][featuresNum], map.getOrDefault(datas[group[i]][featuresNum], 0) + 1);
		}
		for (Map.Entry<String, Integer> entry : map.entrySet()) {
			if (entry.getValue() > count) {
				feature = entry.getKey();
				count = entry.getValue();
			}
		}
		return new Tuple(feature, 0, null);
	}

	public boolean listEquals(List<Integer> aList, List<Integer> bList) {
		if(aList.size() != bList.size()) {
			return false;
		}
		for(int i = 0; i < aList.size(); i++) {
			if(!aList.get(0).equals(bList.get(0)))
				return false;
		}
		return true;
	}
	
	@Override
	public void run() {
		int[] group = new int[this.entities];
		for (int i = 0; i < this.entities; i++)
			group[i] = i;
		do {
			Tuple tuple = chooseBestFeature(group);
			System.out.println(tuple);
			synchronized (candidate) {
				candidate.put(this.id, tuple);
				if (candidate.size() == databases)
					candidate.notifyAll();
			}

			synchronized (TrustedThirdParty.message) {
				if (!TrustedThirdParty.message.containsKey(this.id)) {
					try {
						TrustedThirdParty.message.wait();
					} catch (InterruptedException e) {
						e.printStackTrace();
						TrustedThirdParty.message.notifyAll();
					}
					List<Integer> newGroup = TrustedThirdParty.message.remove(this.id);
					System.out.println("I'm database" + this.id + " new group is:" + newGroup);
					if (newGroup == null) {
						group = null;
					} else {
						if(tuple.subGroups != null) {
							for(Map.Entry<String, List<Integer>> entry : tuple.subGroups.entrySet()) {
								if(listEquals(entry.getValue(), newGroup)) {
									for(int i = 0; i < featuresNum; i++) {
										if(features[i].equals(tuple.feature)) {
											used[i] = true;
											break;
										}
									}
								}
							}
						}
						group = new int[newGroup.size()];
						for (int i = 0; i < newGroup.size(); i++) {
							group[i] = newGroup.get(i);
						}
					}
				}
			}
			System.out.println("I'm database" + this.id + ", I use " + Arrays.toString(used));
		} while (group != null);
	}
}

class Tuple {
	String feature;
	double gain;
	Map<String, List<Integer>> subGroups;

	public Tuple(String feature, double gain, Map<String, List<Integer>> subGroups) {
		this.feature = feature;
		this.gain = gain;
		this.subGroups = subGroups;
	}

	@Override
	public String toString() {
		return "Tuple [feature=" + feature + ", gain=" + gain + ", subGroups=" + subGroups + "]";
	}

}