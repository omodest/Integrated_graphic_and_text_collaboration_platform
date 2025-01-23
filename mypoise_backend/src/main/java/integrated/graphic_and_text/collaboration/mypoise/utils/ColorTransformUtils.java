package integrated.graphic_and_text.collaboration.mypoise.utils;

/**
 * 主色调转换工具类
 */
public class ColorTransformUtils {

    public static String getStandardColor(String color){
        // 十六进制主色调，中间如果连续出现两个零，会被识别成一个0
        if (color.length() == 7){
            color = color.substring(0, 4) + "0" + color.substring(4, 7);
        }
        return color;
    }
}
