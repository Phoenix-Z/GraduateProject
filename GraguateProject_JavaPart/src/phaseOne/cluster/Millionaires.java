package phaseOne.cluster;

import java.util.LinkedList;
import java.util.List;
import java.util.Random;

/**
 * 本代码处理int型或者float型的数字，这些数字会被转换成32bit的二进制形式。
 * 这里没有使用论文中提到的两个半可信第三方，而是采用如下的方式：
 * Party X将cx发送给Party Y， 而将rx保留在本地，对于Party Y也是如此，将cy发送给Party X， 将ry保留在本地。
 * @author Phoenix-Z
 *
 */
public class Millionaires extends Thread {
	
	/**
	 * @param name 百万富翁的名字， 假设两个百万富翁的名字是Alice和Bob
	 * @param num 百万富翁的数字
	 * @param lambda 对应于论文中的lambda
	 * @param queue 多个线程共享的静态资源，用于传递密文
	 * @param anotherQueue 多个线程共享的静态资源，用于传递GT1和GT2
	 */
	private String name = null;
	private Integer num = null;
	private int lambda = 50;
	private byte[][] GT1, GT2;
	static private List<byte[]> queue = new LinkedList<>();
	static private List<byte[][]> anotherQueue = new LinkedList<>();
 
	public Millionaires(String name, int num) {
		this.name = name;
		this.num = num;
	}
	
	public Millionaires(String name, float num) {
		this.name = name;
		this.num = Float.floatToIntBits(num);
	}
	
	public Millionaires(String name, int num, int lambda) {
		this.name = name;
		this.num = num;
		this.lambda = lambda;
	}

	public Millionaires(String name, float num, int lambda) {
		this.name = name;
		this.num = Float.floatToIntBits(num);
		this.lambda = lambda;
	}
	
	@Override
	public void run() {
		// xr的第一行是x的二进制序列
		// xr的第二行是随机数
		byte[][] xr = init(num);
		byte[] cs = new byte[32];
		// 生成异或值
		for(int i = 0; i < 32; i++) {
			cs[i] = (byte) (xr[0][i] ^ xr[1][i]);
		} 
		// anotherCs接受另一方传来的密文
		byte[] anotherCs = null;
		synchronized (queue) {
			queue.add(cs);
			if(queue.size() == 1) {
				try {
					queue.wait();
				} catch (InterruptedException e) {
					e.printStackTrace();
					queue.notify();
				}
			}
			anotherCs = queue.remove(0);
			queue.notify();
		}
		// System.out.println(name + ": " + Arrays.toString(cs) + "and another" + Arrays.toString(anotherCs));
		if(this.name.equals("Alice")) {
			GT1 = SHTP2(anotherCs, xr[1]);
		} else {
			GT1 = SHTP1(anotherCs, xr[1]);
		}
		synchronized (anotherQueue) {
			anotherQueue.add(GT1);
			if(anotherQueue.size() == 1) {
				try {
					anotherQueue.wait();
				} catch (InterruptedException e) {
					e.printStackTrace();
					anotherQueue.notify();
				}
			}
			GT2 = anotherQueue.remove(0);
			anotherQueue.notify();
		}
		// 以下是algorithm 11
		int[][] GT = new int[32][];
		boolean flag = false;
		for(int i = 0; i < 32; i++) {
			GT[i] = new int[lambda];
			int count = 0;
			for(int j = 0; j < lambda; j++) {
				GT[i][j] = GT1[i][j] ^ GT2[i][j];
				if(GT[i][j] == 0) {
					count++;
				}
			}
			if(count == lambda) {
				flag = true;
				break;
			}
		}
		//System.out.println("I'm " + this.name + ".GT is " + Arrays.deepToString(GT));
		// System.out.println("I'm " + this.name + "GT1 is: " + Arrays.deepToString(GT1) + "\nGT2 is: " + Arrays.deepToString(GT2));
		if(this.name.equals("Alice")) {
			if(!flag) {
				System.out.println("I'm " + this.name + ". I'm smaller than the other.");
			} else {
				System.out.println("I'm " + this.name + ". I'm greater than the other.");
			}
		}
	}
	
