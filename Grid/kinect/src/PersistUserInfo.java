import java.awt.Color;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import javax.vecmath.Point3f;

import floor.Map;
import floor.Snapshot;

import SimpleOpenNI.*;
import processing.core.*;

public class PersistUserInfo extends PApplet {

	private static final int MAX_ITERATIONS = 500;
	final static int CALIB_START = 0;
	final static int CALIB_NULLPOINT = 1;
	final static int CALIB_X_POINT = 2;
	final static int CALIB_Z_POINT = 3;
	final static int CALIB_DONE = 4;

	boolean screenFlag = true;
	int calibMode = CALIB_START;

	PVector nullPoint3d = new PVector();
	PVector xDirPoint3d = new PVector();
	PVector zDirPoint3d = new PVector();
	PVector tempVec1 = new PVector();
	PVector tempVec2 = new PVector();
	PVector tempVec3 = new PVector();

	PMatrix3D userCoordsysMat = new PMatrix3D();

	private static final int SNAPSHOT_INIT_DELAY = 1600;
	private static final int SNAPSHOT_DELAY = 600;

	private static final long serialVersionUID = 1L;
	private SimpleOpenNI context;
	private CountInceremnter incrementer;
	private boolean fromFile = true;
	private String fileName = System.getProperty("user.dir")+"/../data/recorded/test14.oni";
	private String rootDirectory = System.getProperty("user.dir")+"/../data/recorded/points/";

	private Floor floor;
	private floor.Floor the_floor = null;
	int heigthOffset = 0, widthOffset = 0;
	private int widthParts = 6;
	private int heigthParts = 5;
	private Map map;

	public void setup() {

		System.out.println(System.getProperty("user.dir"));
		// frameRate(300);

		// init context
		context = new SimpleOpenNI(this, SimpleOpenNI.RUN_MODE_MULTI_THREADED);

		// load from file if necessary
		if (fromFile) {
			if (context.openFileRecording(fileName) == false) {
				println("can't find recording !!!!");
				exit();
			}
		}

		// disable mirror
		context.setMirror(false);

		// enable depthMap generation
		context.enableDepth();

		if (context.enableRGB() == false) {
			println("Can't open the rgbMap, maybe the camera is not connected or there is no rgbSensor!");
			exit();
			return;
		}

		// enable scene generation
		if (context.enableScene() == false) {
			println("Can't open the sceneMap, maybe the camera is not connected!");
			exit();
			return;
		}

		// enable skeleton generation for all joints
		context.enableUser(SimpleOpenNI.SKEL_PROFILE_ALL);

		// align depth data to image data
		context.alternativeViewPointDepthToImage();

		background(200, 0, 0);

		stroke(0, 0, 255);
		strokeWeight(3);
		smooth();

		// set the size
		size(context.depthWidth() + context.rgbWidth() + 10,
				context.rgbHeight() + 150 + 20);

		heigthOffset = context.rgbHeight() + 10;
		map = new Map(context.depthWidth(), 150, widthParts, heigthParts);

		// schedule timer for counter increment used to index saved user info
		Timer timer = new Timer();
		incrementer = new CountInceremnter();
		timer.schedule(incrementer, SNAPSHOT_INIT_DELAY, SNAPSHOT_DELAY);

		// Create the font
		textFont(createFont("Georgia", 16));

	}

