package phaseOne.cluster.test;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;

/**
 * �����봦��int�ͻ���float�͵����֣���Щ���ֻᱻת����4bit�Ķ�������ʽ��
 * ����û��ʹ���������ᵽ����������ŵ����������ǲ������µķ�ʽ��
 * Party X��cx���͸�Party Y�� ����rx�����ڱ��أ�����Party YҲ����ˣ���cy���͸�Party X�� ��ry�����ڱ��ء�
 * @author Phoenix-Z
 *
 */
public class Millionaires extends Thread {
	
	/**
	 * @param name �����̵����֣� �������������̵�������Alice��Bob
	 * @param num �����̵�����
	 * @param lambda ��Ӧ�������е�lambda
	 * @param queue ����̹߳���ľ�̬��Դ�����ڴ�������
	 * @param anotherQueue ����̹߳���ľ�̬��Դ�����ڴ���GT1��GT2
	 */
	private String name = null;
	private Integer num = null;
	private int lambda = 4;
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
		// xr�ĵ�һ����x�Ķ���������
		// xr�ĵڶ����������
		byte[][] xr = init(num);
		
//		// ������
//		if(this.name.equals("Alice")) {
//			xr[1] = new byte[]{0,0,1,1};
//		} else {
//			xr[1] = new byte[]{1,0,0,1};
//		}
		
		byte[] cs = new byte[4];
		// �������ֵ
		for(int i = 0; i < 4; i++) {
			cs[i] = (byte) (xr[0][i] ^ xr[1][i]);
		} 
		System.out.println(this.name + "�������ǣ�" + Arrays.toString(xr[0]));
		System.out.println(this.name + "��������ǣ�" + Arrays.toString(xr[1]));
		System.out.println(this.name + "�������ǣ�" + Arrays.toString(cs));
		// anotherCs������һ������������
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
		 System.out.println(name + ": " + Arrays.toString(cs) + "and another" + Arrays.toString(anotherCs));
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
		// ������algorithm 11
		int[][] GT = new int[4][];
		boolean flag = false;
		for(int i = 0; i < 4; i++) {
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
			}
		}
		System.out.println("total GT is:" + Arrays.deepToString(GT));
		
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
	 * ����������Լ�num�Ķ���������
	 * @param num ������ӵ�е�Ǯ
	 * @return
	 */
	public byte[][] init(int num) {
		Random random = new Random();
		byte[][] xr = new byte[2][];
		xr[0] = new byte[4];
		xr[1] = new byte[4];
		for(int i = 3; i >= 0; i--) {
			xr[0][i] = (byte) (num & 1);
			xr[1][i] = (byte) random.nextInt(2);
			num >>= 1;
		}
		return xr;
	}
	
	/**
	 * ��Ӧ���ĵ�algorithm 9
	 * @param cs ��һ��������������
	 * @param rs ���ر���������
	 * @return
	 */
	public byte[][] SHTP1(byte[] cs, byte[] rs) {
		// System.out.println("I'm " + this.name + ".I'm using SHTP1");
		byte[][] GT = new byte[4][];
		for(int i = 0; i < 4; i++) {
			GT[i] = new byte[lambda];
		}
		helper(GT, cs, rs, true);
		return GT;
	}
	
	/**
	 * ��Ӧ���ĵ�algorithm 10
	 * @param cs ��һ��������������
	 * @param rs ���ر���������
	 * @return
	 */
	public byte[][] SHTP2(byte[] cs, byte[] rs) {
		// System.out.println("I'm " + this.name + ".I'm using SHTP2");
		byte[][] GT = new byte[4][];
		for(int i = 0; i < 4; i++) {
			GT[i] = new byte[lambda];
		}
		for(int i = 0; i < cs.length; i++) {
			cs[i] ^= 1;
		}
		helper(GT, cs, rs, false);
		return GT;
	}
	
	/**
	 * ��Ӧalgorithm 9��algorithm 10���߼���ͬ�Ĳ���
	 * @param GT
	 * @param cs ��һ��������������
	 * @param rs ���ر���������
	 * @param inverse
	 */
	public void helper(byte[][] GT, byte[] cs, byte[] rs, boolean inverse) {
		System.out.println(name + "�յ�������Ϊ��" + Arrays.toString(cs));
		System.out.println(name + "���ص������Ϊ��" + Arrays.toString(rs));
		Random PRNG = new Random(47);
		int[][] ex = new int[4][];
		int[][] ey = new int[4][];
		int[][] rnd = new int[4][];
		for(int i = 0; i < 4; i++) {
			rnd[i] = new int[lambda];
			for(int j = 0; j < lambda; j++) {
				rnd[i][j] = PRNG.nextInt(2);
			}
		}
//		int[][] rnd = {{1, 1, 0, 0}, {1, 0, 1, 1}, {0, 1, 0, 1}, {0, 0, 1, 1}};
		for(int i = 0; i < 4; i++) {
			ex[i] = new int[lambda];
			ey[i] = new int[lambda];
			for(int j = 0; j < lambda; j++) {
				 int choice = PRNG.nextInt();
//				int choice = rnd[i][j];
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
		
		System.out.println(name + "��ex�ǣ�" + Arrays.deepToString(ex));
		System.out.println(name + "��ey�ǣ�" + Arrays.deepToString(ey));
		
		int[][] component1 = new int[4][];
		for(int i = 0; i < 4; i++) {
			component1[i] = new int[lambda];
			for(int j = 0; j < lambda; j++) {
				component1[i][j] = ex[i][j] ^ ey[i][j];
			}
		}
		
		System.out.println(name + "��component1�ǣ�" + Arrays.deepToString(component1));
		
		int[][] exy = new int[4][];
		for(int i = 0; i < 4; i++) {
			exy[i] = new int[lambda];
			if(!inverse)
				cs[i] ^= 1;
			for(int j = 0; j < lambda; j++) {
				int choice = PRNG.nextInt();
//				int choice = rnd[i][j];
				if(choice % 2 == 0) {
					exy[i][j] = cs[i] ^ rs[i];
				} else {
					exy[i][j] = rnd[i][j];
				}
			}
		}
		System.out.println(name + "��exy�ǣ�" + Arrays.deepToString(exy));
		
		/*
		 * ����γ����У���λ��Ӧ���ǵ���������λ��Ӧ���Ǹ�����
		 */
		int[][] component2 = new int[4][lambda];
		for(int i = 3; i >= 0; i--) {
			for(int j = 0; j < lambda; j++) {
				for(int k = i - 1; k >= 0; k--) {
					component2[i][j] ^= exy[k][j];
				}
			}
		}
		
		System.out.println(name + "��component2�ǣ�" + Arrays.deepToString(component2));
		
		for(int i = 0; i < 4; i++) {
			for(int j =0; j < lambda; j++) {
				GT[i][j] = (byte) (component1[i][j] ^ component2[i][j]);
			}
		}
		
		System.out.println(name + "��GT�ǣ�" + Arrays.deepToString(GT));
	}
	
	public static void main(String[] args) {
		Millionaires alice = new Millionaires("Alice", 4);
		Millionaires bob = new Millionaires("Bob", 7);
		alice.start();
		bob.start();
	}
}
