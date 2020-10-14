package stuffs;

import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Comparator;
import java.util.Scanner;
import java.util.TreeSet;

public class Filter {
    public static void main(String[] args) throws IOException {
        Scanner in = new Scanner(new FileReader("Viet39K.txt"));
        TreeSet<String> strings = new TreeSet<>(Comparator.comparingInt((String x) -> x.length() - x.replace(" ", "").length()).thenComparing(x -> x));
        loop:
        while (in.hasNext()) {
            String phrase = in.nextLine();
            if (phrase.contains("-")) {
                continue;
            }
            String[] syllables = phrase.split(" ");
            if (syllables.length == 2) {
                for (String syllable : syllables) {
                    String[] breakdown = stuffs.WordUtils.breakdown_syllable(syllable);
                    if (breakdown == null) {
                        continue loop;
                    }
                }
                strings.add(phrase);
            } else if (syllables.length == 1) {
                String[] breakdown = stuffs.WordUtils.breakdown_syllable(syllables[0]);
                if (breakdown == null) {
                    continue;
                }
                strings.add(phrase);
            }
        }
        BufferedWriter writer = new BufferedWriter(new FileWriter("filtered.txt"));
        for (String string : strings) {
            writer.write(string + System.lineSeparator());
        }
        writer.close();
    }
}
