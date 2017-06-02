//****************************************************************************
//       PA4: Shaded Rendering and Depth-Buffering
//****************************************************************************
// Description: 
//   
//   Depth-buffering, and computation of the illumination and shading of triangles
//
//     The following keys control the program:
//
//		Q,q: quit 
//		C,c: clear polygon (set vertex count=0)
//		R,r: randomly change the color
//		S,s: toggle the smooth shading for triangle 
//			 (no smooth shading by default)
//		T,t: show testing examples
//		>:	 increase the step number for examples
//		<:   decrease the step number for examples
//
//****************************************************************************
// Author : Tanatsigwa Hungwe
// December 2016
//


import javax.swing.*;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.event.*; 
import java.awt.image.*;
//import java.io.File;
//import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.*;

//import javax.imageio.ImageIO;
import javax.media.opengl.GL;
import javax.media.opengl.GL2;
import javax.media.opengl.GLAutoDrawable;
import javax.media.opengl.awt.GLCanvas;//for new version of gl
import javax.media.opengl.GLCapabilities;
import javax.media.opengl.GLEventListener;

import com.jogamp.opengl.util.FPSAnimator;//for new version of gl


public class PA4 extends JFrame
	implements GLEventListener, KeyListener, MouseListener, MouseMotionListener
{	
	private static final long serialVersionUID = 1L;
	private final int DEFAULT_WINDOW_WIDTH=512;
	private final int DEFAULT_WINDOW_HEIGHT=512;
	private final float DEFAULT_LINE_WIDTH=1.0f;

	private GLCapabilities capabilities;
	private GLCanvas canvas;
	private FPSAnimator animator;

	final private int numTestCase;
	private int testCase;
	// Frame buffer
	public BufferedImage buff;
	@SuppressWarnings("unused")
	private ColorType color;
	private Random rng;
	
	 // specular exponent for materials
	private int ns=4; 
	
	private ArrayList<Point2D> lineSegs;
	private ArrayList<Point2D> triangles;
	private boolean doSmoothShading;
	private int Nsteps;

	/** The quaternion which controls the rotation of the world. */
    private Quaternion viewing_quaternion = new Quaternion();
    private Vector3D viewing_center = new Vector3D((float)(DEFAULT_WINDOW_WIDTH/2),(float)(DEFAULT_WINDOW_HEIGHT/2),(float)0.0);
    /** The last x and y coordinates of the mouse press. */
    private int last_x = 0, last_y = 0;
    /** Whether the world is being rotated. */
    private boolean rotate_world = false;
    
    // For the ambient, diffuse and specular terms
    public boolean [] matTerms = new boolean[3];
    
    // For the depth buffer
    public int[][] depth_buffer;
    public int max_x;
    public int max_y;
    public boolean is_phong;
    
    public int num_lights;
    
    public Light[] light_sources;
    
    public int scene_num;
    
    public boolean toggle_lights;
    
    
    public boolean rotate_torus;
    public boolean rotate_ellipsoid;
    public boolean rotate_sphere;
    
    public boolean scale_torus;
    public boolean scale_ellipsoid;
    public boolean scale_sphere;
    
    public boolean translate_torus;
    public boolean translate_ellipsoid;
    public boolean translate_sphere;
    
	public float radius;
	public float sphere_radius;
	public float ellipsoid_radius;
	public float torus_radius;
    
	public PA4()
	{
	    capabilities = new GLCapabilities(null);
	    capabilities.setDoubleBuffered(true);  // Enable Double buffering

	    canvas  = new GLCanvas(capabilities);
	    canvas.addGLEventListener(this);
	    canvas.addMouseListener(this);
	    canvas.addMouseMotionListener(this);
	    canvas.addKeyListener(this);
	    canvas.setAutoSwapBufferMode(true); // true by default. Just to be explicit
	    canvas.setFocusable(true);
	    getContentPane().add(canvas);

	    animator = new FPSAnimator(canvas, 60); // drive the display loop @ 60 FPS

	    numTestCase = 2;
	    testCase = 0;
	    Nsteps = 12;

	    setTitle("CS480/680 Lab 11");
	    setSize( DEFAULT_WINDOW_WIDTH, DEFAULT_WINDOW_HEIGHT);
	    setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	    setVisible(true);
	    setResizable(false);
	    
	    rng = new Random();
	    color = new ColorType(1.0f,0.0f,0.0f);
	    lineSegs = new ArrayList<Point2D>();
	    triangles = new ArrayList<Point2D>();
	    doSmoothShading = false;
	}

	public void run()
	{
		animator.start();
	}

	public static void main( String[] args )
	{
	    PA4 P = new PA4();
	    P.run();
	}

	//*********************************************** 
	//  GLEventListener Interfaces
	//*********************************************** 
	public void init( GLAutoDrawable drawable) 
	{ 
	    GL gl = drawable.getGL();
	    gl.glClearColor( 0.0f, 0.0f, 0.0f, 0.0f);
	    gl.glLineWidth( DEFAULT_LINE_WIDTH );
	    Dimension sz = this.getContentPane().getSize();
	    // Frame buffer
	    buff = new BufferedImage(sz.width,sz.height,BufferedImage.TYPE_3BYTE_BGR);
	    max_x = sz.width;
	    max_y = sz.height;
	    is_phong = false;
	    clearPixelBuffer();
	    scene_num = 1;
	    toggle_lights = true;
		radius = (float)30.0;
		sphere_radius = (float)30.0;
		ellipsoid_radius = (float)30.0;
		torus_radius = (float)30.0;
	}

	// Redisplaying graphics
	public void display(GLAutoDrawable drawable)
	{
	    GL2 gl = drawable.getGL().getGL2();
	    WritableRaster wr = buff.getRaster();
	    DataBufferByte dbb = (DataBufferByte) wr.getDataBuffer();
	    byte[] data = dbb.getData();

	    gl.glPixelStorei(GL2.GL_UNPACK_ALIGNMENT, 1);
	    gl.glDrawPixels (buff.getWidth(), buff.getHeight(),
                GL2.GL_BGR, GL2.GL_UNSIGNED_BYTE,
                ByteBuffer.wrap(data));
        drawTestCase();
	}

	// Window size change
	public void reshape(GLAutoDrawable drawable, int x, int y, int w, int h)
	{
		// deliberately left blank
	}
	public void displayChanged(GLAutoDrawable drawable, boolean modeChanged,
	      boolean deviceChanged)
	{
		// deliberately left blank
	}
	
	void clearPixelBuffer()
	{
		lineSegs.clear();
    	triangles.clear();
		Graphics2D g = buff.createGraphics();
	    g.setColor(Color.BLACK);
	    g.fillRect(0, 0, buff.getWidth(), buff.getHeight());
	    g.dispose();
	}
	
	// drawTest
	void drawTestCase()
	{  
		/* clear the window and vertex state */
		clearPixelBuffer();
	  
		//System.out.printf("Test case = %d\n",testCase);
		switch(scene_num)
		{
		case 1:
			switch (testCase)
			{
			case 0:
				shadeTest1(true); /* smooth shaded, sphere and torus */
				break;
			case 1:
				shadeTest1(false); /* flat shaded, sphere and torus */
				break;
			}
			break;
		case 2:
			switch (testCase)
			{
			case 0:
				shadeTest2(true); /* smooth shaded, sphere and torus */
				break;
			case 1:
				shadeTest2(false); /* flat shaded, sphere and torus */
				break;
			}
			break;
		case 3:
			switch (testCase)
			{
			case 0:
				shadeTest3(true); /* smooth shaded, sphere and torus */
				break;
			case 1:
				shadeTest3(false); /* flat shaded, sphere and torus */
				break;
			}
			break;
		}
	}


	//*********************************************** 
	//          KeyListener Interfaces
	//*********************************************** 
	public void keyTyped(KeyEvent key)
	{
	//      Q,q: quit 
	//      C,c: clear polygon (set vertex count=0)
	//		R,r: randomly change the color
	//		A,a: Ambient term
	//		D,d: Diffuse term
	//		S,s: Specular term
	//		X,x: toggle the smooth shading
	//		T,t: show testing examples (toggles between smooth shading and flat shading test cases)
	//		>:	 increase the step number for examples
	//		<:   decrease the step number for examples
	//     	+,-: increase or decrease spectral exponent

	    switch ( key.getKeyChar() ) 
	    {
	    case 'Q' :
	    case 'q' : 
	    	new Thread()
	    	{
	          	public void run() { animator.stop(); }
	        }.start();
	        System.exit(0);
	        break;
	    case 'R' :
	    case 'r' :
	    	color = new ColorType(rng.nextFloat(),rng.nextFloat(),
	    			rng.nextFloat());
	    	break;
	    case 'C' :
	    case 'c' :
	    	clearPixelBuffer();
	    	break;
	    // Ambient term
	    case 'A' :
	    case 'a' :
	    	matTerms[2] = !matTerms[2];
	    	break;
	    // Diffuse term
	    case 'd' :
	    case 'D' :
	    	matTerms[1] = !matTerms[1];
	    	break;
	    // Specular term
	    case 'S' :
	    case 's' :
	    	matTerms[0] = !matTerms[0];
	    	break;
	    case 'X' :
	    case 'x' :
	    	doSmoothShading = !doSmoothShading; // This is a placeholder (implemented in 'T')
	    	break;
	    // Smooth Shading	
	    case 'T' :
	    case 't' : 
	    	testCase = (testCase+1)%numTestCase;
	    	drawTestCase();
	        break;
	        //Phong shading
	    case 'P' :
	    case 'p' : 
	    	is_phong = !is_phong;
	    	drawTestCase();
	        break;
	        // Flat shading
	    case 'F' :
	    case 'f' : 
	    	testCase = 1;
	    	drawTestCase();
	        break;
	        // Gouraud shading
	    case 'G' :
	    case 'g' :
	    	testCase = 0;
	    	drawTestCase();
	        break;
	        // Scene 1
	    case '8' :
	    	testCase = 0;
	    	scene_num = 1;
	    	drawTestCase();
	        break;
	        // Scene 2
	    case '9' :
	    	testCase = 0;
	    	scene_num = 2;
	    	drawTestCase();
	        break;
	        // Scene 3
	    case '0' :
	    	testCase = 0;
	    	scene_num = 3;
	    	drawTestCase();
	        break;
	    case '1' :
	    	toggle_lights = !toggle_lights;
	    	drawTestCase();
	        break;
	    case 'b' :
	    	scale_sphere = !scale_sphere;
	    	drawTestCase();
	        break;
	    case 'n' :
	    	scale_torus = !scale_torus;
	    	drawTestCase();
	        break;
	    case 'm' :
	    	scale_ellipsoid = !scale_ellipsoid;
	    	drawTestCase();
	        break;
	    case 'B' :
	    	translate_sphere = !translate_sphere;
	    	drawTestCase();
	        break;
	    case 'N' :
	    	translate_torus = !translate_torus;
	    	drawTestCase();
	        break;
	    case 'M' :
	    	translate_ellipsoid = !translate_ellipsoid;
	    	drawTestCase();
	        break;
	    case '<':  
	        Nsteps = Nsteps < 4 ? Nsteps: Nsteps / 2;
	        System.out.printf( "Nsteps = %d \n", Nsteps);
	        drawTestCase();
	        break;
	    case '>':
	        Nsteps = Nsteps > 300 ? Nsteps: Nsteps * 2;
	        System.out.printf( "Nsteps = %d \n", Nsteps);
	        drawTestCase();
	        break;
	    case '+':
	    	ns++;
	        drawTestCase();
	    	break;
	    case '-':
	    	if(ns>0)
	    		ns--;
	        drawTestCase();
	    	break;
	    default :
	        break;
	    }
	}

	public void keyPressed(KeyEvent key)
	{
	    switch (key.getKeyCode()) 
	    {
	    case KeyEvent.VK_ESCAPE:
	    	new Thread()
	        {
	    		public void run()
	    		{
	    			animator.stop();
	    		}
	        }.start();
	        System.exit(0);
	        break;
	      default:
	        break;
	    }
	}

	public void keyReleased(KeyEvent key)
	{
		// deliberately left blank
	}

	//************************************************** 
	// MouseListener and MouseMotionListener Interfaces
	//************************************************** 
	public void mouseClicked(MouseEvent mouse)
	{
		// deliberately left blank
	}
	  public void mousePressed(MouseEvent mouse)
	  {
	    int button = mouse.getButton();
	    if ( button == MouseEvent.BUTTON1 )
	    {
	      last_x = mouse.getX();
	      last_y = mouse.getY();
	      rotate_world = true;
	    }
	  }

	  public void mouseReleased(MouseEvent mouse)
	  {
	    int button = mouse.getButton();
	    if ( button == MouseEvent.BUTTON1 )
	    {
	      rotate_world = false;
	    }
	  }

	public void mouseMoved( MouseEvent mouse)
	{
		// Deliberately left blank
	}

	/**
	   * Updates the rotation quaternion as the mouse is dragged.
	   * 
	   * @param mouse
	   *          The mouse drag event object.
	   */
	  public void mouseDragged(final MouseEvent mouse) {
	    if (this.rotate_world) {
	      // get the current position of the mouse
	      final int x = mouse.getX();
	      final int y = mouse.getY();

	      // get the change in position from the previous one
	      final int dx = x - this.last_x;
	      final int dy = y - this.last_y;

	      // create a unit vector in the direction of the vector (dy, dx, 0)
	      final float magnitude = (float)Math.sqrt(dx * dx + dy * dy);
	      if(magnitude > 0.0001)
	      {
	    	  // define axis perpendicular to (dx,-dy,0)
	    	  // use -y because origin is in upper lefthand corner of the window
	    	  final float[] axis = new float[] { -(float) (dy / magnitude),
	    			  (float) (dx / magnitude), 0 };

	    	  // calculate appropriate quaternion
	    	  final float viewing_delta = 3.1415927f / 180.0f;
	    	  final float s = (float) Math.sin(0.5f * viewing_delta);
	    	  final float c = (float) Math.cos(0.5f * viewing_delta);
	    	  final Quaternion Q = new Quaternion(c, s * axis[0], s * axis[1], s
	    			  * axis[2]);
	    	  this.viewing_quaternion = Q.multiply(this.viewing_quaternion);

	    	  // normalize to counteract acccumulating round-off error
	    	  this.viewing_quaternion.normalize();

	    	  // save x, y as last x, y
	    	  this.last_x = x;
	    	  this.last_y = y;
	          drawTestCase();
	      }
	    }

	  }
	  
	public void mouseEntered( MouseEvent mouse)
	{
		// Deliberately left blank
	}

	public void mouseExited( MouseEvent mouse)
	{
		// Deliberately left blank
	} 


	public void dispose(GLAutoDrawable drawable) {
		// TODO Auto-generated method stub
		
	}
	
	//************************************************** 
	// Test Cases
	// Dec 2016 -- removed line and triangle test cases
	//************************************************** 

	// Infinite White light  & Red Point light source
	void shadeTest1(boolean doSmooth)
	{
	    num_lights = 2;
		// the simple example scene includes one sphere and one torus
		
        Sphere3D sphere = new Sphere3D((float)128.0, (float)128.0, (float)128.0, (float)1.5*sphere_radius, Nsteps, Nsteps);
        Torus3D torus = new Torus3D((float)256.0, (float)384.0, (float)128.0, (float)0.8*torus_radius, (float)1.25*torus_radius, Nsteps, Nsteps);
        Ellipsoid ellipsoid = new Ellipsoid((float)300.0, (float)50.0, (float)128.0, (float)ellipsoid_radius, (float)0.5*ellipsoid_radius, (float)2.5*ellipsoid_radius, Nsteps, Nsteps);
        
        if(scale_torus)
        {
        	torus_radius = 50.0f;
        }
        else
        {
        	torus_radius = 30.0f;
        }
        if(scale_ellipsoid)
        {
        	ellipsoid_radius = 18.0f;
        }
        else
        {
        	ellipsoid_radius = 30.0f;
        }
        if(scale_sphere)
        {
        	sphere_radius = 8.0f;
        }
        else
        {
        	sphere_radius = 30.0f;
        }
        
       if(translate_torus)
       {
    	   torus.set_center(101, 107, 220);
       }
        if(translate_ellipsoid)
        {
        	ellipsoid.set_center(150, 300, 104);
        }
        if(translate_sphere)
        {
        	sphere.set_center(400, 380, 50);
        }
        
        
        // view vector is defined along z axis
        // this example assumes simple othorgraphic projection
        // view vector is used in 
        //   (a) calculating specular lighting contribution
        //   (b) backface culling / backface rejection
        Vector3D view_vector = new Vector3D((float)0.0,(float)0.0,(float)1.0);
      
        // material properties for the sphere and torus
        // ambient, diffuse, and specular coefficients
        // specular exponent is a global variable
        ColorType torus_ka = new ColorType(0.05f,0.1f,0.1f);
        ColorType sphere_ka = new ColorType(0.1f,0.1f,0.1f);
        ColorType ellipsoid_ka = new ColorType(0.1f,0.1f,0.1f);
        ColorType torus_kd = new ColorType(0.0f,0.5f,0.9f);
        ColorType sphere_kd = new ColorType(0.9f,0.3f,0.1f);
        ColorType ellipsoid_kd = new ColorType(0.55f,1.0f,0.0f);
        ColorType torus_ks = new ColorType(1.0f,1.0f,1.0f);
        ColorType sphere_ks = new ColorType(1.0f,1.0f,1.0f);        
        ColorType ellipsoid_ks = new ColorType(1.0f,1.0f,1.0f);
        
        
        Material[] mats = {new Material(sphere_ka, sphere_kd, sphere_ks, matTerms, ns), new Material(torus_ka, torus_kd, torus_ks, matTerms, ns), new Material(ellipsoid_ka, ellipsoid_kd, ellipsoid_ks, matTerms, ns)};
        
        ColorType light_color;
        // define one infinite light source, with color = white
        if(toggle_lights)
        {
        	light_color = new ColorType(1.0f,1.0f,1.0f);
        }
        else
        {
        	light_color = new ColorType(0.0f,0.0f,0.0f);
        }
        
        Vector3D light_direction = new Vector3D((float)0.0,(float)(-1.0/Math.sqrt(2.0)),(float)(1.0/Math.sqrt(2.0)));
        InfiniteLight light = new InfiniteLight(light_color,light_direction);// Lights
        light_sources = new Light[num_lights];
        light_sources[0] = light;
        
        
        // normal to the plane of a triangle
        // to be used in backface culling / backface rejection
        Vector3D triangle_normal = new Vector3D();
        
        // a triangle mesh
        Mesh3D mesh;
            
		int i, j, n, m;
		
		// temporary variables for triangle 3D vertices and 3D normals
		Vector3D v0,v1, v2, n0, n1, n2;
		
		// projected triangle, with vertex colors
		Point2D[] tri = {new Point2D(), new Point2D(), new Point2D()};
		
		
		// init Depth buffer
		depth_buffer = new int[max_x][max_y];
		for (int x_buff = 0; x_buff < max_x; x_buff++)
		{
			for (int y_buff = 0; y_buff < max_y; y_buff++)
			{
				depth_buffer[x_buff][y_buff] = 0;
			}
		}
		
		for(int k=0;k<3;++k) // loop twice: shade sphere, then torus
		{
			if(k==0)
			{
				mesh=sphere.mesh;
				n=sphere.get_n();
				m=sphere.get_m();
			}
			else if (k == 1)
			{
				mesh=torus.mesh;
				n=torus.get_n();
				m=torus.get_m();
			}
			else
			{
				mesh = ellipsoid.mesh;
				n = ellipsoid.get_n();
				m = ellipsoid.get_m();
			}
			
			// rotate the surface's 3D mesh using quaternion
			mesh.rotateMesh(viewing_quaternion, viewing_center);
					
			// draw triangles for the current surface, using vertex colors
			// this works for Gouraud and flat shading only (not Phong)
			for(i=0; i < m-1; ++i)
		    {
				for(j=0; j < n-1; ++j)
				{
					v0 = mesh.v[i][j];
					v1 = mesh.v[i][j+1];
					v2 = mesh.v[i+1][j+1];
					triangle_normal = computeTriangleNormal(v0,v1,v2);
					
					// Back Face Algorithm - if View dot Normal > 0
					if(view_vector.dotProduct(triangle_normal) > 0.0)  // front-facing triangle?
					{
						if(is_phong)
						{
							// vertex normals for Phong shading
							n0 = mesh.n[i][j];
							n1 = mesh.n[i][j+1];
							n2 = mesh.n[i+1][j+1];	
						}
						else if(doSmooth)  
						{
							// vertex colors for Gouraud shading
							n0 = mesh.n[i][j];
							n1 = mesh.n[i][j+1];
							n2 = mesh.n[i+1][j+1];
							tri[0].c = light.applyLight(mats[k], view_vector, n0);
							tri[1].c = light.applyLight(mats[k], view_vector, n1);
							tri[2].c = light.applyLight(mats[k], view_vector, n2);
						}
						else 
						{
							// flat shading: use the normal to the triangle itself
							n2 = n1 = n0 =  triangle_normal;
							tri[2].c = tri[1].c = tri[0].c = light.applyLight(mats[k], view_vector, triangle_normal);
						}
						
						tri[0].x = (int)v0.x;
						tri[0].y = (int)v0.y;
						tri[0].z = (int)v0.z;
						tri[1].x = (int)v1.x;
						tri[1].y = (int)v1.y;
						tri[1].z = (int)v1.z;
						tri[2].x = (int)v2.x;
						tri[2].y = (int)v2.y;
						tri[2].z = (int)v2.z;
						
						// Draw Triangle
						// Second check for phong
						if(is_phong)
						{
							for (int lf = 0; lf < num_lights; lf++)
							{
								SketchBase.drawTriangle(buff,tri[0],tri[1],tri[2],doSmooth,depth_buffer,is_phong,n0, n1, n2, light_sources[lf], mats[k], view_vector); 
							}
						}
						else
						{
							SketchBase.drawTriangle(buff,tri[0],tri[1],tri[2],doSmooth,depth_buffer);  
						}
					}
					
					v0 = mesh.v[i][j];
					v1 = mesh.v[i+1][j+1];
					v2 = mesh.v[i+1][j];
					triangle_normal = computeTriangleNormal(v0,v1,v2);
					
					// Back Face Algorithm - if View dot Normal > 0
					if(view_vector.dotProduct(triangle_normal) > 0.0)  // front-facing triangle?
					{
						if(is_phong)
						{
							// vertex normals for Phong shading
							n0 = mesh.n[i][j];
							n1 = mesh.n[i+1][j+1];
							n2 = mesh.n[i+1][j];	
						}
							
						else if(doSmooth)
						{
							// vertex colors for Gouraud shading
							n0 = mesh.n[i][j];
							n1 = mesh.n[i+1][j+1];
							n2 = mesh.n[i+1][j];
							tri[0].c = light.applyLight(mats[k], view_vector, n0);
							tri[1].c = light.applyLight(mats[k], view_vector, n1);
							tri[2].c = light.applyLight(mats[k], view_vector, n2);
						}
						else 
						{
							// flat shading: use the normal to the triangle itself
							n2 = n1 = n0 =  triangle_normal;
							tri[2].c = tri[1].c = tri[0].c = light.applyLight(mats[k], view_vector, triangle_normal);
						}	
						
						tri[0].x = (int)v0.x;
						tri[0].y = (int)v0.y;
						tri[0].z = (int)v0.z;
						tri[1].x = (int)v1.x;
						tri[1].y = (int)v1.y;
						tri[1].z = (int)v1.z;
						tri[2].x = (int)v2.x;
						tri[2].y = (int)v2.y;
						tri[2].z = (int)v2.z;
						
						if(is_phong)
						{
							for (int lf = 0; lf < num_lights; lf++)
							{
								SketchBase.drawTriangle(buff,tri[0],tri[1],tri[2],doSmooth,depth_buffer,is_phong,n0, n1, n2, light_sources[lf], mats[k], view_vector); 
							}
						}
						else
						{
							SketchBase.drawTriangle(buff,tri[0],tri[1],tri[2],doSmooth,depth_buffer);
						}
					}
				}	
		    }
		}
	}

	// Blue Point Light Source & Yellow spotlight
	void shadeTest2(boolean doSmooth)
	{
	    num_lights = 1;
		// the simple example scene includes one sphere and one torus
		float radius = (float)30.0;
        Sphere3D sphere = new Sphere3D((float)170.0, (float)288.0, (float)60.0, (float)1.5*radius, Nsteps, Nsteps);
        Ellipsoid ellipsoid = new Ellipsoid((float)410.0, (float)400.0, (float)90.0, (float)radius, (float)0.5*radius, (float)2.5*radius, Nsteps, Nsteps);
        Superellipsoid box = new Superellipsoid((float)100.0, (float)150.0, (float)128.0, (float)1.4*radius, (float)1.4*radius, (float)1.4*radius, Nsteps, Nsteps);
        
        
        // view vector is defined along z axis
        // this example assumes simple othorgraphic projection
        // view vector is used in 
        //   (a) calculating specular lighting contribution
        //   (b) backface culling / backface rejection
        Vector3D view_vector = new Vector3D((float)0.0,(float)0.0,(float)1.0);
      
        // material properties for the sphere and torus
        // ambient, diffuse, and specular coefficients
        // specular exponent is a global variable
        ColorType torus_ka = new ColorType(0.05f,0.1f,0.1f);
        ColorType sphere_ka = new ColorType(0.1f,0.1f,0.1f);
        ColorType ellipsoid_ka = new ColorType(0.1f,0.1f,0.1f);
        ColorType torus_kd = new ColorType(0.0f,0.5f,0.9f);
        ColorType sphere_kd = new ColorType(0.9f,0.1f,0.1f);
        ColorType ellipsoid_kd = new ColorType(0.78f,1.0f,0.2f);
        ColorType torus_ks = new ColorType(1.0f,1.0f,1.0f);
        ColorType sphere_ks = new ColorType(1.0f,1.0f,1.0f);        
        ColorType ellipsoid_ks = new ColorType(1.0f,1.0f,1.0f);
        
        
        ColorType box_ka = new ColorType(0.1f,0.1f,0.1f);
        ColorType box_kd = new ColorType(0.7f,0.7f,0.0f);
        ColorType box_ks = new ColorType(1.0f,1.0f,1.0f);
        
        ColorType cube_ka = new ColorType(0.1f,0.1f,0.1f);
        ColorType cube_kd = new ColorType(0.7f,0.1f,0.7f);
        ColorType cube_ks = new ColorType(1.0f,1.0f,1.0f);
        
        Material[] mats = {new Material(sphere_ka, sphere_kd, sphere_ks, matTerms, ns), new Material(ellipsoid_ka, ellipsoid_kd, ellipsoid_ks, matTerms, ns),
        					new Material(box_ka, box_kd, box_ks, matTerms, ns)};
        
        
        ColorType light_color;
        // define one infinite light source, with color = white
        if(toggle_lights)
        {
        	light_color = new ColorType(1.0f,1.0f,1.0f);
        }
        else
        {
        	light_color = new ColorType(0.0f,0.0f,0.0f);
        }
        
        Vector3D light_direction = new Vector3D((float)0.0,(float)(-1.0/Math.sqrt(2.0)),(float)(1.0/Math.sqrt(2.0)));
        InfiniteLight light = new InfiniteLight(light_color,light_direction);
        
        // Lights
        light_sources = new Light[num_lights];
        light_sources[0] = light;
        
        
        // normal to the plane of a triangle
        // to be used in backface culling / backface rejection
        Vector3D triangle_normal = new Vector3D();
        
        // a triangle mesh
        Mesh3D mesh;
            
		int i, j, n, m;
		
		// temporary variables for triangle 3D vertices and 3D normals
		Vector3D v0,v1, v2, n0, n1, n2;
		
		// projected triangle, with vertex colors
		Point2D[] tri = {new Point2D(), new Point2D(), new Point2D()};
		
		
		// init Depth buffer
		depth_buffer = new int[max_x][max_y];
		for (int x_buff = 0; x_buff < max_x; x_buff++)
		{
			for (int y_buff = 0; y_buff < max_y; y_buff++)
			{
				depth_buffer[x_buff][y_buff] = 0;
			}
		}
		
		for(int k=0;k<3;++k) // loop twice: shade sphere, then torus
		{
			if(k==0)
			{
				mesh=sphere.mesh;
				n=sphere.get_n();
				m=sphere.get_m();
			}
			else if (k == 1)
			{
				mesh = ellipsoid.mesh;
				n = ellipsoid.get_n();
				m = ellipsoid.get_m();
			}
			else
			{
				mesh = box.mesh;
				n = box.get_n();
				m = box.get_m();
			}
			
			// rotate the surface's 3D mesh using quaternion
			mesh.rotateMesh(viewing_quaternion, viewing_center);
					
			// draw triangles for the current surface, using vertex colors
			// this works for Gouraud and flat shading only (not Phong)
			for(i=0; i < m-1; ++i)
		    {
				for(j=0; j < n-1; ++j)
				{
					v0 = mesh.v[i][j];
					v1 = mesh.v[i][j+1];
					v2 = mesh.v[i+1][j+1];
					triangle_normal = computeTriangleNormal(v0,v1,v2);
					
					// Back Face Algorithm - if View dot Normal > 0
					if(view_vector.dotProduct(triangle_normal) > 0.0)  // front-facing triangle?
					{
						if(is_phong)
						{
							// vertex normals for Phong shading
							n0 = mesh.n[i][j];
							n1 = mesh.n[i][j+1];
							n2 = mesh.n[i+1][j+1];	
						}
						else if(doSmooth)  
						{
							// vertex colors for Gouraud shading
							n0 = mesh.n[i][j];
							n1 = mesh.n[i][j+1];
							n2 = mesh.n[i+1][j+1];
							tri[0].c = light.applyLight(mats[k], view_vector, n0);
							tri[1].c = light.applyLight(mats[k], view_vector, n1);
							tri[2].c = light.applyLight(mats[k], view_vector, n2);
						}
						else 
						{
							// flat shading: use the normal to the triangle itself
							n2 = n1 = n0 =  triangle_normal;
							tri[2].c = tri[1].c = tri[0].c = light.applyLight(mats[k], view_vector, triangle_normal);
						}
						
						tri[0].x = (int)v0.x;
						tri[0].y = (int)v0.y;
						tri[0].z = (int)v0.z;
						tri[1].x = (int)v1.x;
						tri[1].y = (int)v1.y;
						tri[1].z = (int)v1.z;
						tri[2].x = (int)v2.x;
						tri[2].y = (int)v2.y;
						tri[2].z = (int)v2.z;
						
						// Draw Triangle
						// Second check for phong
						if(is_phong)
						{
							for (int lf = 0; lf < num_lights; lf++)
							{
								SketchBase.drawTriangle(buff,tri[0],tri[1],tri[2],doSmooth,depth_buffer,is_phong,n0, n1, n2, light_sources[lf], mats[k], view_vector); 
							}
						}
						else
						{
							SketchBase.drawTriangle(buff,tri[0],tri[1],tri[2],doSmooth,depth_buffer);  
						}
					}
					
					v0 = mesh.v[i][j];
					v1 = mesh.v[i+1][j+1];
					v2 = mesh.v[i+1][j];
					triangle_normal = computeTriangleNormal(v0,v1,v2);
					
					// Back Face Algorithm - if View dot Normal > 0
					if(view_vector.dotProduct(triangle_normal) > 0.0)  // front-facing triangle?
					{
						if(is_phong)
						{
							// vertex normals for Phong shading
							n0 = mesh.n[i][j];
							n1 = mesh.n[i+1][j+1];
							n2 = mesh.n[i+1][j];	
						}
							
						else if(doSmooth)
						{
							// vertex colors for Gouraud shading
							n0 = mesh.n[i][j];
							n1 = mesh.n[i+1][j+1];
							n2 = mesh.n[i+1][j];
							tri[0].c = light.applyLight(mats[k], view_vector, n0);
							tri[1].c = light.applyLight(mats[k], view_vector, n1);
							tri[2].c = light.applyLight(mats[k], view_vector, n2);
						}
						else 
						{
							// flat shading: use the normal to the triangle itself
							n2 = n1 = n0 =  triangle_normal;
							tri[2].c = tri[1].c = tri[0].c = light.applyLight(mats[k], view_vector, triangle_normal);
						}	
						
						tri[0].x = (int)v0.x;
						tri[0].y = (int)v0.y;
						tri[0].z = (int)v0.z;
						tri[1].x = (int)v1.x;
						tri[1].y = (int)v1.y;
						tri[1].z = (int)v1.z;
						tri[2].x = (int)v2.x;
						tri[2].y = (int)v2.y;
						tri[2].z = (int)v2.z;
						
						if(is_phong)
						{
							for (int lf = 0; lf < num_lights; lf++)
							{
								SketchBase.drawTriangle(buff,tri[0],tri[1],tri[2],doSmooth,depth_buffer,is_phong,n0, n1, n2, light_sources[lf], mats[k], view_vector); 
							}
						}
						else
						{
							SketchBase.drawTriangle(buff,tri[0],tri[1],tri[2],doSmooth,depth_buffer);
						}
					}
				}	
		    }
		}
	}
	
	// Green SpotLight Blue Infinite light
	void shadeTest3(boolean doSmooth)
	{
	    num_lights = 1;
		// the simple example scene includes one sphere and one torus
		float radius = (float)30.0;
        Torus3D torus = new Torus3D((float)256.0, (float)384.0, (float)128.0, (float)0.8*radius, (float)1.25*radius, Nsteps, Nsteps);
        Superellipsoid box = new Superellipsoid((float)100.0, (float)50.0, (float)128.0, (float)radius, (float)radius, (float)radius, Nsteps, Nsteps);
        Box cube = new Box((float)200.0, (float)220.0, (float)150.0, (float)radius, (float)radius, (float)radius, Nsteps, Nsteps);
        
        // view vector is defined along z axis
        // this example assumes simple othorgraphic projection
        // view vector is used in 
        //   (a) calculating specular lighting contribution
        //   (b) backface culling / backface rejection
        Vector3D view_vector = new Vector3D((float)0.0,(float)0.0,(float)1.0);
      
        // material properties for the sphere and torus
        // ambient, diffuse, and specular coefficients
        // specular exponent is a global variable
        ColorType torus_ka = new ColorType(0.05f,0.1f,0.1f);
        ColorType sphere_ka = new ColorType(0.1f,0.1f,0.1f);
        ColorType ellipsoid_ka = new ColorType(0.1f,0.1f,0.1f);
        ColorType torus_kd = new ColorType(0.0f,0.8f,0.8f);
        ColorType sphere_kd = new ColorType(0.9f,0.3f,0.1f);
        ColorType ellipsoid_kd = new ColorType(0.55f,1.0f,0.0f);
        ColorType torus_ks = new ColorType(1.0f,1.0f,1.0f);
        ColorType sphere_ks = new ColorType(1.0f,1.0f,1.0f);        
        ColorType ellipsoid_ks = new ColorType(1.0f,1.0f,1.0f);
        
        ColorType box_ka = new ColorType(0.1f,0.1f,0.1f);
        ColorType box_kd = new ColorType(0.7f,0.7f,0.0f);
        ColorType box_ks = new ColorType(1.0f,1.0f,1.0f);
        
        ColorType cube_ka = new ColorType(0.1f,0.1f,0.1f);
        ColorType cube_kd = new ColorType(0.7f,0.1f,0.7f);
        ColorType cube_ks = new ColorType(1.0f,1.0f,1.0f);
        
        Material[] mats = {new Material(torus_ka, torus_kd, torus_ks, matTerms, ns), new Material(box_ka, box_kd, box_ks, matTerms, ns), new Material(cube_ka, cube_kd, cube_ks, matTerms, ns) };

        
        ColorType light_color;
        // define one infinite light source, with color = white
        if(toggle_lights)
        {
        	light_color = new ColorType(1.0f,1.0f,1.0f);
        }
        else
        {
        	light_color = new ColorType(0.0f,0.0f,0.0f);
        }
        Vector3D light_direction = new Vector3D((float)0.0,(float)(-1.0/Math.sqrt(2.0)),(float)(1.0/Math.sqrt(2.0)));
        InfiniteLight light = new InfiniteLight(light_color,light_direction);
        
        // Lights
        light_sources = new Light[num_lights];
        light_sources[0] = light;
        
        
        // normal to the plane of a triangle
        // to be used in backface culling / backface rejection
        Vector3D triangle_normal = new Vector3D();
        
        // a triangle mesh
        Mesh3D mesh;
            
		int i, j, n, m;
		
		// temporary variables for triangle 3D vertices and 3D normals
		Vector3D v0,v1, v2, n0, n1, n2;
		
		// projected triangle, with vertex colors
		Point2D[] tri = {new Point2D(), new Point2D(), new Point2D()};
		
		
		// init Depth buffer
		depth_buffer = new int[max_x][max_y];
		for (int x_buff = 0; x_buff < max_x; x_buff++)
		{
			for (int y_buff = 0; y_buff < max_y; y_buff++)
			{
				depth_buffer[x_buff][y_buff] = 0;
			}
		}
		
		for(int k=0;k<3;++k) // loop twice: shade sphere, then torus
		{
			if(k==0)
			{
				mesh=torus.mesh;
				n=torus.get_n();
				m=torus.get_m();
			}
			else if (k == 1)
			{
				mesh = box.mesh;
				n = box.get_n();
				m = box.get_m();
			}
			else
			{
				mesh = cube.mesh;
				n = cube.get_n();
				m = cube.get_m();
			}
			
			// rotate the surface's 3D mesh using quaternion
			mesh.rotateMesh(viewing_quaternion, viewing_center);
					
			// draw triangles for the current surface, using vertex colors
			// this works for Gouraud and flat shading only (not Phong)
			for(i=0; i < m-1; ++i)
		    {
				for(j=0; j < n-1; ++j)
				{
					v0 = mesh.v[i][j];
					v1 = mesh.v[i][j+1];
					v2 = mesh.v[i+1][j+1];
					triangle_normal = computeTriangleNormal(v0,v1,v2);
					
					// Back Face Algorithm - if View dot Normal > 0
					if(view_vector.dotProduct(triangle_normal) > 0.0)  // front-facing triangle?
					{
						if(is_phong)
						{
							// vertex normals for Phong shading
							n0 = mesh.n[i][j];
							n1 = mesh.n[i][j+1];
							n2 = mesh.n[i+1][j+1];	
						}
						else if(doSmooth)  
						{
							// vertex colors for Gouraud shading
							n0 = mesh.n[i][j];
							n1 = mesh.n[i][j+1];
							n2 = mesh.n[i+1][j+1];
							tri[0].c = light.applyLight(mats[k], view_vector, n0);
							tri[1].c = light.applyLight(mats[k], view_vector, n1);
							tri[2].c = light.applyLight(mats[k], view_vector, n2);
						}
						else 
						{
							// flat shading: use the normal to the triangle itself
							n2 = n1 = n0 =  triangle_normal;
							tri[2].c = tri[1].c = tri[0].c = light.applyLight(mats[k], view_vector, triangle_normal);
						}
						
						tri[0].x = (int)v0.x;
						tri[0].y = (int)v0.y;
						tri[0].z = (int)v0.z;
						tri[1].x = (int)v1.x;
						tri[1].y = (int)v1.y;
						tri[1].z = (int)v1.z;
						tri[2].x = (int)v2.x;
						tri[2].y = (int)v2.y;
						tri[2].z = (int)v2.z;
						
						// Draw Triangle
						// Second check for phong
						if(is_phong)
						{
							for (int lf = 0; lf < num_lights; lf++)
							{
								SketchBase.drawTriangle(buff,tri[0],tri[1],tri[2],doSmooth,depth_buffer,is_phong,n0, n1, n2, light_sources[lf], mats[k], view_vector); 
							}
						}
						else
						{
							SketchBase.drawTriangle(buff,tri[0],tri[1],tri[2],doSmooth,depth_buffer);  
						}
					}
					
					v0 = mesh.v[i][j];
					v1 = mesh.v[i+1][j+1];
					v2 = mesh.v[i+1][j];
					triangle_normal = computeTriangleNormal(v0,v1,v2);
					
					// Back Face Algorithm - if View dot Normal > 0
					if(view_vector.dotProduct(triangle_normal) > 0.0)  // front-facing triangle?
					{
						if(is_phong)
						{
							// vertex normals for Phong shading
							n0 = mesh.n[i][j];
							n1 = mesh.n[i+1][j+1];
							n2 = mesh.n[i+1][j];	
						}
							
						else if(doSmooth)
						{
							// vertex colors for Gouraud shading
							n0 = mesh.n[i][j];
							n1 = mesh.n[i+1][j+1];
							n2 = mesh.n[i+1][j];
							tri[0].c = light.applyLight(mats[k], view_vector, n0);
							tri[1].c = light.applyLight(mats[k], view_vector, n1);
							tri[2].c = light.applyLight(mats[k], view_vector, n2);
						}
						else 
						{
							// flat shading: use the normal to the triangle itself
							n2 = n1 = n0 =  triangle_normal;
							tri[2].c = tri[1].c = tri[0].c = light.applyLight(mats[k], view_vector, triangle_normal);
						}	
						
						tri[0].x = (int)v0.x;
						tri[0].y = (int)v0.y;
						tri[0].z = (int)v0.z;
						tri[1].x = (int)v1.x;
						tri[1].y = (int)v1.y;
						tri[1].z = (int)v1.z;
						tri[2].x = (int)v2.x;
						tri[2].y = (int)v2.y;
						tri[2].z = (int)v2.z;
						
						if(is_phong)
						{
							for (int lf = 0; lf < num_lights; lf++)
							{
								SketchBase.drawTriangle(buff,tri[0],tri[1],tri[2],doSmooth,depth_buffer,is_phong,n0, n1, n2, light_sources[lf], mats[k], view_vector); 
							}
						}
						else
						{
							SketchBase.drawTriangle(buff,tri[0],tri[1],tri[2],doSmooth,depth_buffer);
						}
					}
				}	
		    }
		}
	}
	
	
	
	
	// helper method that computes the unit normal to the plane of the triangle
	// degenerate triangles yield normal that is numerically zero
	private Vector3D computeTriangleNormal(Vector3D v0, Vector3D v1, Vector3D v2)
	{
		Vector3D e0 = v1.minus(v2);
		Vector3D e1 = v0.minus(v2);
		Vector3D norm = e0.crossProduct(e1);
		
		if(norm.magnitude()>0.000001)
			norm.normalize();
		else 	// detect degenerate triangle and set its normal to zero
			norm.set((float)0.0,(float)0.0,(float)0.0);

		return norm;
	}

}