	/**
	 * 产生随机数以及num的二进制序列
	 * @param num 百万富翁拥有的钱
	 * @return
	 */
	public byte[][] init(int num) {
		Random random = new Random();
		byte[][] xr = new byte[2][];
		xr[0] = new byte[32];
		xr[1] = new byte[32];
		for(int i = 31; i >= 0; i--) {
			xr[0][i] = (byte) (num & 1);
			xr[1][i] = (byte) random.nextInt(2);
			num >>= 1;
		}
		return xr;
	}
	
	/**
	 * 对应论文的algorithm 9
	 * @param cs 另一方传过来的密文
	 * @param rs 本地保存的随机数
	 * @return
	 */
	public byte[][] SHTP1(byte[] cs, byte[] rs) {
		// System.out.println("I'm " + this.name + ".I'm using SHTP1");
		byte[][] GT = new byte[32][];
		for(int i = 0; i < 32; i++) {
			GT[i] = new byte[lambda];
		}
		helper(GT, cs, rs, true);
		return GT;
	}
	
	/**
	 * 对应论文的algorithm 10
	 * @param cs 另一方传过来的密文
	 * @param rs 本地保存的随机数
	 * @return
	 */
	public byte[][] SHTP2(byte[] cs, byte[] rs) {
		// System.out.println("I'm " + this.name + ".I'm using SHTP2");
		byte[][] GT = new byte[32][];
		for(int i = 0; i < 32; i++) {
			GT[i] = new byte[lambda];
		}
		for(int i = 0; i < cs.length; i++) {
			cs[i] ^= 1;
		}
		helper(GT, cs, rs, false);
		return GT;
	}
	
	/**
	 * 对应algorithm 9和algorithm 10中逻辑相同的部分
	 * @param GT
	 * @param cs 另一方传过来的密文
	 * @param rs 本地保存的随机数
	 * @param inverse
	 */
	public void helper(byte[][] GT, byte[] cs, byte[] rs, boolean inverse) {
		Random PRNG = new Random(47);
		int[][] ex = new int[32][];
		int[][] ey = new int[32][];
		int[][] rnd = new int[32][];
		for(int i = 0; i < 32; i++) {
			rnd[i] = new int[lambda];
			for(int j = 0; j < lambda; j++) {
				rnd[i][j] = PRNG.nextInt(2);
			}
		}
		for(int i = 0; i < 32; i++) {
			ex[i] = new int[lambda];
			ey[i] = new int[lambda];
			for(int j = 0; j < lambda; j++) {
				int choice = PRNG.nextInt();
				//System.out.println("I'm " + this.name + ".My choice is " + choice);
				if(choice % 2 == 0) {
					ex[i][j] = cs[i] ^ rnd[i][j];
					ex[i][j] ^= 0;
					ey[i][j] = rs[i] ^ rnd[i][j];
					ey[i][j] ^= 1;
				} else {
					ex[i][j] = rnd[i][j];
					ey[i][j] = rnd[i][j];
				}
			}
		}
		
		int[][] component1 = new int[32][];
		for(int i = 0; i < 32; i++) {
			component1[i] = new int[lambda];
			for(int j = 0; j < lambda; j++) {
				component1[i][j] = ex[i][j] ^ ey[i][j];
			}
		}
		
		int[][] exy = new int[32][];
		for(int i = 0; i < 32; i++) {
			exy[i] = new int[lambda];
			for(int j = 0; j < lambda; j++) {
				int choice = PRNG.nextInt();
				if(choice % 2 == 0) {
					if (!inverse) {
						cs[i] ^= 1;
					}
					exy[i][j] = cs[i] ^ rs[i];
				} else {
					exy[i][j] = rnd[i][j];
				}
			}
		}
		
		int[][] component2 = new int[32][];
		for(int i = 0; i < 32; i++) {
			component2[i] = new int[lambda];
			for(int j = 0; j < lambda; j++) {
				for(int k = i + 1; k < 32; k++) {
					component2[i][j] ^= exy[k][j];
				}
			}
		}
		
		for(int i = 0; i < 32; i++) {
			for(int j =0; j < lambda; j++) {
				GT[i][j] = (byte) (component1[i][j] ^ component2[i][j]);
			}
		}
	}
	
	public static void main(String[] args) {
		Millionaires alice = new Millionaires("Alice", 10);
		Millionaires bob = new Millionaires("Bob", 11);
		alice.start();
		bob.start();
	}
}
