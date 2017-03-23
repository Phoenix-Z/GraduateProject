package phaseOne.decisionTree;

import java.util.ArrayList;

public class TreeNode {

	/*
	 * @param feature �ڵ������������Ե����ƣ�
	 * @param rules �ڵ�ķ��ѹ���
	 * @param kids �ӽڵ�ļ���
	 * @param datas ���ֵ��ýڵ��ѵ��Ԫ��
	 * @param candicateAttr ���ֵ��ýڵ�ĺ�ѡ����
	 */
	private String featrue;
	private ArrayList<String> rules;
	private ArrayList<TreeNode> kids;
	private ArrayList<ArrayList<String>> datas; 
	private ArrayList<String> candicateAttr;
	
	public TreeNode() {
		this.featrue = "";
		this.rules = new ArrayList<>();
		this.kids = new ArrayList<>();
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
