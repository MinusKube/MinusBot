package fr.minuskube.bot.discord.util;

import java.util.Arrays;

public class TabbedList {

    private static final int DEFAULT_DISTANCE = 3;

    private final String[][] items;
    private int[] minSpaces;

    private final int distance;
    private int lines;

    public TabbedList(String[][] items, int distance) {
        this.items = items;
        this.distance = distance;

        for(String[] column : items)
            lines = Math.max(lines, column.length);
    }

    public TabbedList(String[]... items) { this(items, DEFAULT_DISTANCE); }

    private void calculateMinSpaces() {
        if(items.length < 2)
            return;

        minSpaces = new int[items.length - 1];
        Arrays.fill(minSpaces, distance);

        for(int i = 0; i < minSpaces.length; i++) {
            for(String item : items[i])
                minSpaces[i] = Math.max(minSpaces[i], item.length() + distance);
        }
    }

    public String[][] getItems() { return items; }
    public String[] getItems(int column) { return items[column]; }

    @Override
    public String toString() {
        calculateMinSpaces();

        StringBuilder sb = new StringBuilder();

        for(int row = 0; row < lines; row++) {
            for(int column = 0; column < items.length; column++) {
                String item = items[column][row];

                sb.append(item);

                if(column < items.length - 1)
                    for(int i = 0; i < minSpaces[column] - item.length(); i++)
                        sb.append(" ");
            }

            if(row < lines - 1)
                sb.append("\n");
        }

        return sb.toString();
    }

}
