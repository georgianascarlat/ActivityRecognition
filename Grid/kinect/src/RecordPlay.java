import SimpleOpenNI.*;
import processing.core.*;

public class RecordPlay extends PApplet {

	private static final String WRITE_TO_FILE = "/home/gia/An4/Licenta/kinect/data/recorded/test15.oni";
	private static final String READ_FROM_FILE = "/home/gia/An4/Licenta/kinect/data/recorded/test1.oni";
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private SimpleOpenNI context;
	private boolean recordFlag = false;

	public void setup() {
		context = new SimpleOpenNI(this);

		
		
		if (recordFlag == false) {
			// playing, this works without the camera
			if (context.openFileRecording(READ_FROM_FILE) == false) {
				println("can't find recording !!!!");
				exit();
			}

			// it's possible to run the sceneAnalyzer over the recorded data
			// stream
			if (context.enableScene() == false) {
				println("can't setup scene!!!!");
				exit();
				return;
			}

			println("This file has " + context.framesPlayer() + " frames.");
		} else {
			// recording
			// enable depthMap generation
			
			if (context.enableDepth() == false) {
				println("Can't open the depthMap, maybe the camera is not connected!");
				exit();
				return;
			}

			// enable ir generation
			if (context.enableRGB() == false) {
				println("Can't open the rgbMap, maybe the camera is not connected or there is no rgbSensor!");
				exit();
				return;
			}

			// enable skeleton generation for all joints
			context.enableUser(SimpleOpenNI.SKEL_PROFILE_ALL);
			
			// setup the recording
			context.enableRecorder(SimpleOpenNI.RECORD_MEDIUM_FILE, WRITE_TO_FILE);

			
			  
		
			// select the recording channels
			context.addNodeToRecording(SimpleOpenNI.NODE_DEPTH,
					SimpleOpenNI.CODEC_16Z_EMB_TABLES);
			context.addNodeToRecording(SimpleOpenNI.NODE_IMAGE,
					SimpleOpenNI.CODEC_JPEG);
		}

		// set window size
		if ((context.nodes() & SimpleOpenNI.NODE_DEPTH) != 0) {
			if ((context.nodes() & SimpleOpenNI.NODE_IMAGE) != 0)
				// depth + rgb
				size(context.depthWidth() + 10 + context.rgbWidth(),
						context.depthHeight() > context.rgbHeight() ? context
								.depthHeight() : context.rgbHeight());
			else
				// only depth
				size(context.depthWidth(), context.depthHeight());
		} else
			exit();
	}

	public void draw() {
		// update
		context.update();

		background(200, 0, 0);

		// draw the cam data
		if ((context.nodes() & SimpleOpenNI.NODE_DEPTH) != 0) {
			if ((context.nodes() & SimpleOpenNI.NODE_IMAGE) != 0) {
				image(context.depthImage(), 0, 0);
				image(context.rgbImage(), context.depthWidth() + 10, 0);
			} else
				image(context.depthImage(), 0, 0);
		}

		if ((context.nodes() & SimpleOpenNI.NODE_SCENE) != 0)
			image(context.sceneImage(), 0, 0);

		// draw timeline
		if (recordFlag == false)
			drawTimeline();
		
		//System.out.println("La la");
		
		int i;
		int userCount = context.getNumberOfUsers();
		for (i = 1; i <= userCount; i++) {
			// check if the skeleton is being tracked
			if (context.isTrackingSkeleton(i)) {
				drawSkeleton(i); // draw the skeleton
			}
		}

	}

	
	// draw the skeleton with the selected joints
		public void drawSkeleton(int userId) {
			// to get the 3d joint data

			

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

	public void drawTimeline() {
		pushStyle();

		stroke(255, 255, 0);
		line(10, height - 20, width - 10, height - 20);

		stroke(0);
		rectMode(CENTER);
		fill(255, 255, 0);

		int pos = (int) ((width - 2 * 10) * (float) context.curFramePlayer() / (float) context
				.framesPlayer());
		rect(pos, height - 20, 7, 17);

		popStyle();
	}

	public void keyPressed() {
		switch (key) {
		case CODED:
			switch (keyCode) {
			case LEFT:
				// jump back
				context.seekPlayer(-3, SimpleOpenNI.PLAYER_SEEK_CUR);
				break;
			case RIGHT:
				// jump forward
				context.seekPlayer(3, SimpleOpenNI.PLAYER_SEEK_CUR);
				break;
			case UP:
				// slow down
				context.setPlaybackSpeedPlayer(context.playbackSpeedPlayer() * 2.0f);
				println("playbackSpeedPlayer: " + context.playbackSpeedPlayer());
				break;
			case DOWN:
				// speed up
				context.setPlaybackSpeedPlayer(context.playbackSpeedPlayer() * 0.5f);
				println("playbackSpeedPlayer: " + context.playbackSpeedPlayer());
				break;
			}
			break;
		case ' ':
			// toggle pause
			context.setRepeatPlayer(!context.repeatPlayer());
			println("RepeatMode: " + context.repeatPlayer());
			break;
		case BACKSPACE:
			// restart
			context.seekPlayer(0, SimpleOpenNI.PLAYER_SEEK_SET);
			break;
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
