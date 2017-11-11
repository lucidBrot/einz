package ch.ethz.inf.vs.a4.minker.einz;

/**
 * Created by Fabian on 09.11.2017.
 */

public class CardAttributeList {

    //all possible types of cards
    public static final String zero = "zero";
    public static final String one = "one";
    public static final String two = "two";
    public static final String three = "three";
    public static final String four = "four";
    public static final String five = "five";
    public static final String six = "six";
    public static final String seven = "seven";
    public static final String eight = "eight";
    public static final String nine = "nine";
    public static final String plusTwo = "plusTwo";
    public static final String switchOrder = "switchOrder";
    public static final String stop = "stop";
    public static final String changeColor = "changeColor";
    public static final String changeColorPlusFour = "changeColorPlusFour";


    //all possible colors of cards which are also
    //all possible wishes of cards (if a card isn't played but you can choose a color as soon as you play it,
    //it has "none" as wish
    public static final String yellow = "yellow";
    public static final String blue = "blue";
    public static final String red = "red";
    public static final String green = "green";
    public static final String none = "none";

    //some functions to make coding easier
    public static String intToType(int i){
        switch(i){
            case 0:
                return zero;
            case 1:
                return one;
            case 2:
                return two;
            case 3:
                return three;
            case 4:
                return four;
            case 5:
                return five;
            case 6:
                return six;
            case 7:
                return seven;
            case 8:
                return eight;
            case 9:
                return nine;
            case 10:
                return plusTwo;
            case 11:
                return switchOrder;
            case 12:
                return stop;
            case 13:
                return changeColor;
            case 14:
                return changeColorPlusFour;
            default:
                return "";
        }
    }

    public static String intToColor(int i){
        switch (i){
            case 0:
                return yellow;
            case 1:
                return blue;
            case 2:
                return red;
            case 3:
                return green;
            case 4:
                return none;
            default:
                return "";
        }
    }

}
