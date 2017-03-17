package phaseOne;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

public class Party extends Thread {

	/*
	 * 以下是私有数据
	 * 
	 * @param databases 分布式数据库的个数
	 * 
	 * @param id databases个分布式数据库的标号(1<=id<=databases)
	 * 
	 * @param clusterNum 聚类的个数，默认是4个
	 * 
	 * @param data 每个数据库存储的数据
	 * 
	 * @param attributes 数据库属性的个数
	 * 
	 * @param entities 元组的个数
	 * 
	 * @param centers k个聚类的聚类中心在各个分布式数据库的属性上的投影
	 */
	private static int databases;
	private int id;
	private int clusterNum = 4;
	private float[][] data = null;
	private int attributes;
	private int entities;
	private float[][] centers = null;

	/*
	 * 以下是静态数据，用于线程间通信
	 * 
	 * @param randomData 与其他数据库共享的秘密
	 * 
	 * @param sumT 专门为数据库r准备的，从第2个数据库到第r-1个数据库都要放松数据给第r个数据库(r==databases)
	 * 
	 * @param isAlright 用于其他数据库判断第一个数据库和第r个数据库是否已经完成计算
	 * 
	 * @param result 这是第一个数据库找到的最小距离的聚类标号
	 * 
	 * @param tunnel 这是给第1个数据库与第r个数据库传递数据使用的
	 * 
	 * @param now 用于第1个数据库通知第r个数据库当前最小的索引
	 * 
	 * @param done 用于协调工作
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
		// 初始化聚类中心
		init();
		float[][] prev = new float[clusterNum][];
		//int times = 0;
		do {
			for (int i = 0; i < clusterNum; i++) {
				prev[i] = Arrays.copyOf(centers[i], attributes);
			}
			System.out.println(Arrays.deepToString(centers));
			// 计算每一个元组与之最近的聚类的聚类编号
			Map<Integer, List<Integer>> entityBelongCluster = new HashMap<>();
			for (int i = 0; i < entities; i++) {
				System.out.println("another run: " + i);
				int clusterId = closestCluster(i);
				System.out.println("cluster id is " + clusterId);
				// 重置
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

			// 重新计算聚类中心
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
			// 计算当前元组与每个聚类中心的距离
			distance[i] = 0.0f;
			for (int j = 0; j < attributes; j++) {
				distance[i] += Math.pow(data[entityId][j] - centers[i][j], 2);
			}

			// 生成databases - 1个随机数，并发送给其他数据库
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

			// 计算本地秘密
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

		// 第2个数据库到第r-1个数据库将数据发送给第r个数据库
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
		// 第r个数据库计算更新后的数据
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
