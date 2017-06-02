//****************************************************************************
//       2D Point Class from PA1
//****************************************************************************
// Author : Tanatsigwa Hungwe
// December 2016
//

public class Point2D
{
	public int x, y;
	public float u, v; // uv coordinates for texture mapping
	public ColorType c;
	public Vector3D normal; // normal for the point for Phong
	
	// coordinates for z space
	public int z;
	
	public Point2D(int _x, int _y, ColorType _c)
	{
		u = 0;
		v = 0;
		x = _x;
		y = _y;
		c = _c;
		normal = new Vector3D();
	}
	public Point2D(int _x, int _y, ColorType _c, float _u, float _v)
	{
		u = _u;
		v = _v;
		x = _x;
		y = _y;
		c = _c;
		normal = new Vector3D();
	}
	public Point2D()
	{
		c = new ColorType(1.0f, 1.0f, 1.0f);
		normal = new Vector3D();
	}
	public Point2D( Point2D p)
	{
		u = p.u;
		v = p.v;
		x = p.x;
		y = p.y;
		z = p.z;
		c = new ColorType(p.c.r, p.c.g, p.c.b);
		normal = new Vector3D();
	}
	public Point2D(int _x, int _y)
	{
		x = _x;
		y = _y;
		normal = new Vector3D();
	}
}