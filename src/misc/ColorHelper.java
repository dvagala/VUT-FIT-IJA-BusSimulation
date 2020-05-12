package misc;

import javafx.scene.paint.Color;

public class ColorHelper {
    public static Color getLighterColor(Color formerColor, double lightness){

        double red =  formerColor.getRed() + lightness;
        double green =  formerColor.getGreen() + lightness;
        double blue =  formerColor.getBlue() + lightness;

        if(red > 1.0){
            red = 1.0;
        }

        if(green > 1.0){
            green = 1.0;
        }

        if(blue > 1.0){
            blue = 1.0;
        }

        return new Color(red, green, blue, 1);
    }
}
