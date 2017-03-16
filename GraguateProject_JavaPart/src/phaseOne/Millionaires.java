package phaseOne;

import java.util.LinkedList;
import java.util.List;
import java.util.Random;

public class Millionaires extends Thread {
	
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
		for(int i = 0; i < 32; i++) {
			cs[i] = (byte) (xr[0][i] ^ xr[1][i]);
		} 
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
	
	public byte[][] SHTP1(byte[] cs, byte[] rs) {
		// System.out.println("I'm " + this.name + ".I'm using SHTP1");
		byte[][] GT = new byte[32][];
		for(int i = 0; i < 32; i++) {
			GT[i] = new byte[lambda];
		}
		helper(GT, cs, rs, true);
		return GT;
	}
	
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
		Millionaires bob = new Millionaires("Bob", 12);
		alice.start();
		bob.start();
	}
}
