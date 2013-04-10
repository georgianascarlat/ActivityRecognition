package floor;
/**
 * Created with IntelliJ IDEA.
 * User: gia
 * Date: 3/3/13
 * Time: 1:56 PM
 * To change this template use File | Settings | File Templates.
 */
public class Map {

    private int width , height;
    private int widthParts, heightParts;
    private int widthChunk,heightChunk;
    private int highLighted;

    public Map(int width, int height, int widthParts, int heightParts) {
        this.width = width;
        this.height = height;
        this.widthParts = widthParts;
        this.heightParts = heightParts;
        this.widthChunk = width/widthParts;
        this.heightChunk = height/heightParts;
    }


    public int getWidthParts() {
        return widthParts;
    }

    public int getHeightParts() {
        return heightParts;
    }

    public int getWidthChunk() {
        return widthChunk;
    }

    public int getHeightChunk() {
        return heightChunk;
    }

    public int getHighLighted() {
        return highLighted;
    }

    public void setHighLighted(int highLighted) {
        this.highLighted = highLighted;
    }

    public int getWidth() {
        return width;
    }

    public void setWidth(int width) {
        this.width = width;
    }

    public int getHeight() {
        return height;
    }

    public void setHeight(int height) {
        this.height = height;
    }
}