	public void draw() {
		PVector floorPoint = new PVector(), floorNormal = new PVector();
		int rightImageOffset = context.depthWidth() + 10;
		// update the cam
		context.update();
		// set background color
		background(100, 10, 10);

		// draw depthImageMap
		image(context.depthImage(), rightImageOffset , 0);

		// draw camera
		
		image(context.rgbImage(), 0, 0);

		// Put text to help you choose coordSys
		// draw text background
		pushStyle();
		noStroke();
		fill(0, 200, 0, 100);
		rect(0, 0, width, 40);
		popStyle();

		// write text to help the user choose the coordSys
		switch (calibMode) {
		case CALIB_START:
			text("To start the calibration press SPACE!", 5, 30);
			break;
		case CALIB_NULLPOINT:
			text("Set the nullpoint with the left mousebutton", 5, 30);
			break;
		case CALIB_X_POINT:
			text("Set the x-axis with the left mousebutton", 5, 30);
			break;
		case CALIB_Z_POINT:
			text("Set the z-axis with the left mousebutton", 5, 30);
			break;
		case CALIB_DONE:
			text("New nullpoint is defined!", 5, 30);
			break;
		}

		// draw
		drawCalibPoint();

		// draw the user defined coordinate system
		// with the size of 500mm
		if (context.hasUserCoordsys()) {
			PVector temp = new PVector();
			PVector nullPoint = new PVector();

			pushStyle();

			strokeWeight(3);
			noFill();

			context.convertRealWorldToProjective(new PVector(0, 0, 0), tempVec1);
			stroke(255, 255, 255, 150);
			ellipse(tempVec1.x, tempVec1.y, 10, 10);

			context.convertRealWorldToProjective(new PVector(500, 0, 0),
					tempVec2);
			stroke(255, 0, 0, 150);
			line(tempVec1.x, tempVec1.y, tempVec2.x, tempVec2.y);

			context.convertRealWorldToProjective(new PVector(0, 500, 0),
					tempVec2);
			stroke(0, 255, 0, 150);
			line(tempVec1.x, tempVec1.y, tempVec2.x, tempVec2.y);

			context.convertRealWorldToProjective(new PVector(0, 0, 500),
					tempVec2);
			stroke(0, 0, 255, 150);
			line(tempVec1.x, tempVec1.y, tempVec2.x, tempVec2.y);

			popStyle();
		}

		// draw the skeleton if it's available
		int i;
		int userCount = context.getNumberOfUsers();
		for (i = 1; i <= userCount; i++) {
			// check if the skeleton is being tracked
			if (context.isTrackingSkeleton(i)) {
				drawSkeleton(i); // draw the skeleton
			}
		}

		// get the floor plane

		context.getSceneFloor(floorPoint, floorNormal);
		floor = new Floor(floorNormal, floorPoint);

		PVector[] pointsMap = context.depthMapRealWorld();
		List<Point3f> planPoints = new ArrayList<Point3f>();
		PVector proj = new PVector();
		for (i = 0; i < pointsMap.length; i++) {
			if (floor.inPlan(pointsMap[i])) {
				proj = pointsMap[i];
				planPoints.add(new Point3f(proj.x, proj.y, proj.z));

			}
		}

		// obtain floor margins from plan points
		floor.setFloorMargins(planPoints);

		// showFloorPoints(planPoints, proj);
		fitFloorMargins();
		showFloorBounds();

	}

	// draw the points chosen for the coordSys so far
	void drawCalibPoint() {
		pushStyle();

		strokeWeight(3);
		noFill();

		switch (calibMode) {
		case CALIB_START:
			break;
		case CALIB_NULLPOINT:
			context.convertRealWorldToProjective(nullPoint3d, tempVec1);

			stroke(255, 255, 255, 150);
			ellipse(tempVec1.x, tempVec1.y, 10, 10);
			break;
		case CALIB_X_POINT:
			// draw the null point
			context.convertRealWorldToProjective(nullPoint3d, tempVec1);
			context.convertRealWorldToProjective(xDirPoint3d, tempVec2);

			stroke(255, 255, 255, 150);
			ellipse(tempVec1.x, tempVec1.y, 10, 10);

			stroke(255, 0, 0, 150);
			ellipse(tempVec2.x, tempVec2.y, 10, 10);
			line(tempVec1.x, tempVec1.y, tempVec2.x, tempVec2.y);

			break;
		case CALIB_Z_POINT:

			context.convertRealWorldToProjective(nullPoint3d, tempVec1);
			context.convertRealWorldToProjective(xDirPoint3d, tempVec2);
			context.convertRealWorldToProjective(zDirPoint3d, tempVec3);

			stroke(255, 255, 255, 150);
			ellipse(tempVec1.x, tempVec1.y, 10, 10);

			stroke(255, 0, 0, 150);
			ellipse(tempVec2.x, tempVec2.y, 10, 10);
			line(tempVec1.x, tempVec1.y, tempVec2.x, tempVec2.y);

			stroke(0, 0, 255, 150);
			ellipse(tempVec3.x, tempVec3.y, 10, 10);
			line(tempVec1.x, tempVec1.y, tempVec3.x, tempVec3.y);

			break;
		case CALIB_DONE:

			break;
		}

		popStyle();
	}

