package phaseOne;

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
	 */
	private static int databases;
	private int id;
	private int clusterNum = 4;
	private float[][] data = null;
	private int attributes;
	private int entities;
	private float[][] centers = null;

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

	@Override
	public void run() {
		// ��ʼ����������
		init();
		float[][] prev = new float[clusterNum][];
		//int times = 0;
		do {
			for (int i = 0; i < clusterNum; i++) {
				prev[i] = Arrays.copyOf(centers[i], attributes);
			}
			System.out.println(Arrays.deepToString(centers));
			// ����ÿһ��Ԫ����֮����ľ���ľ�����
			Map<Integer, List<Integer>> entityBelongCluster = new HashMap<>();
			for (int i = 0; i < entities; i++) {
				System.out.println("another run: " + i);
				int clusterId = closestCluster(i);
				System.out.println("cluster id is " + clusterId);
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
				System.out.println("Party" + this.id + " is running." + "This is " + i + " entity.");
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

			System.out.println("I'm party" + this.id + ".My centers are " + Arrays.deepToString(centers));
			//times++;
		} while (isSatisfyCriteria(prev, centers));
	}

	public void init() {
		Random random = new Random(47);
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
		float threshold = 10e-2f;
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
				}
				randomData.notifyAll();
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
				}
				done.clear();
				randomData.clear();
				done.notifyAll();
			}
			//System.out.println("I'm out");
		}
		
		//System.out.println("Phase one is okay. I'm Party" + this.id);

		// ��2�����ݿ⵽��r-1�����ݿ⽫���ݷ��͸���r�����ݿ�
		if (this.id != 1) {
			synchronized (sumT) {
				if (this.id == databases && sumT.size() < (databases - 2)) {
					try {
						sumT.wait();
					} catch (InterruptedException e) {
						e.printStackTrace();
						sumT.notify();
					}
				}
				sumT.add(T);
				if (sumT.size() == (databases - 2)) {
					sumT.notifyAll();
				}
			}
		}
		
		//System.out.println("Phase two is okay. I'm Party" + this.id);
		// ��r�����ݿ������º������
		if (this.id == databases) {
			for (int i = 0; i < sumT.size(); i++) {
				for(int j = 0; j < clusterNum; j++) {
					T[j] += sumT.get(i)[j];
				}
			}
			System.out.println("I'm Party" + this.id + ".T is" + Arrays.toString(T));
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
			System.out.println("I'm Party" + this.id + ". T is " + Arrays.toString(T));
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
							System.out.println("I'm waiting");
							tunnel.wait();
						} catch (InterruptedException e) {
							e.printStackTrace();
							tunnel.notify();
						}
					}
					System.out.println("I'm awaking:" + tunnel.get(0));
					right = tunnel.remove(0);
					System.out.println("left is " + left + " right is " +right);
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
				System.out.println("minIndex is " + minIndex);
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
		System.out.println("I'm party" + this.id + ". And result is " + result);
		return result;
	}

	public void recalculateCenters(float[][] centers, int clusterId, List<Integer> entities) {
		System.out.println("cluster id is " + clusterId);
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
		float[][] d1 = {{1f},{1.2f},{100.1f},{99.9f}};
		float[][] d2 = {{0.9f},{1.1f},{100.2f},{99.8f}};
		Party party1 = new Party(1, 2, d1);
		Party party2 = new Party(2, 2, d2);
		party1.start();
		party2.start();
/*		try {
			sleep(100);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		System.out.println(Arrays.deepToString(party1.getCenters()));
		System.out.println(Arrays.deepToString(party2.getCenters()));*/
	}

}
