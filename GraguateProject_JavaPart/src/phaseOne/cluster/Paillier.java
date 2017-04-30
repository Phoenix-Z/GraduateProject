package phaseOne.cluster;

import java.math.BigInteger;
import java.util.Random;

public class Paillier {

	/**
	 * @param p 大素数
	 * @param q 大素数
	 * @param lambda lambda = lcm(p-1, q-1)=(p-1)*(q-1) / gcd(p-1, q-1).
	 */
	private BigInteger p, q, lambda;
	
	/**
	 * @param n n = p * q
	 * @param squareN squareN = n ^ 2
	 */
	public BigInteger n, squareN;
	
	/**
	 * @param g 一个属于Z*_{n^2}的随机数，其中gcd(L(g^lambda mod n^2)) = 1.
	 */
	private BigInteger g;
	
	/**
	 * @param bitLength 模数位数
	 */
	private int bitLength;
	
	public Paillier(int bitLength, int certainty) {
		KeyGeneration(bitLength, certainty);
	}
	
	public Paillier() {
		KeyGeneration(512, 64);
	}
	
	private BigInteger functionL(BigInteger u, BigInteger n) {
		return u.subtract(BigInteger.ONE).divide(n);
	}
	/**
	 * 生成公私密钥对
	 * @param bitLength 模数长度
	 * @param certainty 新BigInteger代表素数的概率将超过(1-2^(-certainty)),该构造函数的执行时间与该参数的值成比例。
	 */
	private void KeyGeneration(int bitLength, int certainty) {
		this.bitLength = bitLength;
		/*
		 * 生成两个随机大素数p和q 
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
	 * 加密明文，得到密文c = g^m * r^n mod n^2. 这个函数显示需求一个随机数来辅助加密。
	 * @param plaintext 明文
	 * @param rnd 随机数
	 * @return 密文
	 */
	public BigInteger Encryption(BigInteger plaintext, BigInteger rnd) {
		return g.modPow(plaintext, squareN).multiply(rnd.modPow(n, squareN)).mod(squareN);
	}
	
	/**
	 * 重载版本，不需要外部提供随机数
	 * @param plaintext 明文
	 * @return 密文
	 */
	public BigInteger Encryption(BigInteger plaintext) {
		BigInteger rnd = null;
		do {
			rnd = new BigInteger(bitLength, new Random());
		} while(rnd.compareTo(n) >= 0);
		return Encryption(plaintext, rnd);
	}
	
	/**
	 * 解密密文，得到明文m = L(c^lambda mod n^2) * u mod n, 其中u = (L(g^lambda mod n^2))^(-1) mod n.
	 * @param ciphertext 密文
	 * @return 明文
	 */
	public BigInteger Decryption(BigInteger ciphertext) {
		BigInteger u = g.modPow(lambda, squareN).subtract(BigInteger.ONE).divide(n).modInverse(n); 
		return ciphertext.modPow(lambda, squareN).subtract(BigInteger.ONE).divide(n).multiply(u).mod(n);
	}
	
	/**
	 * 密文em1和em2之和
	 * @param em1 密文1
	 * @param em2 密文2
	 * @return 密文之和
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