	// display all floor points
	private void showFloorPoints(List<Point3f> planPoints, PVector proj) {
		int i;
		PVector pp;
		for (i = 0; i < planPoints.size(); i++) {
			pp = new PVector(planPoints.get(i).x, planPoints.get(i).y,
					planPoints.get(i).z);
			context.convertRealWorldToProjective(pp, proj);
			stroke(255, 0, 0);
			point(proj.x, proj.y);
		}

	}

	// display a rectangle that fits the floor
	private void showFloorBounds() {
		PVector p1 = new PVector(), p2 = new PVector();
		List<Point3f> floorM = floor.getMargins();

		p1 = new PVector(floorM.get(0).x, floorM.get(0).y, floorM.get(0).z);
		context.convertRealWorldToProjective(p1, p1);
		p2 = new PVector(floorM.get(1).x, floorM.get(1).y, floorM.get(1).z);
		context.convertRealWorldToProjective(p2, p2);
		stroke(0, 10, 255);
		line(p1.x, p1.y, p2.x, p2.y);

		p1 = new PVector(floorM.get(1).x, floorM.get(1).y, floorM.get(1).z);
		context.convertRealWorldToProjective(p1, p1);
		p2 = new PVector(floorM.get(2).x, floorM.get(2).y, floorM.get(2).z);
		context.convertRealWorldToProjective(p2, p2);
		stroke(0, 10, 255);
		line(p1.x, p1.y, p2.x, p2.y);

		p1 = new PVector(floorM.get(2).x, floorM.get(2).y, floorM.get(2).z);
		context.convertRealWorldToProjective(p1, p1);
		p2 = new PVector(floorM.get(3).x, floorM.get(3).y, floorM.get(3).z);
		context.convertRealWorldToProjective(p2, p2);
		stroke(0, 10, 255);
		line(p1.x, p1.y, p2.x, p2.y);

		p1 = new PVector(floorM.get(3).x, floorM.get(3).y, floorM.get(3).z);
		context.convertRealWorldToProjective(p1, p1);
		p2 = new PVector(floorM.get(0).x, floorM.get(0).y, floorM.get(0).z);
		context.convertRealWorldToProjective(p2, p2);
		stroke(0, 10, 255);
		line(p1.x, p1.y, p2.x, p2.y);
	}
	
	public void fitFloorMargins(){
		
		PVector p1 = new PVector(), p2 = new PVector(), p3 = new PVector(), p4 = new PVector();
		List<Point3f> floorM;
		List<PVector> projectedPoints = new LinkedList<PVector>();
		int k = 0;

		while(true){
			
			k++;
			projectedPoints = new LinkedList<PVector>();
			
			floorM = floor.getMargins();
			p1 = new PVector(floorM.get(0).x, floorM.get(0).y, floorM.get(0).z);
			context.convertRealWorldToProjective(p1, p1);
			projectedPoints.add(p1);
			
			p2 = new PVector(floorM.get(1).x, floorM.get(1).y, floorM.get(1).z);
			context.convertRealWorldToProjective(p2, p2);
			projectedPoints.add(p2);
			
			p3 = new PVector(floorM.get(2).x, floorM.get(2).y, floorM.get(2).z);
			context.convertRealWorldToProjective(p3, p3);
			projectedPoints.add(p3);
			
			p4 = new PVector(floorM.get(3).x, floorM.get(3).y, floorM.get(3).z);
			context.convertRealWorldToProjective(p4, p4);
			projectedPoints.add(p4);
			
			
			if(insideImage(p1) && insideImage(p2) && insideImage(p3) && insideImage(p4))
				break;
			
			
			floor.adjustMargins(projectedPoints,context.depthWidth(), context.depthHeight());
			
			
			if(k == MAX_ITERATIONS)
				break;
			
			
		}
		
	}
	
