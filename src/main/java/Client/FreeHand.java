/* Author: Li xuanang
    StudentID: 1218931
    Date: 2024/05/02
 */
package Client;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;
public class FreeHand extends MyShape {
    private List<Point> points = new ArrayList<>();
    private int strokeSize = 1;       // Default stroke size

    public FreeHand(Color color, int strokeSize) {
        this.color = color;
        this.strokeSize = strokeSize;
    }

    public void addPoint(int x, int y) {
        points.add(new Point(x, y));
    }

    @Override
    public void draw(Graphics g) {
        g.setColor(color);
        Graphics2D g2d = (Graphics2D) g;
        g2d.setStroke(new BasicStroke(strokeSize, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));

        for (int i = 1; i < points.size(); i++) {
            int x1 = points.get(i - 1).x;
            int y1 = points.get(i - 1).y;
            int x2 = points.get(i).x;
            int y2 = points.get(i).y;
            g2d.drawLine(x1, y1, x2, y2);
        }
        // set the stroke back to default size because previous stroke may set to big earser size
        g2d.setStroke(new BasicStroke());
    }
}