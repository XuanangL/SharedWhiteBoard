/* Author: Li xuanang
    StudentID: 1218931
    Date: 2024/05/02
 */
package Client;
import java.awt.*;

public class Line extends MyShape {
    private Point start;
    private Point end;

    public Line(Color color, Point start, Point end) {
        this.color = color;
        this.start = start;
        this.end = end;
    }

    @Override
    public void draw(Graphics g) {
        g.setColor(color);
        g.drawLine(start.x, start.y, end.x, end.y);
    }

    public void setEnd(Point end) {
        this.end = end;
    }
}