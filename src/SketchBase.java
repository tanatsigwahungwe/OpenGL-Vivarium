//****************************************************************************
// SketchBase.  
//****************************************************************************
// Comments : 
//   Subroutines to manage and draw points, lines an triangles
//
// Author : Tanatsigwa Hungwe
// December 2016
//

import java.awt.image.BufferedImage;
import java.util.*;

public class SketchBase 
{
	public SketchBase()
	{
		// deliberately left blank
	}
	
	/**********************************************************************
	 * Draws a point.
	 * This is achieved by changing the color of the buffer at the location
	 * corresponding to the point. 
	 * 
	 * @param buff
	 *          Buffer object.
	 * @param p
	 *          Point to be drawn.
	 */
	public static void drawPoint(BufferedImage buff, Point2D p, int [][] depthbuff)
	{
		if(p.x>=0 && p.x<buff.getWidth() && p.y>=0 && p.y < buff.getHeight())
		{
			int z = p.z;
			if(depthbuff[p.x][(buff.getHeight()-p.y-1)] < z)
			{
				buff.setRGB(p.x, buff.getHeight()-p.y-1, p.c.getRGB_int());
				depthbuff[p.x][(buff.getHeight()-p.y-1)] = z;
			}
		}
	}
	
	/**********************************************************************
	 * Draws a line segment using Bresenham's algorithm, linearly 
	 * interpolating RGB color along line segment.
	 * This method only uses integer arithmetic.
	 * 
	 * @param buff
	 *          Buffer object.
	 * @param p1
	 *          First given endpoint of the line.
	 * @param p2
	 *          Second given endpoint of the line.
	 */
	public static void drawLine(BufferedImage buff, Point2D p1, Point2D p2, int [][] depthbuff)
	{
	    int x0=p1.x, y0=p1.y;
	    int xEnd=p2.x, yEnd=p2.y;
	    int dx = Math.abs(xEnd - x0),  dy = Math.abs(yEnd - y0);
	    
	    if(dx==0 && dy==0)
	    {
	    	drawPoint(buff,p1, depthbuff);
	    	return;
	    }
	    
	    // if slope is greater than 1, then swap the role of x and y
	    boolean x_y_role_swapped = (dy > dx); 
	    if(x_y_role_swapped)
	    {
	    	x0=p1.y; 
	    	y0=p1.x;
	    	xEnd=p2.y; 
	    	yEnd=p2.x;
	    	dx = Math.abs(xEnd - x0);
	    	dy = Math.abs(yEnd - y0);
	    }
	    
	    // initialize the decision parameter and increments
	    int p = 2 * dy - dx;
	    int twoDy = 2 * dy,  twoDyMinusDx = 2 * (dy - dx);
	    int x=x0, y=y0;
	    
	    // set step increment to be positive or negative
	    int step_x = x0<xEnd ? 1 : -1;
	    int step_y = y0<yEnd ? 1 : -1;
	    
	    // deal with setup for color interpolation
	    // first get r,g,b integer values at the end points
	    int r0=p1.c.getR_int(), rEnd=p2.c.getR_int();
	    int g0=p1.c.getG_int(), gEnd=p2.c.getG_int();
	    int b0=p1.c.getB_int(), bEnd=p2.c.getB_int();
	    
	 ///   // deal with setup for depth interpolation
	    // first get z integer values at the end points
	    int z0=p1.z, zEnd=p2.z;
	    
	    // compute the change in r,g,b 
	    int dr=Math.abs(rEnd-r0), dg=Math.abs(gEnd-g0), db=Math.abs(bEnd-b0);
	    
	///    // compute the change in z 
	    int dz=Math.abs(zEnd-z0);
	    
	///    // set step increment to be positive or negative 
	    int step_r = r0<rEnd ? 1 : -1;
	    int step_g = g0<gEnd ? 1 : -1;
	    int step_b = b0<bEnd ? 1 : -1;
	    
	    int step_z = z0<zEnd ? 1 : -1;
	    
	///    // compute whole step in each color that is taken each time through loop
	    int whole_step_r = step_r*(dr/dx);
	    int whole_step_g = step_g*(dg/dx);
	    int whole_step_b = step_b*(db/dx);
	    
	///   // compute remainder, which will be corrected depending on decision parameter
	    dr=dr%dx;
	    dg=dg%dx; 
	    db=db%dx;
	    
	    // initialize decision parameters for red, green, and blue
	    int p_r = 2 * dr - dx;
	    int twoDr = 2 * dr,  twoDrMinusDx = 2 * (dr - dx);
	    int r=r0;
	    
	    int p_g = 2 * dg - dx;
	    int twoDg = 2 * dg,  twoDgMinusDx = 2 * (dg - dx);
	    int g=g0;
	    
	    int p_b = 2 * db - dx;
	    int twoDb = 2 * db,  twoDbMinusDx = 2 * (db - dx);
	    int b=b0;
	    
	///	Init depth 
	    int z=z0;
	    
	    // draw start pixel
	    if(x_y_role_swapped)
	    {
	    	if(x>=0 && x<buff.getHeight() && y>=0 && y<buff.getWidth())
	    	{
	    		// Check for the interpolated z value vs. the depth buffer value stored in memory
	    		if(z > depthbuff[y][buff.getHeight()-x-1])
	    		{
	    			buff.setRGB(y, buff.getHeight()-x-1, (r<<16) | (g<<8) | b);
	    			depthbuff[y][buff.getHeight()-x-1] = z;
	    		}
	    	}
	    }
	    else
	    {
	    	if(y>=0 && y<buff.getHeight() && x>=0 && x<buff.getWidth())
	    	{
	    		// Check for the interpolated z value vs. the depth buffer value stored in memory
	    		if(z > depthbuff[x][buff.getHeight()-y-1])
	    		{
		    		buff.setRGB(x, buff.getHeight()-y-1, (r<<16) | (g<<8) | b);
	    			depthbuff[x][buff.getHeight()-y-1] = z;
	    		}
	    	}
	    }
	    
	    while (x != xEnd) 
	    {
	    	// increment x and y values
	    	x+=step_x;
	    	if (p < 0)
	    		p += twoDy;
	    	else 
	    	{
	    		y+=step_y;
	    		p += twoDyMinusDx;
	    	}
		        
	    	// increment r by whole amount slope_r, and correct for accumulated error if needed
	    	r+=whole_step_r;
	    	if (p_r < 0)
	    		p_r += twoDr;
	    	else 
	    	{
	    		r+=step_r;
	    		p_r += twoDrMinusDx;
	    	}
		    
	    	// increment g by whole amount slope_b, and correct for accumulated error if needed  
	    	g+=whole_step_g;
	    	if (p_g < 0)
	    		p_g += twoDg;
	    	else 
	    	{
	    		g+=step_g;
	    		p_g += twoDgMinusDx;
	    	}
		    
	    	// increment b by whole amount slope_b, and correct for accumulated error if needed
	    	b+=whole_step_b;
	    	if (p_b < 0)
	    		p_b += twoDb;
	    	else 
	    	{
	    		b+=step_b;
	    		p_b += twoDbMinusDx;
	    	}
	    	
	    	// increment z by whole amount slope_z, and correct for accumulated error if needed
	    	z+=step_z;
	    	
	    	if(x_y_role_swapped)
	    	{
		    	if(x>=0 && x<buff.getHeight() && y>=0 && y<buff.getWidth())
		    	{
		    		// Check for the interpolated z value vs. the depth buffer value stored in memory
		    		if(z > depthbuff[y][buff.getHeight()-x-1])
		    		{
		    			buff.setRGB(y, buff.getHeight()-x-1, (r<<16) | (g<<8) | b);
		    			depthbuff[y][buff.getHeight()-x-1] = z;
		    		}
		    	}
	    	}
	    	else
	    	{
		    	if(y>=0 && y<buff.getHeight() && x>=0 && x<buff.getWidth())
		    	{
		    		// Check for the interpolated z value vs. the depth buffer value stored in memory
		    		if(z > depthbuff[x][buff.getHeight()-y-1])
		    		{
			    		buff.setRGB(x, buff.getHeight()-y-1, (r<<16) | (g<<8) | b);
		    			depthbuff[x][buff.getHeight()-y-1] = z;
		    		}
		    	}
	    	}
	    }
	}

