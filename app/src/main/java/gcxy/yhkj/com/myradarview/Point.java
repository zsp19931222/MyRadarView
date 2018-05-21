package gcxy.yhkj.com.myradarview;

/**
 * Created by Administrator on 2018/5/21 0021.
 */

public class Point {
    private float x,y;
    private float r;
    private int alpha;

    public Point(float x, float y,float r,int alpha) {
        this.x = x;
        this.y = y;
        this.r = r;
        this.alpha = alpha;
    }

    public float getX() {
        return x;
    }

    public void setX(float x) {
        this.x = x;
    }

    public float getY() {
        return y;
    }

    public void setY(float y) {
        this.y = y;
    }

    public float getR() {
        return r;
    }

    public void setR(float r) {
        this.r = r;
    }

    public int getAlpha() {
        return alpha;
    }

    public void setAlpha(int alpha) {
        this.alpha = alpha;
    }
}
