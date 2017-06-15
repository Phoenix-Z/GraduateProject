package phaseOne.cluster;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

public class Party extends Thread {

	/*
	 * ������˽������
	 * 
	 * @param databases �ֲ�ʽ���ݿ�ĸ���
	 * 
	 * @param id databases���ֲ�ʽ���ݿ�ı��(1<=id<=databases)
	 * 
	 * @param clusterNum ����ĸ�����Ĭ����4��
	 * 
	 * @param data ÿ�����ݿ�洢������
	 * 
	 * @param attributes ���ݿ����Եĸ���
	 * 
	 * @param entities Ԫ��ĸ���
	 * 
	 * @param centers k������ľ��������ڸ����ֲ�ʽ���ݿ�������ϵ�ͶӰ
	 * 
	 * @param entityBelongCluster ����ÿһ��Ԫ����֮����ľ���ľ�����
	 */
	private static int databases;
	private int id;
	private int clusterNum = 4;
	private float[][] data = null;
	private int attributes;
	private int entities;
	private float[][] centers = null;
	private float threshold = 10e-10f;
	private Map<Integer, List<Integer>> entityBelongCluster = null;

	/*
	 * �����Ǿ�̬���ݣ������̼߳�ͨ��
	 * 
	 * @param randomData ���������ݿ⹲�������
	 * 
	 * @param sumT ר��Ϊ���ݿ�r׼���ģ��ӵ�2�����ݿ⵽��r-1�����ݿⶼҪ�������ݸ���r�����ݿ�(r==databases)
	 * 
	 * @param isAlright �����������ݿ��жϵ�һ�����ݿ�͵�r�����ݿ��Ƿ��Ѿ���ɼ���
	 * 
	 * @param result ���ǵ�һ�����ݿ��ҵ�����С����ľ�����
	 * 
	 * @param tunnel ���Ǹ���1�����ݿ����r�����ݿ⴫������ʹ�õ�
	 * 
	 * @param now ���ڵ�1�����ݿ�֪ͨ��r�����ݿ⵱ǰ��С������
	 * 
	 * @param done ����Э������
	 */
	static Map<Integer, float[]> randomData = new HashMap<>();
	static List<float[]> sumT = new ArrayList<>();
	static List<Boolean> isAlright = new ArrayList<>();
	static int result = -1;
	static List<Float> tunnel = new ArrayList<>();
	static List<Integer> now = new ArrayList<>();
	static List<Integer> done = new ArrayList<>();
	static int iterateTimes = 0;

