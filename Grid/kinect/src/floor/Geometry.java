package floor;
import javax.vecmath.Point3f;

/**
 * Created with IntelliJ IDEA.
 * User: gia
 * Date: 3/3/13
 * Time: 5:29 PM
 * To change this template use File | Settings | File Templates.
 */
public class Geometry {
	
	public static double distance(Point3f p1, Point3f p2){
		return Math.sqrt(Math.pow(p1.x-p2.x, 2)+ Math.pow(p1.y - p2.y, 2) + Math.pow(p1.z - p2.z,2));
	}

    public static float dotProduct(Point3f p1, Point3f p2){
        return p1.x*p2.x + p1.y*p2.y + p1.z*p2.z;
    }

    public static Point3f crossProduct(Point3f a,Point3f b){
        float a1 = a.x,a2 = a.y,a3=a.z;
        float b1 = b.x,b2 = b.y,b3=b.z;
        float x,y,z;
        x = a2*b3 - a3*b2;
        y = a3*b1 - a1*b3;
        z = a1*b2 - a2*b1;
        return new Point3f(x,y,z);


    }

    public static Point3f sub(Point3f p1,Point3f p2){
        float x,y,z;
        x = p1.x - p2.x;
        y = p1.y - p2.y;
        z = p1.z - p2.z;

        return new Point3f(x,y,z);
    }

    public static Point3f add(Point3f p1,Point3f p2){
        float x,y,z;
        x = p1.x + p2.x;
        y = p1.y + p2.y;
        z = p1.z + p2.z;

        return new Point3f(x,y,z);
    }

    public static Point3f mul(Point3f p, float a){
        return new Point3f(a*p.x,a*p.y,a*p.z);
    }

    // project P on AB
    public static Point3f projectPointOnLine(Point3f P, Point3f A, Point3f B){
        Point3f AB = sub(B,A);
        Point3f AP = sub(P,A);
        float  t = dotProduct(AB,AP)/dotProduct(AB,AB);
        return add(A,mul(AB,t));

    }

    public static float determinant(Point3f line1,Point3f line2,Point3f line3){
        float prod1,prod2;

        prod1 = line1.x*line2.y*line3.z + line1.z*line2.x*line3.y + line1.y*line2.z*line3.x;

        prod2 = line1.z*line2.y*line3.x + line1.x*line2.z*line3.y + line1.y*line2.x*line3.z;

        return prod1 - prod2;
    }

    public static Point3f planNormal(Point3f p1, Point3f p2, Point3f p3){
        return crossProduct(sub(p2, p1), sub(p3, p1));

    }

    public static Point3f projectPointOnPlan(Point3f planNormal,Point3f planPoint,Point3f point){

        float a=planNormal.x,b=planNormal.y,c=planNormal.z;
        float d = -(a*planPoint.x + b*planPoint.y + c*planPoint.z);
        float u = point.x,v=point.y,w = point.z;
        float t0 = -(a*u + b*v + c*w + d)/(a*a + b*b + c*c);
        float x,y,z;
        x = u+a*t0;
        y = v+b*t0;
        z = w+c*t0;

        return new Point3f(x,y,z);


    }

    public static boolean onLine(Point3f P,Point3f A,Point3f B){

        return P.equals(projectPointOnLine(P,A,B));

    }


    public static void main(String[] args){

        Point3f  normal =  planNormal(new Point3f(0, 0, 0), new Point3f(0, 0, 1), new Point3f(1, 0, 0));
        System.out.println(normal);
        System.out.println(projectPointOnPlan(normal,new Point3f(0,0,0),new Point3f(1,1,1)));
        System.out.println(projectPointOnLine(new Point3f(1,1,0),new Point3f(0,0,0),new Point3f(2,0,0)));
        //System.out.println(projectPointOnLine(new Point3f(1,0,1),new Point3f(10,10,10),new Point3f(13,13,13)));

        //System.out.println(onLine(new Point3f(11,11,1),new Point3f(1,1,1),new Point3f(2,2,2)));

    }

	public static Point3f getPointOnLine(Point3f p1, Point3f p2, double raport) {
		Point3f point =  new Point3f();
		point.x = (float) (raport*(p2.x - p1.x)+p1.x);
		point.y = (float) (raport*(p2.y - p1.y)+p1.y);
		point.z = (float) (raport*(p2.z - p1.z)+p1.z);
		
		return point;
	}


}
