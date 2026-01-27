package org.revhire.util;

import java.util.List;

// Utility for formatting console output into tables
public class TableFormatter {

    public static void printTable(String title, String[] headers, List<String[]> rows) {
        if (rows == null || rows.isEmpty()) {
            System.out.println("\n[ " + title + " ] - No data available.");
            return;
        }

        System.out.println("\n--- " + title + " ---");

        // Calculate column widths
        int[] widths = new int[headers.length];
        for (int i = 0; i < headers.length; i++) {
            widths[i] = headers[i].length();
        }

        for (String[] row : rows) {
            for (int i = 0; i < row.length; i++) {
                if (row[i] != null && row[i].length() > widths[i]) {
                    widths[i] = row[i].length();
                }
            }
        }

        // Print header
        printRow(headers, widths);
        printSeparator(widths);

        // Print rows
        for (String[] row : rows) {
            printRow(row, widths);
        }
        System.out.println("---------------------------------");
    }

    private static void printRow(String[] columns, int[] widths) {
        StringBuilder sb = new StringBuilder("|");
        for (int i = 0; i < columns.length; i++) {
            String format = " %-" + widths[i] + "s |";
            sb.append(String.format(format, columns[i] != null ? columns[i] : ""));
        }
        System.out.println(sb.toString());
    }

    private static void printSeparator(int[] widths) {
        StringBuilder sb = new StringBuilder("+");
        for (int width : widths) {
            for (int i = 0; i < width + 2; i++) {
                sb.append("-");
            }
            sb.append("+");
        }
        System.out.println(sb.toString());
    }
}
