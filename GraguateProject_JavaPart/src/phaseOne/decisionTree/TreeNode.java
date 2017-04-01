package phaseOne.decisionTree;

import java.util.ArrayList;

public class TreeNode {

	/*
	 * @param feature 节点名（分裂属性的名称）
	 * @param rules 节点的分裂规则
	 * @param kids 子节点的集合
	 * @param datas 划分到该节点的训练元组
	 * @param candicateAttr 划分到该节点的候选属性
	 */
	private String featrue;
	private ArrayList<String> rules;
	private ArrayList<TreeNode> kids;
	private ArrayList<ArrayList<String>> datas; 
	private ArrayList<String> candicateAttr;
	

	@Override
	public String toString() {
		return "TreeNode [featrue=" + featrue + ", rules=" + rules + ", kids=" + kids + "]";
	}

	
	public TreeNode() {
		this.featrue = "";
		this.rules = new ArrayList<>();
		this.kids = new ArrayList<>();
		this.datas = null;
		this.candicateAttr = null;
	}

	public TreeNode(String featrue) {
		this.featrue = featrue;
		this.rules = null;
		this.kids = null;
		this.datas = null;
		this.candicateAttr = null;
	}

	public String getFeatrue() {
		return featrue;
	}

	public void setFeatrue(String featrue) {
		this.featrue = featrue;
	}

	public ArrayList<String> getRules() {
		return rules;
	}

	public void setRules(ArrayList<String> rules) {
		this.rules = rules;
	}

	public ArrayList<TreeNode> getKids() {
		return kids;
	}

	public void setKids(ArrayList<TreeNode> kids) {
		this.kids = kids;
	}

	public ArrayList<ArrayList<String>> getDatas() {
		return datas;
	}

	public void setDatas(ArrayList<ArrayList<String>> datas) {
		this.datas = datas;
	}

	public ArrayList<String> getCandicateAttr() {
		return candicateAttr;
	}

	public void setCandicateAttr(ArrayList<String> candicateAttr) {
		this.candicateAttr = candicateAttr;
	}

	
}
