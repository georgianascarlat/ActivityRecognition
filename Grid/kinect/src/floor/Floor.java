package floor;
import javax.vecmath.Point3f;
import java.util.ArrayList;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: gia
 * Date: 3/3/13
 * Time: 1:08 PM
 * To change this template use File | Settings | File Templates.
 */
public class Floor {

    private Point3f point1, point2, point3, point4;

    public Floor(Point3f point1, Point3f point2, Point3f point3, Point3f point4) {
        this.point1 = point1;
        this.point2 = point2;
        this.point3 = point3;
        this.point4 = point4;
    }



    public Floor(List<String> points){
        float x,y,z;
        String[] tokens;
        List<Point3f> point3fs = new ArrayList<Point3f>();


        if(points.size() != 4){
            System.err.println("Invalid input: "+points);
            return;
        }

        for(String point:points){
            tokens= point.split("\\s+");
            if(tokens.length != 3){
                System.err.println("Invalid input: "+point);
                return;
            }
            x = Float.parseFloat(tokens[0]);
            y = Float.parseFloat(tokens[1]);
            z = Float.parseFloat(tokens[2]);
            point3fs.add(new Point3f(x,y,z));
        }

        this.point1 = point3fs.get(0);
        this.point2 = point3fs.get(1);
        this.point3 = point3fs.get(2);
        this.point4 = point3fs.get(3);

    }

    public Point3f getPoint1() {
        return point1;
    }

    public void setPoint1(Point3f point1) {
        this.point1 = point1;
    }

    public Point3f getPoint2() {
        return point2;
    }

    public void setPoint2(Point3f point2) {
        this.point2 = point2;
    }

    public Point3f getPoint3() {
        return point3;
    }

    public void setPoint3(Point3f point3) {
        this.point3 = point3;
    }

    public Point3f getPoint4() {
        return point4;
    }

    public void setPoint4(Point3f point4) {
        this.point4 = point4;
    }

    @Override
    public String toString() {
        return "Floor{" +
                "point1=" + point1 +
                ", point2=" + point2 +
                ", point3=" + point3 +
                ", point4=" + point4 +
                '}';
    }

    private Point3f extractClosest(List<Point3f> points){
        Point3f closest = points.get(0);
        Point3f curr;
        float minDist = closest.z;

        for(int i=1;i<points.size();i++){

            curr = points.get(i);

            if(curr.z < minDist){
                minDist = curr.z;
                closest = curr;
            }
        }

        points.remove(closest);
        return closest;

    }

    public Point3f getLB() {
        Point3f leftBack, back1, back2;
        List<Point3f> backMargins = getBackMargins();

        back1 = backMargins.get(0);
        back2 = backMargins.get(1);

        if(back1.x < back2.x)
            leftBack = back1;
        else
            leftBack = back2;

        return leftBack;



    }

    public Point3f getRB() {
        Point3f rightBack, back1, back2;
        List<Point3f> backMargins = getBackMargins();

        back1 = backMargins.get(0);
        back2 = backMargins.get(1);

        if(back1.x > back2.x)
            rightBack = back1;
        else
            rightBack = back2;

        return rightBack;
    }

    public Point3f getRF() {

        Point3f rightFront, front1, front2;
        List<Point3f> allMargins = getAllMargins();

        front1 = extractClosest(allMargins);
        front2 = extractClosest(allMargins);

        if(front1.x > front2.x)
            rightFront = front1;
        else
            rightFront = front2;

        return rightFront;
    }

    private List<Point3f> getBackMargins() {

        List<Point3f> allMargins = getAllMargins();

        extractClosest(allMargins);
        extractClosest(allMargins);
        return allMargins;
    }

    private List<Point3f> getAllMargins() {

        List<Point3f> allMargins = new ArrayList<Point3f>();
        allMargins.add(point1);
        allMargins.add(point2);
        allMargins.add(point3);
        allMargins.add(point4);
        return allMargins;
    }



}
