package stuffs;

import it.unimi.dsi.fastutil.ints.Int2ObjectAVLTreeMap;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntArraySet;
import it.unimi.dsi.fastutil.ints.IntIterator;
import it.unimi.dsi.fastutil.longs.*;

import java.io.*;
import java.text.Normalizer;
import java.util.Arrays;
import java.util.Locale;
import java.util.Scanner;

import static stuffs.WordUtils.*;

public class Main {
    public static void main(String[] args) throws IOException {
        Scanner in = new Scanner(new FileReader("Viet39K.txt"));
        Long2ObjectOpenHashMap<IntArraySet> hash_map = new Long2ObjectOpenHashMap<>();
        Int2ObjectAVLTreeMap<IntArraySet> set = new Int2ObjectAVLTreeMap<>();
        loop:
        while (in.hasNext()) {
            String phrase = in.nextLine();
            if (phrase.contains("-")) {
                continue;
            }
            String[] syllables = phrase.split(" ");
            long hash = 1;
            int shash = 0;
            if (syllables.length == 2) {
                for (String syllable : syllables) {
                    String[] breakdown = stuffs.WordUtils.breakdown_syllable(syllable);
                    if (breakdown == null) {
                        continue loop;
                    }
                    int sh = spelling_hash(breakdown[0], breakdown[1], breakdown[2]);
                    all_syllables[sh] = Normalizer.normalize(syllable, Normalizer.Form.NFC).toLowerCase();
                    shash *= (all_syllables.length + 1);
                    shash += sh + 1;
                    hash *= phonetic_hash(breakdown[0], breakdown[1], breakdown[2]);
                }
                hash_map.computeIfAbsent(hash, x -> new IntArraySet()).add(-shash);
            } else if (syllables.length == 1) {
                String[] breakdown = stuffs.WordUtils.breakdown_syllable(syllables[0]);
                if (breakdown == null) {
                    continue;
                }
                shash = spelling_hash(breakdown[0], breakdown[1], breakdown[2]);
                all_syllables[shash] = Normalizer.normalize(syllables[0], Normalizer.Form.NFC).toLowerCase();
                hash = phonetic_hash(breakdown[0], breakdown[1], breakdown[2]);
                set.computeIfAbsent((int) hash, x -> new IntArraySet()).add(-shash - 1);
            }
        }
        IntArrayList list = new IntArrayList(set.keySet());
        for (int i = 0; i < list.size(); i++) {
            for (int j = i + 1; j < list.size(); j++) {
                IntArraySet a = hash_map.computeIfAbsent((long) list.getInt(i) * list.getInt(j), x -> new IntArraySet());
                a.add(list.getInt(i));
                a.add(list.getInt(j));
            }
        }
        hash_map.long2ObjectEntrySet().removeIf(x -> x.getValue().size() < 2 || (x.getValue().size() < 3 && x.getValue().toIntArray()[0] > 0));
        Long2ObjectAVLTreeMap<IntArraySet> tree_map = new Long2ObjectAVLTreeMap<>(hash_map);
        System.out.println(tree_map.size());
        System.out.println("done!");
        BufferedWriter writer = new BufferedWriter(new FileWriter("spoonerisms.txt"));
        for (Long2ObjectMap.Entry<IntArraySet> intArraySetEntry : tree_map.long2ObjectEntrySet()) {
            boolean passed = true;
            if (intArraySetEntry.getValue().size() == 3) {
                int word = 0;
                for (int integer : intArraySetEntry.getValue()) {
                    if (integer < 0) {
                        word = integer;
                        continue;
                    }
                    passed = false;
                    if (set.get(integer).size() > 1) {
                        passed = true;
                        break;
                    }
                }
                if (!passed) {
                    passed = intArraySetEntry.getValue().contains((-word) / (all_syllables.length + 1));
                }
            }
            if (passed) {
                writer.write(toString(intArraySetEntry.getValue(), set) + System.lineSeparator());
            }
//            else{
//                System.out.println(toString(intArraySetEntry.getValue(), set));
//            }
        }
        writer.close();
    }

    static String toString(IntArraySet phrases, Int2ObjectAVLTreeMap<IntArraySet> set) {
        StringBuilder s = new StringBuilder();
        IntIterator iterator = phrases.iterator();
        while (iterator.hasNext()) {
            int x = iterator.nextInt();
            if (x < 0) {
                int first_syll = (-x) / (all_syllables.length + 1) - 1;
                int sec_syll = (-x) % (all_syllables.length + 1) - 1;
                if (first_syll == -1) {
                    s.append(" ").append(all_syllables[sec_syll]).append("/");
                } else {
                    s.append(" ").append(all_syllables[first_syll]).append(" ").append(all_syllables[sec_syll]).append(" =");
                }
            } else {
                int y = iterator.nextInt();
                s.append(toString((set.get(x)), set)).append("+").append(toString((set.get(y)), set)).append("=");
            }
        }
        s.delete(s.length() - 1, s.length());
//        s.append("}");

        return s.toString();
    }
}