	/**********************************************************************
	 * Draws a filled triangle. 
	 * The triangle may be filled using flat fill or smooth fill. 
	 * This routine fills columns of pixels within the left-hand part, 
	 * and then the right-hand part of the triangle.
	 *   
	 *	                         *
	 *	                        /|\
	 *	                       / | \
	 *	                      /  |  \
	 *	                     *---|---*
	 *	            left-hand       right-hand
	 *	              part             part
	 *
	 * @param buff
	 *          Buffer object.
	 * @param p1
	 *          First given vertex of the triangle.
	 * @param p2
	 *          Second given vertex of the triangle.
	 * @param p3
	 *          Third given vertex of the triangle.
	 * @param do_smooth
	 *          Flag indicating whether flat fill or smooth fill should be used.                   
	 */
	public static void drawTriangle(BufferedImage buff, Point2D p1, Point2D p2, Point2D p3, boolean do_smooth, int [][] depthbuff)
	{
	    // sort the triangle vertices by ascending x value
	    Point2D p[] = sortTriangleVerts(p1,p2,p3);
	    
	    int x; 
	    float y_a, y_b;
	    float dy_a, dy_b;
	    float dr_a=0, dg_a=0, db_a=0, dr_b=0, dg_b=0, db_b=0;
	    
	    
	    Point2D side_a = new Point2D(p[0]), side_b = new Point2D(p[0]);
	    
	    if(!do_smooth)
	    {
	    	side_a.c = new ColorType(p1.c);
	    	side_b.c = new ColorType(p1.c);
	    }
	    
	    y_b = p[0].y;
	    dy_b = ((float)(p[2].y - p[0].y))/(p[2].x - p[0].x);
	    
	    if(do_smooth)
	    {
	///    	// calculate slopes in r, g, b for segment b
	    	dr_b = ((float)(p[2].c.r - p[0].c.r))/(p[2].x - p[0].x);
	    	dg_b = ((float)(p[2].c.g - p[0].c.g))/(p[2].x - p[0].x);
	    	db_b = ((float)(p[2].c.b - p[0].c.b))/(p[2].x - p[0].x);
	    }
	    
	    // if there is a left-hand part to the triangle then fill it
	    if(p[0].x != p[1].x)
	    {
	    	y_a = p[0].y;
	    	dy_a = ((float)(p[1].y - p[0].y))/(p[1].x - p[0].x);
		    
	    	if(do_smooth)
	    	{
	   /// 		// calculate slopes in r, g, b for segment a
	    		dr_a = ((float)(p[1].c.r - p[0].c.r))/(p[1].x - p[0].x);
	    		dg_a = ((float)(p[1].c.g - p[0].c.g))/(p[1].x - p[0].x);
	    		db_a = ((float)(p[1].c.b - p[0].c.b))/(p[1].x - p[0].x);
	    	}
		    
		    // loop over the columns for left-hand part of triangle
		    // filling from side a to side b of the span
		    for(x = p[0].x; x < p[1].x; ++x)
		    {
		    	drawLine(buff, side_a, side_b, depthbuff);

		    	++side_a.x;
		    	++side_b.x;
		    	y_a += dy_a;
		    	y_b += dy_b;
		    	side_a.y = (int)y_a;
		    	side_b.y = (int)y_b;
		    	if(do_smooth)
		    	{
		    		side_a.c.r +=dr_a;
		    		side_b.c.r +=dr_b;
		    		side_a.c.g +=dg_a;
		    		side_b.c.g +=dg_b;
		    		side_a.c.b +=db_a;
		    		side_b.c.b +=db_b;
		    	}
		    }
	    }
	    
	    // there is no right-hand part of triangle
	    if(p[1].x == p[2].x)
	    	return;
	    
	    // set up to fill the right-hand part of triangle 
	    // replace segment a
	    side_a = new Point2D(p[1]);
	    if(!do_smooth)
	    	side_a.c =new ColorType(p1.c);
	    
	    y_a = p[1].y;
	    dy_a = ((float)(p[2].y - p[1].y))/(p[2].x - p[1].x);
	    if(do_smooth)
	    {
	    	// calculate slopes in r, g, b for replacement for segment a
	    	dr_a = ((float)(p[2].c.r - p[1].c.r))/(p[2].x - p[1].x);
	    	dg_a = ((float)(p[2].c.g - p[1].c.g))/(p[2].x - p[1].x);
	    	db_a = ((float)(p[2].c.b - p[1].c.b))/(p[2].x - p[1].x);
	    }

	    // loop over the columns for right-hand part of triangle
	    // filling from side a to side b of the span
	    for(x = p[1].x; x <= p[2].x; ++x)
	    {
	    	drawLine(buff, side_a, side_b, depthbuff);
		    
	    	++side_a.x;
	    	++side_b.x;
	    	y_a += dy_a;
	    	y_b += dy_b;
	    	side_a.y = (int)y_a;
	    	side_b.y = (int)y_b;
	    	if(do_smooth)
	    	{
	    		side_a.c.r +=dr_a;
	    		side_b.c.r +=dr_b;
	    		side_a.c.g +=dg_a;
	    		side_b.c.g +=dg_b;
	    		side_a.c.b +=db_a;
	    		side_b.c.b +=db_b;
	    	}
	    }
	}

