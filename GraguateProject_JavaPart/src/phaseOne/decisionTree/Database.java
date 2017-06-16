package phaseOne.decisionTree;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Database extends Thread {

	/*
	 * @param id ���ڱ�����ݿ�
	 * 
	 * @param features ��ʾ���ݿ�ӵ�е����ݵ����Լ�
	 * 
	 * @param used ��ʾ���Լ��е������Ƿ��Ѿ�ʹ�ù��ˣ������ظ�ѡ��ĳһ������
	 * 
	 * @param datas ��ʾ���ݿ��е��������ݣ��������һ�б�ʾ���߱�����ֵ
	 * 
	 * @param featruesNum ��ʾ���ݿ������Եĸ�������ô���ݿ��е�������ΪfeatruesNum+1(���һ��Ϊ���߱���)
	 * 
	 * @param entities ��ʾ���ݿ���Ԫ��ĸ���
	 * 
	 * @param candidate ��ʾÿ�����ݿ�ѡȡ�ľ��������Ϣ��������ԣ��Թ�TTP����ѯ
	 * 
	 * @param databases ��ʾ���ݿ�ĸ���
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
		// best��ʾ��Ϣ������ߵ����Զ�Ӧ������
		int best = -1;
		// bestNow��ʾ��ߵ���Ϣ����
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
		// best����1�����������һ��û�к�ѡ���ԣ�����ֻ��һ�־��߱���ֵ��
		// ���ڵ�һ�������ѡ����߱���ֵ���ִ��������һ��
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
			System.out.println("I'm database" + this.id + ". I send message: " + tuple);
			synchronized (candidate) {
				candidate.put(this.id, tuple);
				if (candidate.size() == databases)
					candidate.notifyAll();
			}
//			System.out.println("I'm database" + this.id + ". I'm running!");
			synchronized (TrustedThirdParty.message) {
				if (!TrustedThirdParty.message.containsKey(this.id)) {
					try {
						TrustedThirdParty.message.wait();
					} catch (InterruptedException e) {
						e.printStackTrace();
						TrustedThirdParty.message.notifyAll();
					}
					List<Integer> newGroup = TrustedThirdParty.message.remove(this.id);
//					 System.out.println("I'm database" + this.id + " new group is:" + newGroup);
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
//			 System.out.println("I'm database" + this.id + ", I use " + Arrays.toString(used));
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