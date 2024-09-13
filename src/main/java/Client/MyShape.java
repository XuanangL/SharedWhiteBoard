/* Author: Li xuanang
    StudentID: 1218931
    Date: 2024/05/02
 */
package Client;
import java.awt.*;
import java.io.Serializable;

public abstract class MyShape implements Serializable {
    Color color;
    public abstract void draw(Graphics g);

}