	/**********************************************************************
	 * Helper function to bubble sort triangle vertices by ascending x value.
	 * 
	 * @param p1
	 *          First given vertex of the triangle.
	 * @param p2
	 *          Second given vertex of the triangle.
	 * @param p3
	 *          Third given vertex of the triangle.
	 * @return 
	 *          Array of 3 points, sorted by ascending x value.
	 */
	private static Point2D[] sortTriangleVerts(Point2D p1, Point2D p2, Point2D p3)
	{
	    Point2D pts[] = {p1, p2, p3};
	    Point2D tmp;
	    int j=0;
	    boolean swapped = true;
	         
	    while (swapped) 
	    {
	    	swapped = false;
	    	j++;
	    	for (int i = 0; i < 3 - j; i++) 
	    	{                                       
	    		if (pts[i].x > pts[i + 1].x) 
	    		{                          
	    			tmp = pts[i];
	    			pts[i] = pts[i + 1];
	    			pts[i + 1] = tmp;
	    			swapped = true;
	    		}
	    	}                
	    }
	    return(pts);
	}
	
	
	
	
	
	
	
	
	

	
	
	// Phong Implementations (no need for draw point)
	// Similar implementation but overloaded
	
	public static void drawLine(BufferedImage buff, Point2D p1, Point2D p2, int [][] depthbuff, Light lights, Material mats, Vector3D view_vector)
	{
	    int x0=p1.x, y0=p1.y;
	    int xEnd=p2.x, yEnd=p2.y;
	    int dx = Math.abs(xEnd - x0),  dy = Math.abs(yEnd - y0);
	    
	    if(dx==0 && dy==0)
	    {
	    	drawPoint(buff,p1, depthbuff);
	    	return;
	    }
	    
	    // if slope is greater than 1, then swap the role of x and y
	    boolean x_y_role_swapped = (dy > dx); 
	    if(x_y_role_swapped)
	    {
	    	x0=p1.y; 
	    	y0=p1.x;
	    	xEnd=p2.y; 
	    	yEnd=p2.x;
	    	dx = Math.abs(xEnd - x0);
	    	dy = Math.abs(yEnd - y0);
	    }
	    
	    // initialize the decision parameter and increments
	    int p = 2 * dy - dx;
	    int twoDy = 2 * dy,  twoDyMinusDx = 2 * (dy - dx);
	    int x=x0, y=y0;
	    
	    // set step increment to be positive or negative
	    int step_x = x0<xEnd ? 1 : -1;
	    int step_y = y0<yEnd ? 1 : -1;
	    
	    // deal with setup for normal interpolation
	    // first get x,y,z integer values at the end points
	    float r0=p1.normal.x, rEnd=p2.normal.x;
	    float g0=p1.normal.y, gEnd=p2.normal.y;
	    float b0=p1.normal.z, bEnd=p2.normal.z;
	    
	 ///   // deal with setup for depth interpolation
	    // first get z integer values at the end points
	    int z0=p1.z, zEnd=p2.z;
	    
	    // compute the change in r,g,b 
	    float dr=Math.abs(rEnd-r0), dg=Math.abs(gEnd-g0), db=Math.abs(bEnd-b0);
	    
	///    // compute the change in z 
	    int dz=Math.abs(zEnd-z0);
	    
	///    // set step increment to be positive or negative 
	    int step_r = r0<rEnd ? 1 : -1;
	    int step_g = g0<gEnd ? 1 : -1;
	    int step_b = b0<bEnd ? 1 : -1;
	    
	    int step_z = z0<zEnd ? 1 : -1;
	    
	///    // compute whole step in each color that is taken each time through loop
	    float whole_step_r = step_r*(dr/dx);
	    float whole_step_g = step_g*(dg/dx);
	    float whole_step_b = step_b*(db/dx);
	    
	///   // compute remainder, which will be corrected depending on decision parameter
	    dr=dr%dx;
	    dg=dg%dx; 
	    db=db%dx;
	    
	    // initialize decision parameters for red, green, and blue
	    float p_r = 2 * dr - dx;
	    float twoDr = 2 * dr,  twoDrMinusDx = 2 * (dr - dx);
	    float r=r0;
	    
	    float p_g = 2 * dg - dx;
	    float twoDg = 2 * dg,  twoDgMinusDx = 2 * (dg - dx);
	    float g=g0;
	    
	    float p_b = 2 * db - dx;
	    float twoDb = 2 * db,  twoDbMinusDx = 2 * (db - dx);
	    float b=b0;
	    
	///	Init depth 
	    int z=z0;
	    
	    // draw start pixel
	    if(x_y_role_swapped)
	    {
	    	if(x>=0 && x<buff.getHeight() && y>=0 && y<buff.getWidth())
	    	{
	    		// Check for the interpolated z value vs. the depth buffer value stored in memory
	    		if(z > depthbuff[y][buff.getHeight()-x-1])
	    		{
	    			//Apply light via light equation
	    			// Set the frame buff to appropriate colors
	    			ColorType color = new ColorType();
	    			color = lights.applyLight(mats, view_vector, new Vector3D((float)r,(float)g,(float)b), new Vector3D(y,buff.getHeight()-x-1,z));
	    			buff.setRGB(y, buff.getHeight()-x-1, (color.getR_int()<<16) | (color.getG_int()<<8) | color.getB_int());
	    			depthbuff[y][buff.getHeight()-x-1] = z;
	    		}
	    	}
	    }
	    else
	    {
	    	if(y>=0 && y<buff.getHeight() && x>=0 && x<buff.getWidth())
	    	{
	    		// Check for the interpolated z value vs. the depth buffer value stored in memory
	    		if(z > depthbuff[x][buff.getHeight()-y-1])
	    		{
	    			//Apply light via light equation
	    			// Set the frame buff to appropriate colors
	    			ColorType color = new ColorType();
	    			color = lights.applyLight(mats, view_vector, new Vector3D((float)r,(float)g,(float)b), new Vector3D(x,buff.getHeight()-y-1,z));
		    		buff.setRGB(x, buff.getHeight()-y-1, (color.getR_int()<<16) | (color.getG_int()<<8) | color.getB_int());
	    			depthbuff[x][buff.getHeight()-y-1] = z;
	    		}
	    	}
	    }
	    
	    while (x != xEnd) 
	    {
	    	// increment x and y values
	    	x+=step_x;
	    	if (p < 0)
	    		p += twoDy;
	    	else 
	    	{
	    		y+=step_y;
	    		p += twoDyMinusDx;
	    	}
		        
	    	// increment r by whole amount slope_r, and correct for accumulated error if needed
	    	r+=whole_step_r;
	    	if (p_r < 0)
	    		p_r += twoDr;
	    	else 
	    	{
	    		r+=step_r;
	    		p_r += twoDrMinusDx;
	    	}
		    
	    	// increment g by whole amount slope_b, and correct for accumulated error if needed  
	    	g+=whole_step_g;
	    	if (p_g < 0)
	    		p_g += twoDg;
	    	else 
	    	{
	    		g+=step_g;
	    		p_g += twoDgMinusDx;
	    	}
		    
	    	// increment b by whole amount slope_b, and correct for accumulated error if needed
	    	b+=whole_step_b;
	    	if (p_b < 0)
	    		p_b += twoDb;
	    	else 
	    	{
	    		b+=step_b;
	    		p_b += twoDbMinusDx;
	    	}
	    	
	    	// increment z by whole amount slope_z, and correct for accumulated error if needed
	    	z+=step_z;
	    	
	    	if(x_y_role_swapped)
	    	{
		    	if(x>=0 && x<buff.getHeight() && y>=0 && y<buff.getWidth())
		    	{
		    		// Check for the interpolated z value vs. the depth buffer value stored in memory
		    		if(z > depthbuff[y][buff.getHeight()-x-1])
		    		{
		    			//Apply light via light equation
		    			// Set the frame buff to appropriate colors
		    			ColorType color = new ColorType();
		    			color = lights.applyLight(mats, view_vector, new Vector3D((float)r,(float)g,(float)b), new Vector3D(y,buff.getHeight()-x-1,z));
		    			buff.setRGB(y, buff.getHeight()-x-1, (color.getR_int()<<16) | (color.getG_int()<<8) | color.getB_int());
		    			depthbuff[y][buff.getHeight()-x-1] = z;
		    		}
		    	}
	    	}
	    	else
	    	{
		    	if(y>=0 && y<buff.getHeight() && x>=0 && x<buff.getWidth())
		    	{
		    		// Check for the interpolated z value vs. the depth buffer value stored in memory
		    		if(z > depthbuff[x][buff.getHeight()-y-1])
		    		{
		    			//Apply light via light equation
		    			// Set the frame buff to appropriate colors
		    			ColorType color = new ColorType();
		    			color = lights.applyLight(mats, view_vector, new Vector3D((float)r,(float)g,(float)b), new Vector3D(x,buff.getHeight()-y-1,z));
		    			buff.setRGB(x, buff.getHeight()-y-1, (color.getR_int()<<16) | (color.getG_int()<<8) | color.getB_int());
		    			depthbuff[x][buff.getHeight()-y-1] = z;
		    		}
		    	}
	    	}
	    }
	}
	
	
	
