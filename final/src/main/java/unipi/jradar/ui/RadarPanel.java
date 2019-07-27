package unipi.jradar.ui;

import java.awt.Color;
import java.awt.Font;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;

import javax.swing.event.MouseInputListener;

import com.jogamp.opengl.GL;
import com.jogamp.opengl.GL2;
import com.jogamp.opengl.GLAutoDrawable;
import com.jogamp.opengl.GLCapabilities;
import com.jogamp.opengl.GLEventListener;
import com.jogamp.opengl.GLProfile;
import com.jogamp.opengl.awt.GLCanvas;
import com.jogamp.opengl.glu.GLU;
import com.jogamp.opengl.glu.GLUquadric;
import com.jogamp.opengl.util.FPSAnimator;
import com.jogamp.opengl.util.awt.TextRenderer;

import unipi.jradar.core.RadarData;
import unipi.jradar.core.RadarDataContainer;
import unipi.jradar.core.RadarDataNotifier;
import unipi.jradar.core.SystemConstant;
import unipi.jradar.proxy.RadarDataListener;
import unipi.jradar.proxy.SystemProxy;

public class RadarPanel extends GLCanvas
		implements GLEventListener, MouseWheelListener, RadarDataListener, MouseInputListener {

	/**
	 * The step of zoom.
	 */
	private static final float ZOOM_STEP = 0.09f;

	/**
	 * Maximum zoom, 25x
	 */
	private static final int MAX_ZOOM = 25;

	/**
	 * Initial FPS.
	 */
	private static final int INITIAL_FPS = 50;

	/**
	 * Default serial UID.
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * Format of height.
	 */
	private static final String DATA_H_FORMAT = "Height: %.2f cm";

	/**
	 * Format of angle.
	 */
	private static final String DATA_THETA_FORMAT = "Angle: %.2f°";

	/**
	 * Format of data.
	 */
	private static final String DATA_DATA_FORMAT = "Memory: cur=%d,max=%d";

	/**
	 * Format of rotation view info.
	 */
	private static final String INFO_ROT_FORMAT = "Rotation: x=%.1f°,y=%.1f°";

	/**
	 * Format of rotation view info.
	 */
	private static final String CTRL_THETA_SENS_FORMAT = "Angle sens: %d°";

	/**
	 * Format of rotation view info.
	 */
	private static final String CTRL_H_SENS_FORMAT = "Height sens: %d";

	/**
	 * Format of rotation view info.
	 */
	private static final String INFO_FPS_FORMAT = "FPS: %d";
	private final RadarKeyListener keyListener;

	private FPSAnimator animator;
	private TextRenderer textRenderer;
	private GLU glu;
	private float currentRotationY;
	private float currentRotationX;
	private double currentAltitude;
	private double currentRealAltitude;
	private double currentAngle;
	private double currentValue;
	private int lastMouseY;
	private int lastMouseX;
	private float currentZoom;
	private int zoomCounter;
	private int memory;
	private int memoryLimit;
	private final static int fontSize = 32;

	/**
	 * RDC Interface
	 */
	private final RadarDataContainer rdn;
	/**
	 * System Proxy Interface
	 */
	private final SystemProxy sp;

	public RadarPanel() {
		super(new GLCapabilities(GLProfile.get(GLProfile.GL2)));

		addGLEventListener(this);

		keyListener = new RadarKeyListener();
		addKeyListener(keyListener);
		addMouseWheelListener(this);
		addMouseListener(this);
		addMouseMotionListener(this);

		// notify the radar data notifier that we are alive!
		RadarDataNotifier.getInstance().register(this);

		rdn = RadarDataContainer.getInstance();
		sp = SystemProxy.getInstance();

		animator = new FPSAnimator(this, INITIAL_FPS, true);
		animator.start();
		// this.getAnimator().start();
		currentRotationY = 0.f;
		currentRotationX = 0.f;
		currentZoom = 1.f;
	}

	private void computeColor(GL2 gl, double value) {
		/*
		 * in base of color, we need to set the color. Near : red Far : blue value :
		 * maxValue = x : 1
		 */
		final float c = (float) (value / SystemConstant.MAX_VALUE);

		// setting color
		gl.glColor3f(1.f - c, 0.5f + c/3, c * 2f);
	}

	@Override
	public void init(GLAutoDrawable drawable) {
		final GL2 gl = drawable.getGL().getGL2();
		gl.glShadeModel(GL2.GL_SMOOTH);
		gl.glClearColor(0f, 0f, 0f, 0f);
		gl.glClearDepth(1.0f);

		gl.glEnable(GL2.GL_BLEND);
		gl.glEnable(GL2.GL_LINE_SMOOTH);
		gl.glEnable(GL2.GL_POLYGON_SMOOTH);

		gl.glBlendFunc(GL.GL_SRC_ALPHA, GL.GL_ONE_MINUS_SRC_ALPHA);
//		gl.glDepthFunc(GL2.GL_LEQUAL);
//		gl.glHint(GL2.GL_PERSPECTIVE_CORRECTION_HINT, GL2.GL_NICEST);

		// initialize tools
		this.glu = new GLU();
		this.textRenderer = new TextRenderer(new Font("Courier", Font.PLAIN, fontSize), true, true);
	}

	@Override
	public void dispose(GLAutoDrawable drawable) {
	}

	public void drawDataBox(GLAutoDrawable drawable, GL2 gl) {
		textRenderer.beginRendering(drawable.getSurfaceWidth(), drawable.getSurfaceHeight());

		String data;
		final float scaleFactor = 1.0f;
		int y0;

		y0 = drawable.getSurfaceHeight() - fontSize;
		textRenderer.setColor(Color.CYAN);
		textRenderer.draw3D("View info", 10, y0, 0, scaleFactor);
		data = String.format(INFO_ROT_FORMAT, this.currentRotationX, this.currentRotationY);
		textRenderer.draw3D(data, 10, y0 - fontSize, 0, scaleFactor);
		data = String.format(INFO_FPS_FORMAT, this.animator.getFPS());
		textRenderer.draw3D(data, 10, y0 - fontSize * 2, 0, scaleFactor);

		// control data
		textRenderer.setColor(Color.ORANGE);
		textRenderer.draw3D("Control data", drawable.getSurfaceWidth() - 8 * fontSize, y0, 0, scaleFactor);
		data = String.format(RadarPanel.CTRL_THETA_SENS_FORMAT, this.sp.getAngleSens());
		textRenderer.draw3D(data, (int) (drawable.getSurfaceWidth() - data.length() * fontSize / 1.6), y0 - fontSize, 0,
				scaleFactor);
		data = String.format(RadarPanel.CTRL_H_SENS_FORMAT, this.sp.getHeightSens());
		textRenderer.draw3D(data, (int) (drawable.getSurfaceWidth() - data.length() * fontSize / 1.6),
				y0 - fontSize * 2, 0, scaleFactor);

		// radar data
		y0 = 4 * fontSize;
		textRenderer.setColor(Color.GREEN);
		textRenderer.draw3D("Radar data", 10, y0, 0, scaleFactor);
		data = String.format(DATA_H_FORMAT, this.currentRealAltitude);
		textRenderer.draw3D(data, 10, y0 - fontSize, 0, scaleFactor);
		data = String.format(DATA_THETA_FORMAT, this.currentAngle);
		textRenderer.draw3D(data, 10, y0 - fontSize * 2, 0, scaleFactor);

		/*
		 * getting info from system
		 */
		memoryLimit = rdn.getMemorySize();
		memory = rdn.getSize();

		data = String.format(DATA_DATA_FORMAT, memory, memoryLimit);
		textRenderer.draw3D(data, 10, y0 - fontSize * 3, 0, scaleFactor);

		textRenderer.endRendering();
		textRenderer.flush();
	}

	public void drawValueBox(GLAutoDrawable drawable, GL2 gl) {
		/*
		 * drawing the value box on right side
		 */
		final float x0 = 0.8f, y0 = -0.7f, l = 0.2f, h = 0.1f, s = 0.05f, lt = 0.02f;

		// main line
		gl.glBegin(GL2.GL_LINES);
		computeColor(gl, SystemConstant.MAX_VALUE);
		gl.glVertex2f(x0, y0);
		computeColor(gl, 0);
		gl.glVertex2f(x0, y0 - l);
		gl.glEnd();

		// first line
		computeColor(gl, SystemConstant.MAX_VALUE / 4);
		gl.glBegin(GL2.GL_LINES);
		gl.glVertex2f(x0 - s, y0 - l * 2 / 3);
		gl.glVertex2f(x0 + s, y0 - l * 2 / 3);
		gl.glEnd();

		// second line
		computeColor(gl, SystemConstant.MAX_VALUE / 2);
		gl.glBegin(GL2.GL_LINES);
		gl.glVertex2f(x0 - s, y0 - l * 1 / 3);
		gl.glVertex2f(x0 + s, y0 - l * 1 / 3);
		gl.glEnd();

		// top line
		computeColor(gl, SystemConstant.MAX_VALUE);
		gl.glBegin(GL2.GL_LINES);
		gl.glVertex2f(x0 - h, y0);
		gl.glVertex2f(x0 + h, y0);
		gl.glEnd();

		// bottom line
		computeColor(gl, 0);
		gl.glBegin(GL2.GL_LINES);
		gl.glVertex2f(x0 - h, y0 - l);
		gl.glVertex2f(x0 + h, y0 - l);
		gl.glEnd();

		// drawing the cursor
		final float tx0 = x0 + l / 2 + 0.008f;
		/*
		 * this is a proportion h = maxH : value : MaxValue
		 */
		final float hc = (float) (l * this.currentValue / SystemConstant.MAX_VALUE);
		final float ty0 = y0 - l + hc;

		computeColor(gl, this.currentValue);

		gl.glBegin(GL2.GL_TRIANGLES);
		gl.glVertex2f(tx0, ty0);
		gl.glVertex2f(tx0 + lt, ty0 + lt);
		gl.glVertex2f(tx0 + lt, ty0 - lt);
		gl.glEnd();

	}

	public void drawRadar(GLAutoDrawable drawable, GL2 gl) {
		// cylinder
		final float cylH = 0.50f;
		gl.glPushMatrix();
		gl.glColor4d(0, 1, 0, 0.5);
		GLUquadric glq = glu.gluNewQuadric();
		glu.gluCylinder(glq, 0.50, 0.48, cylH, 20, 20);
		gl.glTranslatef(0, 0, 0.5f);
		gl.glClearColor(0f, 0f, 0f, 0f);
		gl.glClearDepth(1f);

		// radar cursor
		gl.glBegin(GL.GL_LINE_STRIP);
		gl.glColor3f(0f, 0.6f, 0f);
		double altitude = (this.currentAltitude * cylH / SystemConstant.MAX_H) - cylH;
		gl.glVertex3d(0f, 0f, altitude);
		double rad = Math.toRadians(this.currentAngle);
		gl.glVertex3d(Math.cos(rad) * 0.5, Math.sin(rad) * 0.5, altitude);
		gl.glEnd();

		// elements
		RadarDataContainer rdc = RadarDataContainer.getInstance();
		for (RadarData data : rdc.getList()) {
			rad = Math.toRadians(data.angle);
			double xElem = 0.5 * data.value / SystemConstant.MAX_VALUE;
			double yElem = xElem * Math.sin(rad);
			xElem *= Math.cos(rad);
			altitude = (data.altitude * cylH / SystemConstant.MAX_H) - cylH;
			drawElement(gl, xElem, yElem, altitude, data.value);
		}

		gl.glPopMatrix();
	}

	private void drawElement(GL2 gl, double x, double y, double z, double currentValue) {
		final float radiusConst = (float) (currentValue / SystemConstant.MAX_VALUE);
		final float elemSize = 0.008f * radiusConst * 5.f;

		// setting the color
		this.computeColor(gl, currentValue);

		gl.glBegin(GL2.GL_QUADS); // Start Drawing The Cube

		// 1 face
		gl.glVertex3d(x - elemSize, y + elemSize, z + elemSize);
		gl.glVertex3d(x + elemSize, y + elemSize, z + elemSize);
		gl.glVertex3d(x + elemSize, y + elemSize, z - elemSize);
		gl.glVertex3d(x - elemSize, y + elemSize, z - elemSize);

		// 2 face
		gl.glVertex3d(x - elemSize, y + elemSize, z + elemSize);
		gl.glVertex3d(x + elemSize, y + elemSize, z + elemSize);
		gl.glVertex3d(x + elemSize, y - elemSize, z + elemSize);
		gl.glVertex3d(x - elemSize, y - elemSize, z + elemSize);

		// 3 face
		gl.glVertex3d(x + elemSize, y + elemSize, z + elemSize);
		gl.glVertex3d(x + elemSize, y + elemSize, z - elemSize);
		gl.glVertex3d(x + elemSize, y - elemSize, z + elemSize);
		gl.glVertex3d(x + elemSize, y + elemSize, z - elemSize);

		// 4 face
		gl.glVertex3d(x + elemSize, y - elemSize, z - elemSize);
		gl.glVertex3d(x - elemSize, y - elemSize, z - elemSize);
		gl.glVertex3d(x - elemSize, y + elemSize, z - elemSize);
		gl.glVertex3d(x + elemSize, y + elemSize, z - elemSize);

		// 5 face
		gl.glVertex3d(x - elemSize, y + elemSize, z + elemSize);
		gl.glVertex3d(x - elemSize, y + elemSize, z - elemSize);
		gl.glVertex3d(x - elemSize, y - elemSize, z - elemSize);
		gl.glVertex3d(x - elemSize, y - elemSize, z + elemSize);

		// 6 face
		gl.glVertex3d(x - elemSize, y - elemSize, z + elemSize);
		gl.glVertex3d(x + elemSize, y - elemSize, z + elemSize);
		gl.glVertex3d(x + elemSize, y - elemSize, z - elemSize);
		gl.glVertex3d(x - elemSize, y - elemSize, z - elemSize);
		gl.glEnd(); // Done Drawing The Quad
		gl.glFlush();
	}

	@Override
	public void display(GLAutoDrawable drawable) {
		final GL2 gl = drawable.getGL().getGL2();
		gl.glClear(GL2.GL_COLOR_BUFFER_BIT | GL2.GL_DEPTH_BUFFER_BIT);
		// loading identity
		gl.glLoadIdentity();

		// these must be always on screen, so with identity loaded
		drawDataBox(drawable, gl);
		drawValueBox(drawable, gl);

		// rotation!
		gl.glRotatef(currentRotationY, 1, 0, 0);
		gl.glRotatef(currentRotationX, 0, 0, 1);
		// zooming!
		gl.glScalef(currentZoom, currentZoom, currentZoom);

		// draw the real radar
		drawRadar(drawable, gl);

		// flushing
		gl.glFlush();
	}

	@Override
	public void reshape(GLAutoDrawable drawable, int x, int y, int width, int height) {
	}

	public void updateYRotation(float rotation) {
		this.currentRotationY += rotation;

		if (this.currentRotationY > 360 || this.currentRotationY < -360) {
			this.currentRotationY = 0;
		}
	}

	public void updateXRotation(float rotation) {
		this.currentRotationX += rotation;

		if (this.currentRotationX > 360 || this.currentRotationX < -360) {
			this.currentRotationX = 0;
		}
	}

	@Override
	public void newDataAvailable(RadarData radarData) {
		this.currentRealAltitude = radarData.getRealAltitude();
		this.currentAltitude = radarData.altitude;
		this.currentAngle = radarData.angle;
		this.currentValue = radarData.value;
	}

	@Override
	public void mouseWheelMoved(MouseWheelEvent e) {
		// zoom
		double d = e.getPreciseWheelRotation();

		int zoomCounter = this.zoomCounter;
		float currentZoom = this.currentZoom;
		/*
		 * calculating the zoom with acceleration of mouse wheel d : 25 = x : 0.05
		 */

		if (d < 0) {
			currentZoom -= ZOOM_STEP;
			zoomCounter -= 1;
		} else if (d > 0) {
			currentZoom += ZOOM_STEP;
			zoomCounter += 1;
		} else {
			return;
		}

		if (zoomCounter < 0 || zoomCounter >= MAX_ZOOM) {
			return;
		}

		this.zoomCounter = zoomCounter;
		this.currentZoom = currentZoom;
	}

	@Override
	public void mouseClicked(MouseEvent e) {
		// TODO Auto-generated method stub
	}

	@Override
	public void mousePressed(MouseEvent e) {
	}

	@Override
	public void mouseReleased(MouseEvent e) {
		this.lastMouseY = -1;
		this.lastMouseX = -1;

		// System.out.println("released");

	}

	@Override
	public void mouseEntered(MouseEvent e) {
	}

	@Override
	public void mouseExited(MouseEvent e) {
	}

	public void mouseRotateX(MouseEvent e) {
		if (lastMouseX == -1) {
			lastMouseX = e.getX();
			return;
		}

		final int diff = e.getX() - lastMouseX;
		final int maxH = this.getWidth();
		/*
		 * This is a proportion diff : maxH = x : 360
		 */
		final float rotationUpdate = diff * 360.f / maxH;
		updateXRotation(rotationUpdate);

		// saving the y
		lastMouseX = e.getX();
	}

	public void mouseRotateY(MouseEvent e) {
		if (lastMouseY == -1) {
			lastMouseY = e.getY();
			return;
		}

		final int diff = e.getY() - lastMouseY;
		final int maxH = this.getHeight();
		/*
		 * This is a proportion diff : maxH = x : 360
		 */
		final float rotationUpdate = diff * 360.f / maxH;
		updateYRotation(rotationUpdate);

		// saving the y
		lastMouseY = e.getY();
	}

	@Override
	public void mouseDragged(MouseEvent e) {
		e.consume();
		if (e.isShiftDown()) {
			mouseRotateX(e);
		} else {
			mouseRotateY(e);
		}

	}

	@Override
	public void mouseMoved(MouseEvent e) {
	}

	public int getDesideredFPS() {
		return animator.getFPS();
	}

	public void setDesideredFPS(int desideredFPS) {
		animator.setFPS(desideredFPS);
	}

}
