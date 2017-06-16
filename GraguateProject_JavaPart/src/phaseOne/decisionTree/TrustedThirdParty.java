package phaseOne.decisionTree;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TrustedThirdParty extends Thread {

	public static Map<Integer, List<Integer>> message = new HashMap<>();
	@SuppressWarnings("rawtypes")
	private Class testClass;

	public TrustedThirdParty(@SuppressWarnings("rawtypes") Class clazz) {
		testClass = clazz;
	}
	
	@Override
	public void run() {
		
		TreeNode root = new TreeNode();
		root = constructTree(root);
		System.out.println(root);
		// System.out.println("运行时间：" + (System.currentTimeMillis() - TestOneDatabase.startTime) * 0.001 + "秒");
		try {
			System.out.println("运行时间：" + (System.currentTimeMillis() - testClass.getField("startTime").getLong(testClass)) * 0.001 + "秒");
		} catch (IllegalArgumentException | IllegalAccessException | NoSuchFieldException | SecurityException e) {
			e.printStackTrace();
		}
		synchronized (message) {
			for(int i = 1; i <= Database.databases; i++) {
				message.put(i, null);
			}
			message.notifyAll();
		}
	}
	
	public TreeNode constructTree(TreeNode root) {
		List<Tuple> tuples = new ArrayList<>();
		synchronized (Database.candidate) {
			if (Database.candidate.size() < Database.databases) {
				try {
					Database.candidate.wait();
				} catch (InterruptedException e) {
					e.printStackTrace();
					Database.candidate.notifyAll();
				}
				tuples.addAll(Database.candidate.values());
				Database.candidate.clear();
			}
		}
		 System.out.println(tuples);
		// 如果样本已不再可分(或者已不再有候选属性)，则返回一个叶子节点，叶子对应的属性就是决策变量的值
		
		if(isLeaf(tuples)){
			if(tuples.size() != 0)
				return new TreeNode(tuples.get(0).feature);
			else
				return new TreeNode();
//			return new TreeNode(tuples.get(0).feature);
		}
	
		// 对tuples排序，以便找到信息增益最大的属性
		Collections.sort(tuples, new Comparator<Tuple>() {
			@Override
			public int compare(Tuple o1, Tuple o2) {
				return -Double.compare(o1.gain, o2.gain);
			}
		});
		
		Tuple maxTuple = tuples.get(0);
		root.setFeatrue(maxTuple.feature);
		Map<String, List<Integer>> maxGain = maxTuple.subGroups;
		ArrayList<String> rules = new ArrayList<>();
		ArrayList<TreeNode> kids = new ArrayList<>();
		for(Map.Entry<String, List<Integer>> entry : maxGain.entrySet()) {
			rules.add(entry.getKey());
			synchronized (message) {
				for(int i = 1; i <= Database.databases; i++) {
					message.put(i, entry.getValue());
				}
				message.notifyAll();
			}
			TreeNode kid = new TreeNode();
			kids.add(constructTree(kid));
		}
		root.setRules(rules);
		root.setKids(kids);
		return root;
	}
	
	public boolean isLeaf(List<Tuple> tuples) {
		int count = 0;
		for(Tuple tuple : tuples) {
			if(Math.abs(tuple.gain - 0) < 10e-7)
				count++;
		}
		return count == tuples.size();
	}
}
