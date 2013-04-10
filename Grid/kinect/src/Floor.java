import java.util.ArrayList;
import java.util.List;

import javax.media.j3d.Transform3D;
import javax.vecmath.Point3f;

import floor.Geometry;

import processing.core.PVector;


public class Floor {
	
	private static final double EPS = 0.2;
	private PVector normal, point;
	private List<Point3f> margins;
	public static final float EPSILON = (float) 0.5; 
	public static final double ANGLE = 3;
	public static final double COMPRESS_DISTANCE = 5;
	

	public Floor(PVector normal, PVector point) {
		super();
		this.normal = normal;
		this.point = point;
		this.margins = new ArrayList<Point3f>();
	}
	
	
public void setFloorMargins(List<Point3f> planPoints) {
		
		
		Transform3D transform ;
		double beta = -Math.atan2(normal.z, normal.y);
		double len = Math.sqrt(Math.pow(normal.y, 2)+ Math.pow(normal.z, 2));
		double alpha = Math.atan2(normal.x, len);
		
		List<Point3f> margins = null, bestMargins = null;
		int rotations = (int) (360/ANGLE);
		double bestRotation = 0;
		
		
		transform = getRotationTransform(beta,0,alpha);
		
		transformPoints(planPoints, transform);	

		
		for(int i=1;i<=rotations;i++){
			transform = getRotationTransform(0,Math.toRadians(ANGLE),0);
			transformPoints(planPoints, transform);	
			margins= getMargins(planPoints);
			if(bestMargins == null || smallerArea(margins,bestMargins)){
				bestMargins = margins;
				bestRotation = i*ANGLE;
			}			
			
		}
		
				
		transform = getRotationTransform(-beta,-Math.toRadians(bestRotation),-alpha);		
		
		transformPoints(bestMargins, transform);	
				
		transform = getRotationTransform(-beta,0,-alpha);	
		
		transformPoints(planPoints, transform);
		
		
		this.margins =  bestMargins;
		
		
				
	}
private boolean smallerArea(List<Point3f> rect1, List<Point3f> rect2) {
	
	float L1,l1,L2,l2;
	L1 = rect1.get(0).distance(rect1.get(1));
	l1 = rect1.get(1).distance(rect1.get(2));
	
	L2 = rect2.get(0).distance(rect2.get(1));
	l2 = rect2.get(1).distance(rect2.get(2));
	
	
	return L1*l1 < L2*l2;
}


private void transformPoints(List<Point3f> planPoints, Transform3D transform) {
	int size = planPoints.size();
	for(int i=0;i<size;i++){			
		transform.transform(planPoints.get(i));
	}
}

private Transform3D getRotationTransform(double xRot,double yRot, double zRot){
	Transform3D transform = new Transform3D();
	Transform3D someRotation;
	
	someRotation = new Transform3D();
	someRotation.rotX(xRot);		
	transform.mul(someRotation);
	
	someRotation = new Transform3D();
	someRotation.rotY(yRot);
	transform.mul(someRotation);
	
	someRotation = new Transform3D();
	someRotation.rotZ(zRot);
	transform.mul(someRotation);
	
	return transform;
}
private List<Point3f> getMargins(List<Point3f> planPoints) {
	
	List<Point3f> margins = new ArrayList<Point3f>();
	Point3f p;
		
	float medY = 0;
	float minX= Float.MAX_VALUE,maxX = Float.MIN_VALUE ,minZ = Float.MAX_VALUE,maxZ = Float.MIN_VALUE ;
	int size = planPoints.size();
	for(int i=0;i<size;i++){
		p = planPoints.get(i);
		if(p.x < minX)
			minX = p.x;
		if(p.x > maxX)
			maxX = p.x;
		if(p.z < minZ)
			minZ = p.z;
		if(p.z > maxZ)
			maxZ = p.z;
		medY+=p.y;
	}
	
	medY = medY/size;
	
	margins.add(new Point3f(minX,medY,minZ));
	margins.add(new Point3f(maxX,medY,minZ));
	margins.add(new Point3f(maxX,medY,maxZ));
	margins.add(new Point3f(minX,medY,maxZ));
	
	return margins;
}

public boolean inPlan(PVector M) {
	float ec = normal.x*(M.x-point.x)+ normal.y*(M.y-point.y) + normal.z*(M.z - point.z);
	
	return Math.abs(ec) < EPSILON;
}

	
	

	public List<Point3f> getMargins() {
		return margins;
	}



	public void setMargins(List<Point3f> margins) {
		this.margins = margins;
	}



	public PVector getNormal() {
		return normal;
	}

	public void setNormal(PVector normal) {
		this.normal = normal;
	}

	public PVector getPoint() {
		return point;
	}

	public void setPoint(PVector point) {
		this.point = point;
	}


	/**
	 * Adjust margins to fit camera view.
	 * 
	 * @param projectedPoints
	 * @param maxWidth
	 * @param maxHeight
	 */
	public void adjustMargins(List<PVector> projectedPoints,
			int minWidth, int maxWidth,
			int minHeight, int maxHeight) {
		PVector point,prev,next;
		int size = projectedPoints.size(), prevIndex, nextIndex;
		
		
		for(int index=0;index<size;index++){
			
			nextIndex = nextIndex(size, index);
			prevIndex = previousIndex(size, index);
			
			next = projectedPoints.get(nextIndex);
			prev = projectedPoints.get(prevIndex);
			
			point = projectedPoints.get(index);
			
			checkPoint(projectedPoints,size, index, point.x < minWidth,next.x < prev.x);
			checkPoint(projectedPoints,size, index, point.y < minHeight,next.y < prev.y);
			checkPoint(projectedPoints,size, index, point.x >= maxWidth, next.x > prev.x);
			checkPoint(projectedPoints,size, index, point.y >=  maxHeight,next.y > prev.y);
			
			
		}
		
	}


	private void checkPoint(List<PVector> projectedPoints, int size, int index, boolean condition1, boolean condition2) {
		
		PVector point, prev, next;
		int prevIndex,nextIndex;
		
		point = projectedPoints.get(index);
		if(condition1){
			
			nextIndex = nextIndex(size, index);
			prevIndex = previousIndex(size, index);
			next = projectedPoints.get(nextIndex);
			prev = projectedPoints.get(prevIndex);
			
			if(condition2){
				moveLine(index,nextIndex,prevIndex,nextIndex(size, nextIndex));
			}
			else{
				moveLine(index,prevIndex,nextIndex,previousIndex(size, prevIndex));
			}
		}
	}


	private void moveLine(int p1, int p2, int m1, int m2) {
		
		Point3f P1 = margins.get(p1), P2 = margins.get(p2),
				M1 = margins.get(m1), M2 = margins.get(m2),
				newP1,newP2;
		newP1 = getCloserPoint(P1, M1);
		newP2 = getCloserPoint(P2, M2);
		
		margins.set(p1, newP1);
		margins.set(p2, newP2);
		
	}


	private Point3f getCloserPoint(Point3f P1, Point3f M1) {
		Point3f newP1;
		double k,  P1M1 = Geometry.distance(P1, M1);
		k = COMPRESS_DISTANCE/P1M1;
		newP1 = Geometry.getPointOnLine(P1,M1,k);
		if((k + Geometry.distance(newP1, M1)/P1M1 - 1) > EPS){
			k = -k;
			newP1 = Geometry.getPointOnLine(P1,M1,k);
			
		}
		
		return newP1;
	}


	private int previousIndex(int num, int i) {
		return i==0?(num-1):(i-1);
	}


	private int nextIndex(int num, int i) {
		return (i+1)%num;
	}
	
	

}
