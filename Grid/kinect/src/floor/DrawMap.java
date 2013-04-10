package floor;
import java.applet.Applet;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: gia
 * Date: 3/3/13
 * Time: 1:57 PM
 * To change this template use File | Settings | File Templates.
 */
public class DrawMap extends Applet implements Runnable {

    private Map map;
    private List<Snapshot> snapshotList = new ArrayList<Snapshot>();
    private String dataFolder = "/home/gia/An4/Licenta/kinect/data/recorded/points";
    private static final int widthParts = 10, heightParts = 6;
    private int count = 0, maxCount;

    private Image bi;
    private Graphics2D big;
    private Thread animatie;

    @Override
    public void init() {
        super.init();
        map = new Map(getSize().width, getSize().height, widthParts, heightParts);

        File dataDirectory = new File(dataFolder);

        String fileName;


        List<Integer> indexes = getFileIndexes(dataDirectory);


        for (Integer index : indexes) {

            fileName = "skel" + index;

            try {
                snapshotList.add(new Snapshot(dataFolder+"/"+fileName, widthParts, heightParts));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }


        maxCount = snapshotList.size();


        //double buffering
        bi = (BufferedImage) createImage(map.getWidth(), map.getHeight());
        big = (Graphics2D) bi.getGraphics();

        //initializare thread
        animatie = new Thread(this);
        //pornire thread
        animatie.start();
    }




    private List<Integer> getFileIndexes(File dataDirectory) {
        List<Integer> indexes = new ArrayList<Integer>();
        for (File file : dataDirectory.listFiles()) {
            if (file.getName().startsWith("skel"))
                indexes.add(Integer.parseInt(file.getName().substring(4)));
        }
        Collections.sort(indexes);
        return indexes;
    }


    public void paint(Graphics g) {

        Graphics2D gg;
        gg = (Graphics2D) g;
        int x, y, highLight;

        highLight = snapshotList.get(count).getUserOnFloorPosition();
        System.out.println("Count: " + (count + 5) + " Highlight: " + highLight);


        big.setColor(Color.white);

        big.clearRect(0, 0, map.getWidth(), map.getHeight());

        big.setBackground(Color.black);


        for (int line = 0; line < heightParts; line++) {
            for (int column = 0; column < widthParts; column++) {

                x = column * map.getWidthChunk();
                y = line * map.getHeightChunk();


                if ((line * map.getWidthParts() + column) == highLight) {
                    big.setColor(Color.green);
                    big.fillRect(x, y, map.getWidthChunk(), map.getHeightChunk());
                } else {
                    big.setColor(Color.blue);
                    big.drawRect(x, y, map.getWidthChunk(), map.getHeightChunk());

                }

            }
        }


        gg.drawImage(bi, 0, 0, this);

    }


    @Override
    public void run() {
        while (true) {
            try {
                Thread.sleep(1000);


                if (count < maxCount - 1)
                    count++;

                repaint();
            } catch (Exception e) {
            }
        }


    }


}
