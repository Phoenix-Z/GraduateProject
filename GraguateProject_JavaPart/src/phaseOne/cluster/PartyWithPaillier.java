package phaseOne.cluster;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

public class PartyWithPaillier extends Thread {

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
	 * 
	 * @param compare 用于加解密
	 * 
	 * @param cipher 用于第1个数据库与第r个数据库传递密文
	 */
	static Map<Integer, float[]> randomData = new HashMap<>();
	static List<float[]> sumT = new ArrayList<>();
	static List<Boolean> isAlright = new ArrayList<>();
	static int result = -1;
	static List<BigInteger> tunnel = new ArrayList<>();
	static List<Integer> now = new ArrayList<>();
	static List<Integer> done = new ArrayList<>();
	static Paillier compare = new Paillier(32, 32);
	static List<BigInteger> cipher = new ArrayList<>();
	static int iterateTimes = 0;

	public PartyWithPaillier(int id, float[][] data) {
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

	public PartyWithPaillier(int id, int k, float[][] data) {
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
		do {
			for (int i = 0; i < clusterNum; i++) {
				prev[i] = Arrays.copyOf(centers[i], attributes);
			}
			//System.out.println("I'm party" + this.id + Arrays.deepToString(centers));
			// 计算每一个元组与之最近的聚类的聚类编号
			Map<Integer, List<Integer>> entityBelongCluster = new HashMap<>();
			for (int i = 0; i < entities; i++) {
				//System.out.println("another run: " + i);
				int clusterId = closestCluster(i);
				//System.out.println("cluster id is " + clusterId);
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
//				System.out.println("Party" + this.id + " is running." + "This is " + i + " entity.");
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
//			System.out.println("I'm still awake!");
			if(iterateTimes++ > 15)
				break;
		} while (!isSatisfyCriteria(prev, centers));
		System.out.println("I'm party" + this.id + ".My centers are " + Arrays.deepToString(centers));
		// System.out.println("iterate times is: " + iterateTimes);
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
				} else{
					randomData.notifyAll();
				}
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
				} else {
					done.clear();
					randomData.clear();
					done.notifyAll();
				}
			}
		}
		//System.out.println("I'm out");
		
		//System.out.println("Phase one is okay. I'm Party" + this.id);

		// 第2个数据库到第r-1个数据库将数据发送给第r个数据库
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
		
		// System.out.println("Phase two is okay. I'm Party" + this.id);
		// 第r个数据库计算更新后的数据
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
				int leftWithTwoPrecision = (int) (left * 100);
				BigInteger plain = new BigInteger(Integer.toString(leftWithTwoPrecision));
				// System.out.println("plaintext is: " + plain);
				BigInteger X, Y;
				synchronized (now) {
					now.add(result);
					now.notify();
				}
				synchronized (cipher) {
					cipher.add(compare.Encryption(plain));
					cipher.notify();
				}
				synchronized (tunnel) {
					if(tunnel.size() < 2) {
						try {
//							System.out.println("I'm waiting tunnel");
							tunnel.wait();
						} catch (InterruptedException e) {
							e.printStackTrace();
							tunnel.notify();
						}
					}
//					System.out.println("I got tunnel");
//					System.out.println("I'm awaking:" + tunnel.get(0));
					X = tunnel.remove(0);
					Y = tunnel.remove(0);
//					System.out.println("left is " + left + " right is " +right);
				}
				BigInteger decrypt_X = compare.Decryption(X);
				BigInteger decrypt_Y = compare.Decryption(Y);
				//System.out.println("decrypt_X is " + decrypt_X + " decrypt_Y is " + decrypt_Y);
				if(decrypt_X.compareTo(decrypt_Y) > 0) {
					result = i;
				}
			}
			synchronized (isAlright) {
				isAlright.add(true);
				isAlright.notifyAll();
			}
		}
//		System.out.println("Phase three is okay. I'm Party" + this.id);
		if(this.id == databases) {

			for(int i = 1; i < clusterNum; i++) {
				int minIndex;
				synchronized (now) {
					if(now.size() < 1) {
						try {
//							System.out.println("I'm waiting now");
							now.wait();
						} catch (InterruptedException e) {
							e.printStackTrace();
							now.notify();
						}
					}
//					System.out.println("I got now");
					minIndex = now.remove(0);
				}
//				System.out.println("minIndex is " + minIndex);
				float right = T[i] - T[minIndex];
				int rightWithTwoPrecision = (int) (right * 100);
//				Random rnd = new Random();
				int u = 8;
				int w = 5;
//				System.out.println(u + " " + w);
				int v = 4;
				BigInteger c;
				synchronized (cipher) {
					if(cipher.size() < 1) {
						try {
//							 System.out.println("I'm waiting cipher");
							cipher.wait();
						} catch (InterruptedException e) {
							e.printStackTrace();
							cipher.notify();
						}
					}
					c = cipher.remove(0);
				}
				BigInteger X = c.pow(u).multiply(compare.Encryption(new BigInteger(Integer.toString(v))));
				BigInteger Y = compare.Encryption(new BigInteger(Integer.toString(u * rightWithTwoPrecision + w)));
				synchronized (tunnel) {
					tunnel.add(X);
					tunnel.add(Y);
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
		// System.out.println("I'm party" + this.id + ". And result is " + result);
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
		
		PartyWithPaillier party1 = new PartyWithPaillier(1, 3, d1);
		PartyWithPaillier party2 = new PartyWithPaillier(2, 3, d2);
		PartyWithPaillier party3 = new PartyWithPaillier(3, 3, d3);
		PartyWithPaillier party4 = new PartyWithPaillier(4, 3, d4);
		party1.start();
		party2.start();
		party3.start();
		party4.start();
/*		try {
			sleep(100);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		System.out.println(Arrays.deepToString(party1.getCenters()));
		System.out.println(Arrays.deepToString(party2.getCenters()));*/
	}

}
