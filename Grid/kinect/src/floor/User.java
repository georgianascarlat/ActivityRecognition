package floor;
import javax.vecmath.Point3f;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created with IntelliJ IDEA.
 * User: gia
 * Date: 3/3/13
 * Time: 1:07 PM
 * To change this template use File | Settings | File Templates.
 */
public class User {

    private Map<String,Point3f> skeleton;

    @Override
    public String toString() {
        return "User{" +
                "skeleton=" + skeleton +
                '}';
    }

    public User(List<String> points){

        skeleton = new HashMap<String, Point3f>();
        int n = points.size();
        String[] tokens;
        float x,y,z;

        for(int i=0;i<n;i++){
            tokens = points.get(i).split("\\s+");
            if(tokens.length != 4){
                System.err.println("Invalid input entry "+points.get(i));
            } else{
                x =   Float.parseFloat(tokens[1]);
                y =   Float.parseFloat(tokens[2]);
                z =   Float.parseFloat(tokens[3]);
                skeleton.put(tokens[0],new Point3f(x,y,z));
            }
        }

    }

    public Map<String, Point3f> getSkeleton() {
        return skeleton;
    }

    public Point3f getSkeletonElement(String name){
        return skeleton.get(name);
    }
}
