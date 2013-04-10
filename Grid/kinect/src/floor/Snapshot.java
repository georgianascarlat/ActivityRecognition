package floor;
import javax.vecmath.Point3f;
import java.io.*;
import java.util.LinkedList;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: gia
 * Date: 3/3/13
 * Time: 1:07 PM
 * To change this template use File | Settings | File Templates.
 */

public class Snapshot {


    private User user;
    private Floor floor;
    private int widthParts, heigthParts;
    private float EPSILON;

    public Snapshot(User user, Floor floor, int widthParts, int heightParts) {
        this.user = user;
        this.floor = floor;
        this.widthParts = widthParts;
        this.heigthParts = heightParts;
        this.EPSILON = floor.getPoint1().distance(floor.getPoint2())/1000;
    }

    public Snapshot(String fileName, int widthParts,int heightParts) throws IOException {


        FileInputStream fstream = new FileInputStream(fileName);

        DataInputStream in = new DataInputStream(fstream);
        BufferedReader br = new BufferedReader(new InputStreamReader(in));

        this.floor = readFloor(br);
        this.user = readUser(br);
        this.widthParts = widthParts;
        this.heigthParts = heightParts;

        in.close();

    }
    
    public Snapshot(String fileName, int widthParts,int heightParts,Floor floor) throws IOException{
    	this(fileName,widthParts,heightParts);
    	this.floor = floor;
    }

    private User readUser(BufferedReader br) throws IOException {

        String strLine;
        User user;
        List<String> strings;

        strings = new LinkedList<String>();

        while ((strLine = br.readLine()) != null) {

            strings.add(strLine);
        }

        user = new User(strings);
        return user;
    }


    private Floor readFloor(BufferedReader br) throws IOException {

        Floor floor;
        br.readLine();
        List<String> strings = new LinkedList<String>();
        for (int i = 0; i < 4; i++) {
            strings.add(br.readLine());
        }

        floor = new Floor(strings);
        return floor;
    }

    public int getUserOnFloorPosition(){

        Position position = new Position().calcPosition("TORSO");
        int line = position.getLine();
        int column = position.getColumn();
        float distance = position.getDist();

        if(distance < EPSILON || column >= widthParts || line >= heigthParts){
            position = new Position().calcPosition("LEFT_FOOT");
            line = position.getLine();
            column = position.getColumn();

        }

        if(distance < EPSILON || column >= widthParts || line >= heigthParts){
            position = new Position().calcPosition("RIGHT_FOOT");
            line = position.getLine();
            column = position.getColumn();

        }

        if(column > (widthParts - 1))
            column = widthParts - 1;
        if(line > (heigthParts - 1))
            line = heigthParts - 1;

        return line*widthParts+column;

    }

    public User getUser() {
        return user;
    }

    public Floor getFloor() {
        return floor;
    }

    public int getWidthParts() {
        return widthParts;
    }

    public int getHeigthParts() {
        return heigthParts;
    }

    @Override
    public String toString() {
        return "Snapshot{" +
                "user=" + user +
                ", floor=" + floor +
                '}';
    }

    public class Position {
        private int line;
        private int column;
        private float distance;

        public float getDist() {
			return distance;
		}


		public int getLine() {
            return line;
        }

        public int getColumn() {
            return column;
        }

        public Position calcPosition(String skelPoint) {
            Point3f p1 , p2 , p3 ;
            Point3f floorNormal;
            Point3f projectionPoint;
            Point3f projection1, projection2;
            float lat, lung, dist, chunkLat,chunkLung;

            p1 = floor.getLB();
            p2 = floor.getRB();
            p3 = floor.getRF();


            lat = p2.distance(p3);
            lung = p1.distance(p2);
            chunkLat = lat/heigthParts;
            chunkLung = lung/widthParts;


            floorNormal = Geometry.planNormal(p1,p2,p3);
            projectionPoint =  Geometry.projectPointOnPlan(floorNormal,p1,user.getSkeletonElement(skelPoint));
            projection1 = Geometry.projectPointOnLine(projectionPoint,p1,p2);
            projection2 = Geometry.projectPointOnLine(projectionPoint,p2,p3);


            dist = projection1.distance(p1);
            column = (int) (dist/chunkLung);
            this.distance = dist - column*chunkLung;

            dist = projection2.distance(p2);
            line = (int) (dist/chunkLat);
            this.distance = Math.min(this.distance, dist - line*chunkLat);


            return this;
        }
    }
}