	public Party(int id, float[][] data) {
		this.id = id;
		this.data = data;
		this.entities = data.length;
		this.attributes = data[0].length;
		this.centers = new float[clusterNum][];
		for (int i = 0; i < clusterNum; i++) {
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
		this.centers = new float[clusterNum][];
		for (int i = 0; i < clusterNum; i++) {
			centers[i] = new float[this.attributes];
		}
		databases++;
	}
	
	public Party(int id, int k, float[][] data, float threshold) {
		this.id = id;
		this.clusterNum = k;
		this.data = data;
		this.entities = data.length;
		this.attributes = data[0].length;
		this.threshold = threshold;
		this.centers = new float[clusterNum][];
		for (int i = 0; i < clusterNum; i++) {
			centers[i] = new float[this.attributes];
		}
		databases++;
	}

	@Override
	public void run() {
		// ��ʼ����������
		init();
		float[][] prev = new float[clusterNum][];
		do {
			for (int i = 0; i < clusterNum; i++) {
				prev[i] = Arrays.copyOf(centers[i], attributes);
			}
			//System.out.println("I'm party" + this.id + Arrays.deepToString(centers));
			entityBelongCluster = new HashMap<>();
			for (int i = 0; i < entities; i++) {
				//System.out.println("another run: " + i);
				int clusterId = closestCluster(i);
				//System.out.println("cluster id is " + clusterId);
				// ����
				synchronized (done) {
					done.add(this.id);
					if(done.size() < databases) {
						try {
							done.wait();
						} catch (InterruptedException e) {
							e.printStackTrace();
							done.notifyAll();
						}
					} else {
						done.clear();
						isAlright.clear();
						result = -1;
						done.notifyAll();
					}
				}
				//System.out.println("Party" + this.id + " is running." + "This is " + i + " entity.");
				List<Integer> old = null;
				if (entityBelongCluster.containsKey(clusterId)) {
					old = entityBelongCluster.get(clusterId);
				} else {
					old = new ArrayList<>();
				}
				old.add(i);
				entityBelongCluster.put(clusterId, old);
			}

			// ���¼����������
			for (Map.Entry<Integer, List<Integer>> entry : entityBelongCluster.entrySet()) {
				recalculateCenters(centers, entry.getKey(), entry.getValue());
			}
			iterateTimes++;
		} while (!isSatisfyCriteria(prev, centers));
		System.out.println("I'm party" + this.id + ".My centers are " + Arrays.deepToString(centers));
		//System.out.println("iterate times is: " + iterateTimes);
	}

	public void init() {
		Random random = new Random();
		for (int i = 0; i < attributes; i++) {
			float min = Float.MAX_VALUE, max = Float.MIN_NORMAL;
			for (int j = 0; j < entities; j++) {
				if (data[j][i] < min) {
					min = data[j][i];
				}
				if (data[j][i] > max) {
					max = data[j][i];
				}
			}
			for (int j = 0; j < clusterNum; j++) {
				centers[j][i] = random.nextFloat() * (max - min) + min;
			}
		}
	}

	public boolean isSatisfyCriteria(float[][] prev, float[][] now) {
		for (int i = 0; i < prev.length; i++) {
			for (int j = 0; j < prev[i].length; j++) {
				if (Math.abs(prev[i][j] - now[i][j]) > threshold)
					return false;
			}
		}
		return true;
	}

	public int closestCluster(int entityId) {
		Random random = new Random();
		float[] distance = new float[clusterNum];
		float[] T = new float[clusterNum];
		for (int i = 0; i < clusterNum; i++) {
			// ���㵱ǰԪ����ÿ���������ĵľ���
			distance[i] = 0.0f;
			for (int j = 0; j < attributes; j++) {
				distance[i] += Math.pow(data[entityId][j] - centers[i][j], 2);
			}

			// ����databases - 1��������������͸��������ݿ�
			float[] secrets = new float[databases];
			for (int j = 1; j <= databases; j++) {
				if (j != this.id) {
					secrets[j - 1] = random.nextFloat();
				}
			}
			//System.out.println("entity id is: " + entityId + ". cluster id is: " + i + ".I'm party" + this.id);
			synchronized (randomData) {
				randomData.put(id, secrets);
				if (randomData.size() < databases) {
					try {
						randomData.wait();
					} catch (InterruptedException e) {
						e.printStackTrace();
						randomData.notifyAll();
					}
				} else{
					randomData.notifyAll();
				}
			}

			// ���㱾������
			secrets[id - 1] = distance[i];
			for (int j = 1; j <= databases; j++) {
				if (j != this.id) {
					secrets[id - 1] -= secrets[j - 1];
				}
			}

			T[i] = secrets[id - 1];
			for (int j = 1; j <= databases; j++) {
				if (j != this.id) {
					T[i] += randomData.get(j)[id - 1];
				}
			}
			
			synchronized (done) {
				done.add(id);
				if(done.size() < databases) {
					try {
						done.wait();
					} catch (InterruptedException e) {
						e.printStackTrace();
						done.notifyAll();
					}
				} else {
					done.clear();
					randomData.clear();
					done.notifyAll();
				}
			}
		}
		//System.out.println("I'm out");
		
		//System.out.println("Phase one is okay. I'm Party" + this.id);

		// ��2�����ݿ⵽��r-1�����ݿ⽫���ݷ��͸���r�����ݿ�
//		if (this.id != 1) {
			synchronized (sumT) {
				if (this.id == databases && sumT.size() < (databases - 1)) {
					try {
						sumT.wait();
					} catch (InterruptedException e) {
						e.printStackTrace();
						sumT.notify();
					}
				}
				sumT.add(T);
				if (sumT.size() == (databases - 1)) {
					sumT.notifyAll();
				}
			}
//		}
		
		//System.out.println("Phase two is okay. I'm Party" + this.id);
		// ��r�����ݿ������º������
		if (this.id == databases) {
			for (int i = 0; i < sumT.size(); i++) {
				for(int j = 0; j < clusterNum; j++) {
					T[j] += sumT.get(i)[j];
				}
			}
			synchronized (sumT) {
				sumT.clear();
			}
			//System.out.println("I'm Party" + this.id + ".T is" + Arrays.toString(T));
		}
		
		
		if (this.id > 1 && this.id < databases) {
			synchronized (isAlright) {
				if(isAlright.size() < 1) {
					try {
						isAlright.wait();
					} catch (InterruptedException e) {
						e.printStackTrace();
						isAlright.notifyAll();
					}
				}
			}
		}
		
		if(this.id == 1) {
			//System.out.println("I'm Party" + this.id + ". T is " + Arrays.toString(T));
			//System.out.println("now's result is" + result);
			result = 0;
			for(int i = 1; i < clusterNum; i++) {
				float left = T[result] - T[i];
				float right = 0f;
				synchronized (now) {
					now.add(result);
					now.notify();
				}
				synchronized (tunnel) {
					if(tunnel.size() < 1) {
						try {
//							System.out.println("I'm waiting");
							tunnel.wait();
						} catch (InterruptedException e) {
							e.printStackTrace();
							tunnel.notify();
						}
					}
//					System.out.println("I'm awaking:" + tunnel.get(0));
					right = tunnel.remove(0);
//					System.out.println("left is " + left + " right is " +right);
				}
				if(left > right) {
					result = i;
				}
			}
			synchronized (isAlright) {
				isAlright.add(true);
				isAlright.notifyAll();
			}
		}
		//System.out.println("Phase three is okay. I'm Party" + this.id);
		if(this.id == databases) {

			for(int i = 1; i < clusterNum; i++) {
				int minIndex;
				synchronized (now) {
					if(now.size() < 1) {
						try {
							now.wait();
						} catch (InterruptedException e) {
							e.printStackTrace();
							now.notify();
						}
					}
					minIndex = now.remove(0);
				}
//				System.out.println("minIndex is " + minIndex);
				float right = T[i] - T[minIndex];
				synchronized (tunnel) {
					tunnel.add(right);
					tunnel.notify();
				}
			}
			
			synchronized (isAlright) {
				if(isAlright.size() < 1) {
					try {
						isAlright.wait();
					} catch (InterruptedException e) {
						e.printStackTrace();
						isAlright.notifyAll();
					}
				}
			}
		}
//		System.out.println("I'm party" + this.id + ". And result is " + result);
		return result;
	}

	public void recalculateCenters(float[][] centers, int clusterId, List<Integer> entities) {
		//System.out.println("cluster id is " + clusterId);
		for (int i = 0; i < this.attributes; i++) {
			float sum = 0;
			for (int entityId : entities) {
				sum += data[entityId][i];
			}
			centers[clusterId][i] = sum / entities.size();
		}
	}

	public float[][] getCenters() {
		return centers;
	}

	public static void main(String[] args) {
		List<float[]> datas = GetData.getData();
		int length = datas.size();
		float[][] d1 = new float[length][];
		float[][] d2 = new float[length][];
		float[][] d3 = new float[length][];
		float[][] d4 = new float[length][];
		for(int i = 0; i < length; i++) {
			int j = 0;
			d1[i] = new float[3];
			for(int k = 0; k < 3; k++) {
				d1[i][k] = datas.get(i)[j++];
			}
			d2[i] = new float[3];
			for(int k = 0; k < 3; k++) {
				d2[i][k] = datas.get(i)[j++];
			}
			d3[i] = new float[3];
			for(int k = 0; k < 3; k++) {
				d3[i][k] = datas.get(i)[j++];
			}
			d4[i] = new float[4];
			for(int k = 0; k < 4; k++) {
				d4[i][k] = datas.get(i)[j++];
			}
		}
		
		Party party1 = new Party(1, 3, d1);
		Party party2 = new Party(2, 3, d2);
		Party party3 = new Party(3, 3, d3);
		Party party4 = new Party(4, 3, d4);
		party1.start();
		party2.start();
		party3.start();
		party4.start();
		//System.out.println(iterateTimes);
/*		try {
			sleep(100);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		System.out.println(Arrays.deepToString(party1.getCenters()));
		System.out.println(Arrays.deepToString(party2.getCenters()));*/
	}

	public double getErrors() {
		float errors = 0.0f;
		for(Map.Entry<Integer, List<Integer>> entry : entityBelongCluster.entrySet()) {
			int clusterId = entry.getKey();
			for(int entityId : entry.getValue()) {
				for(int i = 0; i < this.attributes; i++) {
					errors += Math.pow(data[entityId][i] - centers[clusterId][i], 2);
				}
			}
		}
		return errors;
	}
	
}
