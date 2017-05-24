package phaseOne.cluster;

import java.math.BigInteger;
import java.util.Random;

public class Paillier {

	/**
	 * @param p ������
	 * @param q ������
	 * @param lambda lambda = lcm(p-1, q-1)=(p-1)*(q-1) / gcd(p-1, q-1).
	 */
	private BigInteger p, q, lambda;
	
	/**
	 * @param n n = p * q
	 * @param squareN squareN = n ^ 2
	 */
	public BigInteger n, squareN;
	
	/**
	 * @param g һ������Z*_{n^2}�������������gcd(L(g^lambda mod n^2)) = 1.
	 */
	private BigInteger g;
	
	/**
	 * @param bitLength ģ��λ��
	 */
	private int bitLength;
	
	public Paillier(int bitLength, int certainty) {
		KeyGeneration(bitLength, certainty);
	}
	
	public Paillier() {
		KeyGeneration(512, 64);
	}
	
	@SuppressWarnings("unused")
	private BigInteger functionL(BigInteger u, BigInteger n) {
		return u.subtract(BigInteger.ONE).divide(n);
	}
	/**
	 * ���ɹ�˽��Կ��
	 * @param bitLength ģ������
	 * @param certainty ��BigInteger���������ĸ��ʽ�����(1-2^(-certainty)),�ù��캯����ִ��ʱ����ò�����ֵ�ɱ�����
	 */
	private void KeyGeneration(int bitLength, int certainty) {
		this.bitLength = bitLength;
		/*
		 * �����������������p��q 
		 */
		p = new BigInteger(bitLength, certainty, new Random());
		q = new BigInteger(bitLength, certainty, new Random());
		
		n = p.multiply(q);
		squareN = n.pow(2);
		
		Random rng = new Random();
		lambda = p.subtract(BigInteger.ONE).multiply(q.subtract(BigInteger.ONE))
				.divide(p.subtract(BigInteger.ONE).gcd(q.subtract(BigInteger.ONE)));
		g = new BigInteger("2");
//		g = new BigInteger(bitLength, rng);
//		while(!functionL(g.modPow(lambda, squareN), n).gcd(n).equals(BigInteger.ONE)) {
//			g = new BigInteger(bitLength, rng);
//		}
		
	}
	
	/**
	 * �������ģ��õ�����c = g^m * r^n mod n^2. ���������ʾ����һ����������������ܡ�
	 * @param plaintext ����
	 * @param rnd �����
	 * @return ����
	 */
	public BigInteger Encryption(BigInteger plaintext, BigInteger rnd) {
		return g.modPow(plaintext, squareN).multiply(rnd.modPow(n, squareN)).mod(squareN);
	}
	
	/**
	 * ���ذ汾������Ҫ�ⲿ�ṩ�����
	 * @param plaintext ����
	 * @return ����
	 */
	public BigInteger Encryption(BigInteger plaintext) {
		BigInteger rnd = null;
		do {
			rnd = new BigInteger(bitLength, new Random());
		} while(rnd.compareTo(n) >= 0);
		return Encryption(plaintext, rnd);
	}
	
	/**
	 * �������ģ��õ�����m = L(c^lambda mod n^2) * u mod n, ����u = (L(g^lambda mod n^2))^(-1) mod n.
	 * @param ciphertext ����
	 * @return ����
	 */
	public BigInteger Decryption(BigInteger ciphertext) {
		BigInteger u = g.modPow(lambda, squareN).subtract(BigInteger.ONE).divide(n).modInverse(n); 
		return ciphertext.modPow(lambda, squareN).subtract(BigInteger.ONE).divide(n).multiply(u).mod(n);
	}
	
	/**
	 * ����em1��em2֮��
	 * @param em1 ����1
	 * @param em2 ����2
	 * @return ����֮��
	 */
	public BigInteger cipher_add(BigInteger em1, BigInteger em2) {
		return em1.multiply(em2).mod(squareN);
	}
	
	public static void main(String[] args) {
		Paillier paillier = new Paillier();
		BigInteger numA = new BigInteger("300");
		int numB = 20;
		BigInteger c = paillier.Encryption(numA);
		Random rnd = new Random();
		int u = Math.abs(rnd.nextInt(10));
		int w = Math.abs(rnd.nextInt(5));
//		System.out.println(u + " " + w);
		int v;
		do{
			v = Math.abs(rnd.nextInt(5));
		} while(Math.abs(v - w) >= u);
		BigInteger X = c.pow(u).multiply(paillier.Encryption(new BigInteger(Integer.toString(v))));
		BigInteger Y = paillier.Encryption(new BigInteger(Integer.toString(u * numB + w)));
		BigInteger decrypt_X = paillier.Decryption(X);
		BigInteger decrypt_Y = paillier.Decryption(Y);
		if(decrypt_X.compareTo(decrypt_Y) > 0) {
			System.out.println("a is bigger than b");
		} else {
			System.out.println("a is smaller than b");
		}
	}
}