	public boolean insideImage(PVector p){
		return (p.x >= 0 && p.x < context.depthWidth() && p.y >=0 && p.y < context.depthHeight());
	}

	// draw the skeleton with the selected joints
	public void drawSkeleton(int userId) {
		
		// save joint positions
		writeJointPositionsToFile(userId);

		// save rgb image
		PImage rgbImage = context.rgbImage();
		rgbImage.save(rootDirectory + "img" + incrementer.counter + ".jpg");

		// draw limbs
		context.drawLimb(userId, SimpleOpenNI.SKEL_HEAD, SimpleOpenNI.SKEL_NECK);

		context.drawLimb(userId, SimpleOpenNI.SKEL_NECK,
				SimpleOpenNI.SKEL_LEFT_SHOULDER);
		context.drawLimb(userId, SimpleOpenNI.SKEL_LEFT_SHOULDER,
				SimpleOpenNI.SKEL_LEFT_ELBOW);
		context.drawLimb(userId, SimpleOpenNI.SKEL_LEFT_ELBOW,
				SimpleOpenNI.SKEL_LEFT_HAND);

		context.drawLimb(userId, SimpleOpenNI.SKEL_NECK,
				SimpleOpenNI.SKEL_RIGHT_SHOULDER);
		context.drawLimb(userId, SimpleOpenNI.SKEL_RIGHT_SHOULDER,
				SimpleOpenNI.SKEL_RIGHT_ELBOW);
		context.drawLimb(userId, SimpleOpenNI.SKEL_RIGHT_ELBOW,
				SimpleOpenNI.SKEL_RIGHT_HAND);

		context.drawLimb(userId, SimpleOpenNI.SKEL_LEFT_SHOULDER,
				SimpleOpenNI.SKEL_TORSO);
		context.drawLimb(userId, SimpleOpenNI.SKEL_RIGHT_SHOULDER,
				SimpleOpenNI.SKEL_TORSO);

		context.drawLimb(userId, SimpleOpenNI.SKEL_TORSO,
				SimpleOpenNI.SKEL_LEFT_HIP);
		context.drawLimb(userId, SimpleOpenNI.SKEL_LEFT_HIP,
				SimpleOpenNI.SKEL_LEFT_KNEE);
		context.drawLimb(userId, SimpleOpenNI.SKEL_LEFT_KNEE,
				SimpleOpenNI.SKEL_LEFT_FOOT);

		context.drawLimb(userId, SimpleOpenNI.SKEL_TORSO,
				SimpleOpenNI.SKEL_RIGHT_HIP);
		context.drawLimb(userId, SimpleOpenNI.SKEL_RIGHT_HIP,
				SimpleOpenNI.SKEL_RIGHT_KNEE);
		context.drawLimb(userId, SimpleOpenNI.SKEL_RIGHT_KNEE,
				SimpleOpenNI.SKEL_RIGHT_FOOT);
	}

	

