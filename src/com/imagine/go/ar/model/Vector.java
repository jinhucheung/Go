package com.imagine.go.ar.model;

/**
 * Vector:向量
 * 
 * @author Jinhu
 * @date 2016/4/11
 */
public class Vector {

	private final float[] tmpMatrix = new float[9];

	private volatile float x = 0f;
	private volatile float y = 0f;
	private volatile float z = 0f;

	public Vector() {
		this(0, 0, 0);
	}

	public Vector(float x, float y, float z) {
		set(x, y, z);
	}

	public synchronized void set(float x, float y, float z) {
		this.x = x;
		this.y = y;
		this.z = z;
	}

	public void set(Vector v) {
		if (null == v)
			return;
		set(v.x, v.y, v.z);
	}

	public void set(float[] array) {
		if (null == array || 3 != array.length)
			throw new IllegalArgumentException(
					"get() array must be non-NULL and size of 3");
		set(array[0], array[1], array[2]);
	}

	public synchronized float getX() {
		return x;
	}

	public synchronized void setX(float x) {
		this.x = x;
	}

	public synchronized float getY() {
		return y;
	}

	public synchronized void setY(float y) {
		this.y = y;
	}

	public synchronized float getZ() {
		return z;
	}

	public synchronized void setZ(float z) {
		this.z = z;
	}

	public synchronized void get(float[] array) {
		if (null == array || 3 != array.length)
			throw new IllegalArgumentException(
					"get() array must be non-NULL and size of 3");

		array[0] = this.x;
		array[1] = this.y;
		array[2] = this.z;
	}

	@Override
	public synchronized boolean equals(Object obj) {
		if (null == obj)
			return false;
		if (this == obj)
			return true;
		if (obj instanceof Vector) {
			Vector v = (Vector) obj;
			return (this.x == v.x && this.y == v.y && this.z == v.z);
		}
		return false;
	}

	/**
	 * 向量相加
	 * 
	 * @param x
	 * @param y
	 * @param z
	 */
	public synchronized void add(float x, float y, float z) {
		this.x += x;
		this.y += y;
		this.z += z;
	}

	public void add(Vector v) {
		if (null == v)
			return;
		add(v.x, v.y, v.z);
	}

	/**
	 * 向量相减
	 * 
	 * @param v
	 */
	public void sub(Vector v) {
		if (null == v)
			return;
		add(-v.x, -v.y, -v.z);
	}

	/**
	 * 向量数乘
	 * 
	 * @param s
	 */
	public synchronized void mult(float s) {
		this.x *= s;
		this.y *= s;
		this.z *= s;
	}

	/**
	 * 向量数除
	 * 
	 * @param s
	 */
	public synchronized void divide(float s) {
		this.x /= s;
		this.y /= s;
		this.z /= s;
	}

	/**
	 * 向量模
	 * 
	 * @return
	 */
	public synchronized float length() {
		return (float) Math.sqrt(this.x * this.x + this.y * this.y + this.z
				* this.z);
	}

	/**
	 * 单位向量
	 */
	public void norm() {
		divide(length());
	}

	/**
	 * 向量叉乘
	 * 
	 * @param u
	 * @param v
	 */
	public synchronized void cross(Vector u, Vector v) {
		if (null == v || null == u)
			return;

		float x = u.y * v.z - u.z * v.y;
		float y = u.z * v.x - u.x * v.z;
		float z = u.x * v.y - u.y * v.x;

		this.x = x;
		this.y = y;
		this.z = z;
	}

	/**
	 * 矩阵乘向量
	 * 
	 * @param m
	 */
	public synchronized void prod(Matrix m) {
		if (null == m)
			return;

		m.get(tmpMatrix);
		float x = tmpMatrix[0] * this.x + tmpMatrix[1] * this.y + tmpMatrix[2]
				* this.z;
		float y = tmpMatrix[3] * this.x + tmpMatrix[4] * this.y + tmpMatrix[5]
				* this.z;
		float z = tmpMatrix[6] * this.x + tmpMatrix[7] * this.y + tmpMatrix[8]
				* this.z;

		this.x = x;
		this.y = y;
		this.z = z;
	}

	@Override
	public synchronized String toString() {
		return "x = " + this.x + ", y = " + this.y + ", z = " + this.z;
	}
}