	public static void drawTriangle(BufferedImage buff, Point2D p1, Point2D p2, Point2D p3, boolean do_smooth, int [][] depthbuff, boolean do_phong,Vector3D n0, Vector3D n1, Vector3D n2, Light lightfix, Material mats, Vector3D view_vector)
	{
		//Initialize points with normals
		p1.normal = n0;
		p2.normal = n1;
		p3.normal = n2;
		
	    // sort the triangle vertices by ascending x value
	    Point2D p[] = sortTriangleVerts(p1,p2,p3);
	    
	    int x; 
	    float y_a, y_b;
	    float dy_a, dy_b;
	    float dr_a=0, dg_a=0, db_a=0, dr_b=0, dg_b=0, db_b=0;
	    
	    
	    Point2D side_a = new Point2D(p[0]), side_b = new Point2D(p[0]);
	    
	    y_b = p[0].y;
	    dy_b = ((float)(p[2].y - p[0].y))/(p[2].x - p[0].x);
	 
	///    	// calculate slopes in normals
	    	dr_b = ((float)(p[2].normal.x - p[0].normal.x))/(p[2].x - p[0].x);
	    	dg_b = ((float)(p[2].normal.y - p[0].normal.y))/(p[2].x - p[0].x);
	    	db_b = ((float)(p[2].normal.z - p[0].normal.z))/(p[2].x - p[0].x);
	    
	    // if there is a left-hand part to the triangle then fill it
	    if(p[0].x != p[1].x)
	    {
	    	y_a = p[0].y;
	    	dy_a = ((float)(p[1].y - p[0].y))/(p[1].x - p[0].x);

	   /// 		// calculate slopes in normals of other side
	    		dr_a = ((float)(p[1].normal.x - p[0].normal.x))/(p[1].x - p[0].x);
	    		dg_a = ((float)(p[1].normal.y - p[0].normal.y))/(p[1].x - p[0].x);
	    		db_a = ((float)(p[1].normal.z - p[0].normal.z))/(p[1].x - p[0].x);
		    
		    // loop over the columns for left-hand part of triangle
		    // filling from side a to side b of the span
	    		// Interpolate normal values
		    for(x = p[0].x; x < p[1].x; ++x)
		    {
		    	drawLine(buff, side_a, side_b, depthbuff, lightfix, mats, view_vector);

		    	++side_a.x;
		    	++side_b.x;
		    	y_a += dy_a;
		    	y_b += dy_b;
		    	side_a.y = (int)y_a;
		    	side_b.y = (int)y_b;
		    	side_a.normal.x +=dr_a;
	    		side_b.normal.x +=dr_b;
	    		side_a.normal.y +=dg_a;
	    		side_b.normal.y +=dg_b;
	    		side_a.normal.z +=db_a;
	    		side_b.normal.z +=db_b;
		    }
	    }
	    
	    // there is no right-hand part of triangle
	    if(p[1].x == p[2].x)
	    	return;
	    
	    // set up to fill the right-hand part of triangle 
	    // replace segment a
	    side_a = new Point2D(p[1]);
	    
	    y_a = p[1].y;
	    dy_a = ((float)(p[2].y - p[1].y))/(p[2].x - p[1].x);
	    
    	// calculate slopes in r, g, b for replacement for segment a
    	dr_a = ((float)(p[2].normal.x - p[1].normal.x))/(p[2].x - p[1].x);
    	dg_a = ((float)(p[2].normal.y - p[1].normal.y))/(p[2].x - p[1].x);
    	db_a = ((float)(p[2].normal.z - p[1].normal.z))/(p[2].x - p[1].x);

	    // loop over the columns for right-hand part of triangle
	    // filling from side a to side b of the span
	    for(x = p[1].x; x <= p[2].x; ++x)
	    {
	    	drawLine(buff, side_a, side_b, depthbuff, lightfix, mats, view_vector);
		    
	    	++side_a.x;
	    	++side_b.x;
	    	y_a += dy_a;
	    	y_b += dy_b;
	    	side_a.y = (int)y_a;
	    	side_b.y = (int)y_b;
    		side_a.normal.x +=dr_a;
    		side_b.normal.x +=dr_b;
    		side_a.normal.y +=dg_a;
    		side_b.normal.y +=dg_b;
    		side_a.normal.z +=db_a;
    		side_b.normal.z +=db_b;
	    }
	}
}