	// write joint positions to file
	private void writeJointPositionsToFile(int userId) {

		BufferedWriter out = null;
		List<Point3f> floorMargins = floor.getMargins();
		Point3f margin;
		String fileName2 = rootDirectory + "skel" + incrementer.counter;

		try {

			out = new BufferedWriter(new FileWriter(fileName2));

			out.append("FLOOR_BOUNDS: \n");
			for (int i = 0; i < 4; i++) {
				margin = floorMargins.get(i);
				out.append(margin.x + " " + margin.y + " " + margin.z + "\n");
			}

			writeJoint(userId, out, SimpleOpenNI.SKEL_HEAD, "HEAD");
			writeJoint(userId, out, SimpleOpenNI.SKEL_NECK, "NECK");

			writeJoint(userId, out, SimpleOpenNI.SKEL_LEFT_SHOULDER,
					"LEFT_SHOULDER");
			writeJoint(userId, out, SimpleOpenNI.SKEL_LEFT_ELBOW, "LEFT_ELBOW");
			writeJoint(userId, out, SimpleOpenNI.SKEL_LEFT_HAND, "LEFT_HAND");

			writeJoint(userId, out, SimpleOpenNI.SKEL_RIGHT_SHOULDER,
					"RIGHT_SHOULDER");
			writeJoint(userId, out, SimpleOpenNI.SKEL_RIGHT_ELBOW,
					"RIGHT_ELBOW");
			writeJoint(userId, out, SimpleOpenNI.SKEL_RIGHT_HAND, "RIGHT_HAND");

			writeJoint(userId, out, SimpleOpenNI.SKEL_TORSO, "TORSO");

			writeJoint(userId, out, SimpleOpenNI.SKEL_LEFT_HIP, "LEFT_HIP");
			writeJoint(userId, out, SimpleOpenNI.SKEL_LEFT_KNEE, "LEFT_KNEE");
			writeJoint(userId, out, SimpleOpenNI.SKEL_LEFT_FOOT, "LEFT_FOOT");

			writeJoint(userId, out, SimpleOpenNI.SKEL_RIGHT_HIP, "RIGHT_HIP");
			writeJoint(userId, out, SimpleOpenNI.SKEL_RIGHT_KNEE, "RIGHT_KNEE");
			writeJoint(userId, out, SimpleOpenNI.SKEL_RIGHT_FOOT, "RIGHT_FOOT");

		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (out != null) {
				try {
					out.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}

		try {
			showFloorHighlight(fileName2);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	private void showFloorHighlight(String fileName2) throws IOException {

		Snapshot snapshot;
		if (the_floor != null)
			snapshot = new Snapshot(fileName2, widthParts, heigthParts,
					the_floor);
		else {
			snapshot = new Snapshot(fileName2, widthParts, heigthParts);
			the_floor = snapshot.getFloor();
		}

		int highLight = snapshot.getUserOnFloorPosition();
		int x, y;

		for (int line = 0; line < heigthParts; line++) {
			for (int column = 0; column < widthParts; column++) {

				x = column * map.getWidthChunk();
				y = line * map.getHeightChunk();

				if ((line * map.getWidthParts() + column) == highLight) {
					fill(255, 0, 0);

				} else {
					fill(10, 23, 255);

				}
				rect(x + widthOffset, y + heigthOffset, map.getWidthChunk(),
						map.getHeightChunk());

			}
		}

	}

	// write joint position to file
	private void writeJoint(int userId, BufferedWriter out, int joint,
			String name) throws IOException {

		PVector jointPos = new PVector();

		context.getJointPositionSkeleton(userId, joint, jointPos);

		out.append(name + " " + jointPos.x + " " + jointPos.y + " "
				+ jointPos.z + "\n");
	}

	// -----------------------------------------------------------------
	// SimpleOpenNI events
	
	public void keyPressed() {
		switch (key) {
		case '1':
			screenFlag = !screenFlag;
			break;
		case ' ':
			calibMode++;
			if (calibMode > CALIB_DONE) {
				calibMode = CALIB_START;
				context.resetUserCoordsys();
			} else if (calibMode == CALIB_DONE) {
				// set the calibration
				context.setUserCoordsys(nullPoint3d.x, nullPoint3d.y,
						nullPoint3d.z, xDirPoint3d.x, xDirPoint3d.y,
						xDirPoint3d.z, zDirPoint3d.x, zDirPoint3d.y,
						zDirPoint3d.z);

				println("Set the user define coordinatesystem");
				println("nullPoint3d: " + nullPoint3d);
				println("xDirPoint3d: " + xDirPoint3d);
				println("zDirPoint3d: " + zDirPoint3d);

				/*
				 * // test context.getUserCoordsysTransMat(userCoordsysMat);
				 * PVector temp = new PVector();
				 * 
				 * userCoordsysMat.mult(new PVector(0, 0, 0), temp);
				 * println("PVector(0,0,0): " + temp);
				 * 
				 * userCoordsysMat.mult(new PVector(500, 0, 0), temp);
				 * println("PVector(500,0,0): " + temp);
				 * 
				 * userCoordsysMat.mult(new PVector(0, 500, 0), temp);
				 * println("PVector(0,500,0): " + temp);
				 * 
				 * userCoordsysMat.mult(new PVector(0, 0, 500), temp);
				 * println("PVector(0,0,500): " + temp);
				 */
			}

			break;
		}
	}

	public void mousePressed() {
		if (mouseButton == LEFT) {
			PVector[] realWorldMap = context.depthMapRealWorld();
			int index = mouseX + mouseY * context.depthWidth();

			switch (calibMode) {
			case CALIB_NULLPOINT:
				nullPoint3d.set(realWorldMap[index]);
				break;
			case CALIB_X_POINT:
				xDirPoint3d.set(realWorldMap[index]);
				break;
			case CALIB_Z_POINT:
				zDirPoint3d.set(realWorldMap[index]);
				break;
			}
		} else {
			PVector[] realWorldMap = context.depthMapRealWorld();
			int index = mouseX + mouseY * context.depthWidth();

			println("Point3d: " + realWorldMap[index].x + ","
					+ realWorldMap[index].y + "," + realWorldMap[index].z);
		}
	}

	public void mouseDragged() {
		if (mouseButton == LEFT) {
			PVector[] realWorldMap = context.depthMapRealWorld();
			int index = mouseX + mouseY * context.depthWidth();

			switch (calibMode) {
			case CALIB_NULLPOINT:
				nullPoint3d.set(realWorldMap[index]);
				break;
			case CALIB_X_POINT:
				xDirPoint3d.set(realWorldMap[index]);
				break;
			case CALIB_Z_POINT:
				zDirPoint3d.set(realWorldMap[index]);
				break;
			}
		}

	}
	public void onNewUser(int userId) {
		println("onNewUser - userId: " + userId);
		println("  start pose detection");

		context.startPoseDetection("Psi", userId);
	}

	public void onLostUser(int userId) {
		println("onLostUser - userId: " + userId);
	}

	public void onStartCalibration(int userId) {
		println("onStartCalibration - userId: " + userId);
	}

	public void onEndCalibration(int userId, boolean successfull) {
		println("onEndCalibration - userId: " + userId + ", successfull: "
				+ successfull);

		if (successfull) {
			println("  User calibrated !!!");
			context.startTrackingSkeleton(userId);

		} else {
			println("  Failed to calibrate user !!!");
			println("  Start pose detection");
			context.startPoseDetection("Psi", userId);
		}
	}

	public void onStartPose(String pose, int userId) {
		println("onStartPose - userId: " + userId + ", pose: " + pose);
		println(" stop pose detection");

		context.stopPoseDetection(userId);
		context.requestCalibrationSkeleton(userId, true);

	}

	public void onEndPose(String pose, int userId) {
		println("onEndPose - userId: " + userId + ", pose: " + pose);
	}
}

class CountInceremnter extends TimerTask {

	public int counter;

	public CountInceremnter() {
		counter = 0;
	}

	@Override
	public void run() {
		counter++;
	}
}
