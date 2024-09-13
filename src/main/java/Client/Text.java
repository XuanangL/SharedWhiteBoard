/* Author: Li xuanang
    StudentID: 1218931
    Date: 2024/05/02
 */
package Client;

import java.awt.*;

class Text extends MyShape {
    String text;
    Point position;
    Font font;

    public Text(Color color, String text, Point position, Font font) {
        this.color = color;
        this.text = text;
        this.position = position;
        this.font = font;
    }

    @Override
    public void draw(Graphics g) {
        g.setColor(color);
        g.setFont(font);
        g.drawString(text, position.x, position.y);
    }
}