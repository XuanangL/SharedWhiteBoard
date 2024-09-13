/* Author: Li xuanang
    StudentID: 1218931
    Date: 2024/05/02
 */
package Client;

import java.awt.*;

public class Circle extends MyShape {
    private Point start;
    private int radius;

    public Circle(Color color, Point start, int radius) {
        this.color = color;
        this.start = start;
        this.radius = radius;
    }

    public void setRadius(int radius) {
        this.radius = radius;
    }


    @Override
    public void draw(Graphics g) {
        g.setColor(color);
        g.drawOval(start.x - radius, start.y - radius, 2 * radius, 2 * radius);
    }
}