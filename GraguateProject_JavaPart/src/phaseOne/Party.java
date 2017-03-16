package phaseOne;

import java.util.Random;

public class Party extends Thread{

	/*
	 * @param databases �ֲ�ʽ���ݿ�ĸ���
	 * @param id databases���ֲ�ʽ���ݿ�ı��(1<=id<=databases)
	 * @param clusterNum ����ĸ�����Ĭ����4��
	 * @param data ÿ�����ݿ�洢������
	 * @param attributes ���ݿ����Եĸ���
	 * @param entities Ԫ��ĸ���
	 * @param centers k������ľ��������ڸ����ֲ�ʽ���ݿ�������ϵ�ͶӰ
	 */
	private static int databases;
	private int id;
	private int clusterNum = 4;
	private float[][] data = null;
	private int attributes;
	private int entities;
	private float[][] centers = new float[clusterNum][];
	
	public Party(int id, float[][] data) {
		this.id = id;
		this.data = data;
		this.entities = data.length;
		this.attributes = data[0].length;
		for(int i = 0; i < clusterNum; i++) {
			centers[i] = new float[this.attributes];
		}
		databases++;
	}

	public Party(int id, int k, float[][] data) {
		this.id = id;
		this.clusterNum = k;
		this.data = data;
		this.entities = data.length;
		this.attributes = data[0].length;
		for(int i = 0; i < clusterNum; i++) {
			centers[i] = new float[this.attributes];
		}
		databases++;
	}
	
	@Override
	public void run() {
		init();
	}
	
	public void init(){
		Random random = new Random(47);
		for(int i = 0; i < attributes; i++) {
			float min = Float.MAX_VALUE, max = Float.MIN_NORMAL;
			for(int j = 0; j < entities; j++) {
				if(data[j][i] < min) {
					min = data[j][i];
				}
				if(data[j][i] > max) {
					max = data[j][i];
				}
			}
			for(int j = 0; j < clusterNum; j++) {
				centers[j][i] = random.nextFloat() * (max - min) + min;
			}
		}
	}
	
	public static void main(String[] args) {
		
	}
	
}
