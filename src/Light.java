//****************************************************************************
//       Light source class, where all lights get these shared functions
//****************************************************************************
// Author : Tanatsigwa Hungwe
// December 2016
//
public interface Light 
{
	public ColorType applyLight(Material mat, Vector3D v, Vector3D n, Point2D p);
	public ColorType applyLight(Material mat, Vector3D v, Vector3D n, Vector3D p);
	public ColorType applyLight(Material mat, Vector3D v, Vector3D n);
}
