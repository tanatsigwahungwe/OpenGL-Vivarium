//****************************************************************************
//       material class
//****************************************************************************
// Author : Tanatsigwa Hungwe
// December 2016
//
public class Material 
{
	public ColorType ka, kd, ks;
	public int ns;
	public boolean specular, diffuse, ambient;
	
	public Material(ColorType _ka, ColorType _kd, ColorType _ks, boolean [] matTerms, int _ns)
	{
		ks = new ColorType(_ks);  // specular coefficient for r,g,b
		ka = new ColorType(_ka);  // ambient coefficient for r,g,b
		kd = new ColorType(_kd);  // diffuse coefficient for r,g,b
		ns = _ns;  // specular exponent
		
		// set boolean variables 
		specular = (matTerms[0] && (ns>0 && (ks.r > 0.0 || ks.g > 0.0 || ks.b > 0.0)));
		diffuse = (matTerms[1] && (kd.r > 0.0 || kd.g > 0.0 || kd.b > 0.0));
		ambient = (matTerms[2] && (ka.r > 0.0 || ka.g > 0.0 || ka.b > 0.0));
	}
}