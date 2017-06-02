//****************************************************************************
//       Point light source class
//****************************************************************************
// Author : Tanatsigwa Hungwe
// December 2016
//
public class PointLight implements Light
{
	public Vector3D direction;
	public ColorType color;
	// Add direction
	public Vector3D position;
	
	public PointLight(ColorType _c, Vector3D _direction, Vector3D _position)
	{
		color = new ColorType(_c);
		direction = new Vector3D(_direction);
		position = new Vector3D(_position);	
	}
	
	// overloaded method
	// no direction, then do not apply any light to the scene
	public ColorType applyLight(Material mat, Vector3D v, Vector3D n)
	{
		ColorType blank = new ColorType(0,0,0);
		return blank;
	}
	
	// apply this light source to the vertex / normal, given material
	// return resulting color value
	// inherited from Light class
	
	public ColorType applyLight(Material mat, Vector3D v, Vector3D n, Point2D p){
		ColorType res = new ColorType();
		
		// Calculate and add ambient light
		if(mat.ambient)
		{
			res.r = (float)mat.ka.r*color.r; 
			res.g = (float)mat.ka.g*color.g;
			res.b = (float)mat.ka.b*color.b; 
		}
		
		// variable to calculate direction of light and distance from point for attenuation purposes
		this.direction.x = position.x - p.x;
		this.direction.y = position.x - p.y;
		this.direction.z = position.x - p.z;
		
		float distance = (float)Math.sqrt((direction.x*direction.x) + (direction.y*direction.y) + (direction.z*direction.z));
		
		
		// dot product between light direction and normal
		// light must be facing in the positive direction
		// dot <= 0.0 implies this light is facing away (not toward) this point
		// therefore, light only contributes if dot > 0.0
		double dot = direction.dotProduct(n);
		if(dot>0.0)
		{
			// diffuse component
			if(mat.diffuse)
			{
				res.r += (float)(dot*mat.kd.r*color.r);
				res.g += (float)(dot*mat.kd.g*color.g);
				res.b += (float)(dot*mat.kd.b*color.b);
			}
			// specular component
			if(mat.specular)
			{
				Vector3D r = direction.reflect(n);
				dot = r.dotProduct(v);
				if(dot>0.0)
				{
					res.r += (float)Math.pow((dot*mat.ks.r*color.r),mat.ns);
					res.g += (float)Math.pow((dot*mat.ks.g*color.g),mat.ns);
					res.b += (float)Math.pow((dot*mat.ks.b*color.b),mat.ns);
				}
			}
			// clamp so that allowable maximum illumination level is not exceeded
			res.r = (float) Math.min(1.0, res.r);
			res.g = (float) Math.min(1.0, res.g);
			res.b = (float) Math.min(1.0, res.b);
		}
		return(res);
	}
	
	public ColorType applyLight(Material mat, Vector3D v, Vector3D n, Vector3D p){
		ColorType res = new ColorType();
		
		// Calculate and add ambient light
		if(mat.ambient)
		{
			res.r = (float)mat.ka.r*color.r; 
			res.g = (float)mat.ka.g*color.g;
			res.b = (float)mat.ka.b*color.b; 
		}
		
		// variable to calculate direction of light and distance from point for attenuation purposes
		this.direction.x = position.x - p.x;
		this.direction.y = position.x - p.y;
		this.direction.z = position.x - p.z;
		
		float distance = (float)Math.sqrt((direction.x*direction.x) + (direction.y*direction.y) + (direction.z*direction.z));
		
		
		// dot product between light direction and normal
		// light must be facing in the positive direction
		// dot <= 0.0 implies this light is facing away (not toward) this point
		// therefore, light only contributes if dot > 0.0
		double dot = direction.dotProduct(n);
		if(dot>0.0)
		{
			// diffuse component
			if(mat.diffuse)
			{
				res.r += (float)(dot*mat.kd.r*color.r);
				res.g += (float)(dot*mat.kd.g*color.g);
				res.b += (float)(dot*mat.kd.b*color.b);
			}
			// specular component
			if(mat.specular)
			{
				Vector3D r = direction.reflect(n);
				dot = r.dotProduct(v);
				if(dot>0.0)
				{
					res.r += (float)Math.pow((dot*mat.ks.r*color.r),mat.ns);
					res.g += (float)Math.pow((dot*mat.ks.g*color.g),mat.ns);
					res.b += (float)Math.pow((dot*mat.ks.b*color.b),mat.ns);
				}
			}
			// clamp so that allowable maximum illumination level is not exceeded
			res.r = (float) Math.min(1.0, res.r);
			res.g = (float) Math.min(1.0, res.g);
			res.b = (float) Math.min(1.0, res.b);
		}
		return(res);
	}
}
