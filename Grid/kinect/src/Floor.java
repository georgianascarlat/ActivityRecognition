import java.util.ArrayList;
import java.util.List;

import javax.media.j3d.Transform3D;
import javax.vecmath.Point3f;

import processing.core.PVector;


public class Floor {
	
	private PVector normal, point;
	private List<Point3f> margins;
	public static final float EPSILON = (float) 0.5; 
	public static final double ANGLE = 3;
	

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
	
	

}
