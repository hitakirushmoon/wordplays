package stuffs;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.text.Normalizer;
import java.util.*;

import static java.text.Normalizer.Form;
import static java.text.Normalizer.normalize;

public class WordUtils {
    static final Comparator<String> string_by_length_then_lexicon = Comparator.comparingInt((String a) -> -Normalizer.normalize(a, Form.NFC).length()).thenComparing(a -> a);

    static final TreeMap<String, Short> all_prefixes = new TreeMap<>(string_by_length_then_lexicon);
    static final TreeMap<String, Short> all_suffixes = new TreeMap<>(string_by_length_then_lexicon);
    static final HashMap<String, Short> all_accents = new HashMap<>();

    static final char[] ACCENT = {769, 768, 803, 777, 771};
    static final String[] all_syllables;
    static final int count[] = new int[3];
    static final int[] primes = primes(200);
//    static int[][] precomputed;

    static {
        count[2] = 1;
        all_accents.put("", (short) 0);
        for (char c : ACCENT) {
            all_accents.put(Character.toString(c), (short) count[2]++);
        }

        Scanner in = null;
        try {
            in = new Scanner(new BufferedReader(new FileReader("full_prefixes.txt", StandardCharsets.UTF_8)));
        } catch (IOException e) {
            e.printStackTrace();
        }
        assert in != null;

        count[0] = 0;
        while (in.hasNext()) {
            String line = in.nextLine();
            all_prefixes.put(line, (short) count[0]++);
        }
        in.close();


        try {
            in = new Scanner(new BufferedReader(new FileReader("full_suffixes.txt", StandardCharsets.UTF_8)));
        } catch (IOException e) {
            e.printStackTrace();
        }

        count[1] = 0;
        while (in.hasNext()) {
            String line = in.nextLine();
            all_suffixes.put(line, (short) count[1]++);
        }
        in.close();

        all_syllables = new String[count[0] * count[1] * count[2]];

        System.out.println(all_syllables.length);
        System.out.println(Arrays.toString(count));
//        precomputed = new int[180][8];
    }

    public static String[] breakdown_syllable(String syll) {
        String[] answer = new String[3];
        syll = normalize(syll.toLowerCase(), Form.NFD);
        int accent_index = locateAccent(syll);
        if (accent_index != -1) {
            answer[2] = syll.substring(accent_index, accent_index + 1);
            syll = removeChar(syll, accent_index);
        } else {
            answer[2] = "";
        }
        for (String prefix : all_prefixes.keySet()) {
            if (syll.startsWith(prefix)) {
                syll = syll.substring(prefix.length());
                answer[0] = prefix;
                if (prefix.equals("gi")) {
                    if (syll.equals("a")) {
                        answer[1] = "a";
                        return answer;
                    }
                    syll = "i" + syll;
                    if (matchSuffix(syll, answer)) return answer;
                    syll = syll.substring(1);
                }
                if (prefix.equals("qu")) {
                    if (syll.startsWith("ô")) {
                        answer[1] = "u" + syll;
                        answer[0] = "q";
                        return answer;
                    }
                    if (matchSuffix(syll, answer)) return answer;
                    answer[0] = "q";
                    syll = "u" + syll;
                }
                if (matchSuffix(syll, answer)) {
                    return answer;
                } else {
                    return null;
                }
            }
        }
        return null;
    }

    private static boolean matchSuffix(String syll, String[] answer) {
        for (String suffix : all_suffixes.keySet()) {
            if (syll.equals(suffix)) {
                answer[1] = suffix;
                return true;
            }
        }
        return false;
    }

    public static int spelling_hash(String prefix, String suffix, String accent) {
        return (all_accents.get(accent) * count[0] + all_prefixes.get(prefix)) * count[1] + all_suffixes.get(suffix);
    }

    public static int phonetic_hash(String prefix, String suffix, String accent) {
        return primes[all_prefixes.get(phonetic_prefix(prefix))] * primes[all_suffixes.get(phonetic_suffix(suffix)) + count[0]] * primes[all_accents.get(phonetic_accent(accent, suffix)) + count[0] + count[1]];
    }

    public static String phonetic_prefix(String prefix) {
        if (prefix.equals("tr"))
            return "ch";
        if (prefix.equals("gi") || prefix.equals("r"))
            return "d";
        if (prefix.equals("k") || prefix.equals("q"))
            return "c";
        if (prefix.equals("ngh") || prefix.equals("gh"))
            return prefix.replace("h", "");
        if (prefix.equals("x"))
            return "s";
        return prefix;
    }

    public static String phonetic_suffix(String suffix) {
        if (suffix.startsWith("y"))
            return "i" + suffix.substring(1);
        return suffix;
    }

    public static String phonetic_accent(String accent, String suffix) {
        if (accent.isEmpty() && (suffix.endsWith("t") || suffix.endsWith("c") || suffix.endsWith("ch") || suffix.endsWith("p"))) {
            return "" + ACCENT[0];
        }
        return accent;
    }

    static int locateAccent(String s) {
        for (char c : ACCENT) {
            int index = s.indexOf(c);
            if (index != -1) {
                return index;
            }
        }
        return -1;
    }

    static String removeChar(String s, int index) {
        return s.substring(0, index) + s.substring(index + 1);
    }

    public static int[] primes(int n) {
        int[] primes = new int[n];
        primes[0] = 1;
        int count = 1;
        int num = primes[0];
        outer_loop:
        while (count < primes.length) {
            num++;
            for (int i = 1; i < count; i++) {
                if (num % primes[i] == 0) {
                    continue outer_loop;
                }
            }
            primes[count] = num;
            count++;
        }
        return primes;
    }
}
