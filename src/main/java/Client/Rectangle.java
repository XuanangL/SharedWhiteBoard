
/* Author: Li xuanang
    StudentID: 1218931
    Date: 2024/05/02
 */
package Client;
import java.awt.*;

class Rectangle extends MyShape {
    private Point start;
    private int width, height;

    public Rectangle(Color color, Point start, int width, int height) {
        this.color = color;
        this.start = start;
        this.width = width;
        this.height = height;
    }

    public void setStart(Point start) {
        this.start = start;
    }

    public void setWidth(int width) {
        this.width = width;
    }

    public void setHeight(int height) {
        this.height = height;
    }

    @Override
    public void draw(Graphics g) {
        g.setColor(color);
        g.drawRect(start.x, start.y, width, height);
    }

}
