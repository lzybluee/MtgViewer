package lu.cifer.mtgviewer;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CardAnalyzer {

    public static String[] LegalList = {"Block", "Modern", "Legacy", "Vintage", "Commander"};
    public static String[] TypeList = {"Artifact", "Creature", "Enchantment",
            "Instant", "Land", "Planeswalker", "Sorcery", "Tribal"};
    public static String[] SpecialList = {"Plane", "Phenomenon", "Scheme",
            "Ongoing Scheme", "Conspiracy"};

    static String last_name = "";
    static ReprintInfo last_card = null;
    static int same_count = 0;
    static Hashtable<String, CardInfo> cardDatabase = new Hashtable<>();
    static int reprintCards = 0;
    static String[] allName;
    static int[] landIndex = new int[5];
    static HashMap<String, Integer> cardNameInSet = new HashMap<>();

    public static String getEntry(String str, String tag) {
        Pattern pattern = Pattern.compile("<" + tag + ">(.+?)</" + tag + ">",
                Pattern.DOTALL);
        Matcher matcher = pattern.matcher(str);
        if (matcher.find()) {
            return matcher.group(1);
        }
        return null;
    }

    private static boolean nearlyEquals(String str1, String str2) {
        String s1 = str1.toLowerCase().replaceAll("[^a-z0-9]", "");
        String s2 = str2.toLowerCase().replaceAll("[^a-z0-9]", "");

        if (s1.equals(s2)) {
            return true;
        }

        if (s2.length() < s1.length()) {
            String s = s1;
            s1 = s2;
            s2 = s;
        }

        int[] array = new int[s1.length()];
        int count = 0, sum = 0;
        for (int i = 0; i < s1.length(); i++) {
            int n;
            array[i] = -1;
            if ((n = s2.indexOf(s1.charAt(i))) >= 0) {
                s2 = s2.substring(0, n) + s2.substring(n + 1);
                array[i] = n;
                sum += n;
                count++;
            }
        }

        float avg = (float) sum / (float) count;
        float power = 0.0f;
        for (int i = 0; i < s1.length(); i++) {
            if (array[i] >= 0) {
                power += (array[i] - avg) * (array[i] - avg);
            }
        }

        return s2.length() <= Math.max(8, str2.length() / 8)
                && Math.sqrt(power / count) <= 8;
    }

    private static String getFormatedNumber(String name, int length) {
        char c = name.charAt(name.length() - 1);
        if (c >= '0' && c <= '9') {
            while (name.length() < length) {
                name = "0" + name;
            }
        } else {
            while (name.length() < length + 1) {
                name = "0" + name;
            }
        }
        return name;
    }

    public static void initData() {

        for (String[] s : CardParser.SetList) {
            landIndex = new int[5];
            cardNameInSet = new HashMap<>();
            for (int i = 2; i < s.length; i++) {
                processSet(new File(MainActivity.SDPath + "/MTG/Oracle/MtgOracle_" + s[i] + ".txt"));
            }
            break; // test
        }

        allName = new String[cardDatabase.size()];

        System.out.println(CardParser.SetList.length + " Sets and " + allName.length
                + " Cards" + " (" + reprintCards + " Reprints)");

        Enumeration<String> keys = cardDatabase.keys();
        int count = 0;
        while (keys.hasMoreElements()) {
            CardInfo info = cardDatabase.get(keys.nextElement());
            allName[count] = info.name;
            count++;
        }

        Arrays.sort(allName);

        int AtherPos = 0;
        for (int i = allName.length - 1; i >= 0; i--) {
            if (allName[i].startsWith("Z")) {
                AtherPos = i + 1;
                break;
            }
        }

        String[] temp = new String[allName.length];
        for (int i = 0; i < temp.length; i++) {
            if (i < temp.length - AtherPos) {
                temp[i] = allName[AtherPos + i];
            } else {
                temp[i] = allName[i - temp.length + AtherPos];
            }
        }
        allName = temp;
    }

    public static CardInfo getNewCard(String str) {
        String entry;

        CardInfo card = new CardInfo();
        CardInfo otherCard;

        card.name = getEntry(str, "Name");

        entry = getEntry(str, "OtherPart");
        if (entry != null) {
            card.otherPart = entry;
            String num = getEntry(str, "No");
            if (num.charAt(num.length() - 1) >= 'a') {
                card.partIndex = num.charAt(num.length() - 1) - 'a' + 1;
            }
            if (card.name.contains("(")) {
                card.isSplit = true;
            } else if (card.partIndex == 2 && getEntry(str, "ManaCost") == null) {
                card.isDoubleFaced = true;
                otherCard = cardDatabase.get(entry);
                otherCard.isDoubleFaced = true;
            } else if (card.partIndex == 2) {
                card.isFlip = true;
                otherCard = cardDatabase.get(entry);
                otherCard.isFlip = true;
            }
        }

        entry = getEntry(str, "Type");

        Pattern pattern = Pattern.compile(" ([^ ]+)/([^ ]+)");
        Matcher matcher = pattern.matcher(entry);

        if (matcher.find()) {
            card.power = matcher.group(1);
            card.toughness = matcher.group(2);
            entry = entry.substring(0, entry.indexOf(card.power));
        }

        pattern = Pattern.compile("\\(Loyalty: ([^)]+)\\)");
        matcher = pattern.matcher(entry);

        if (matcher.find()) {
            card.loyalty = matcher.group(1);
            entry = entry.substring(0, entry.indexOf(matcher.group(0)));
        }

        pattern = Pattern.compile("([^—]+)(—(.+))?");
        matcher = pattern.matcher(entry);

        String types = null;
        String subtypes = null;

        if (matcher.find()) {
            types = matcher.group(1);
            subtypes = matcher.group(3);
        }

        card.types = new Vector<>();
        card.subTypes = new Vector<>();
        card.superTypes = new Vector<>();

        if (types != null) {
            for (String s : types.trim().split(" ")) {
                boolean flag = false;
                for (String t : TypeList) {
                    if (s.equals(t)) {
                        card.types.add(s);
                        flag = true;
                        break;
                    }
                }
                if (!flag) {
                    card.superTypes.add(s);
                }
            }
        }

        if (subtypes != null) {
            card.subTypes.addAll(Arrays.asList(subtypes.trim().split(" ")));
        }

        entry = getEntry(str, "ManaCost");
        if (card.isFlip && card.partIndex == 2) {
            if (entry != null) {
                if (entry.contains("W")) {
                    card.colorIndicator = "White";
                } else if (entry.contains("U")) {
                    card.colorIndicator = "Blue";
                } else if (entry.contains("B")) {
                    card.colorIndicator = "Black";
                } else if (entry.contains("R")) {
                    card.colorIndicator = "Red";
                } else if (entry.contains("G")) {
                    card.colorIndicator = "Green";
                }
            }
        } else {
            if (entry != null) {
                pattern = Pattern.compile("([^( ]+)( \\(([^)]+)\\))?");
                matcher = pattern.matcher(entry);

                if (matcher.find()) {
                    card.mana = matcher.group(1);
                    if (matcher.group(3) != null) {
                        card.converted = Integer.parseInt(matcher.group(3));
                    }
                }
            }
            card.colorIndicator = getEntry(str, "ColorIndicator");
        }

        card.text = getEntry(str, "CardText");

        card.rules = getEntry(str, "Rulings");

        card.legal = new Vector<>();
        card.restricted = new Vector<>();
        card.banned = new Vector<>();

        for (String s : LegalList) {
            entry = getEntry(str, s);
            if (entry != null) {
                switch (entry) {
                    case "Legal":
                        card.legal.add(s);
                        break;
                    case "Restricted":
                        card.restricted.add(s);
                        break;
                    case "Banned":
                        card.banned.add(s);
                        break;
                }
            }
        }

        card.reserved = (getEntry(str, "Reserved") != null);

        return card;
    }

    public static ReprintInfo addReprintCard(String str, CardInfo card) {

        reprintCards++;

        ReprintInfo reprint = new ReprintInfo();
        reprint.set = getEntry(str, "Set");
        reprint.number = getEntry(str, "No");
        reprint.flavor = getEntry(str, "Flavor");
        reprint.artist = getEntry(str, "Artist");
        reprint.rarity = getEntry(str, "Rarity");
        reprint.multiverseid = Integer.parseInt(getEntry(str, "Multiverseid"));
        reprint.watermark = getEntry(str, "Watermark");
        reprint.rating = Float.parseFloat(getEntry(str, "Rating"));
        reprint.votes = Integer.parseInt(getEntry(str, "Votes"));

        for (String[] strs : CardParser.SetList) {
            if (reprint.set.equals(strs[0])) {
                reprint.code = strs[1].substring(strs[1].lastIndexOf("/") + 1);
                reprint.folder = strs[1];
                reprint.altCode = strs[2];
                break;
            }
        }
        if (card.superTypes.size() > 0) {
            for (String special : SpecialList) {
                for (String types : card.superTypes) {
                    if (types.equals(special)) {
                        reprint.specialType = special;
                        break;
                    }
                }
            }
        }
        if (!card.isInCore) {
            for (String[] s : CardParser.SetList) {
                if (reprint.set.equals(s[0])) {
                    card.isInCore = true;
                    break;
                }
                if (s[0].equals("Limited Edition Alpha")) {
                    break;
                }
            }
        }
        if (reprint.multiverseid == 0) {
            String entry;
            if (card.name.contains("Who/What/When/Where/Why")) {
                entry = "Who (Who/What/When/Where/Why)";
            } else {
                entry = getEntry(str, "OtherPart");
            }
            if (entry != null) {
                Vector<ReprintInfo> vector = cardDatabase.get(entry).reprints;
                for (ReprintInfo info : vector) {
                    if (reprint.set.equals(info.set)) {
                        reprint.multiverseid = info.multiverseid;
                        break;
                    }
                }
            }
        }
        if (!card.rarityChanged) {
            Vector<ReprintInfo> vector = card.reprints;
            for (ReprintInfo info : vector) {
                if (!info.rarity.equals(reprint.rarity)) {
                    card.rarityChanged = true;
                    break;
                }
            }
        }
        if (reprint.set.equals("Unhinged") || reprint.set.equals("Unglued")) {
            if (!card.name.equals("Plains") && !card.name.equals("Island")
                    && !card.name.equals("Swamp")
                    && !card.name.equals("Mountain")
                    && !card.name.equals("Forest")) {
                card.isFun = true;
            }
        }

        if (reprint.specialType == null || reprint.specialType.equals("Conspiracy")) {
            reprint.formatedNumber = getFormatedNumber(reprint.number, 3);
        } else {
            reprint.formatedNumber = getFormatedNumber(reprint.number, 2);
        }

        for (String[] strs : CardParser.SetList) {
            if (reprint.set.equals(strs[0])) {
                reprint.picture = strs[1] + "/" + reprint.formatedNumber + ".jpg";
                break;
            }
        }

        reprint.card = card;
        card.reprints.add(reprint);

        return reprint;
    }

    public static void processCard(String str) {
        String name = getEntry(str, "Name");

        if (name.equals(last_name) && same_count == 0) {
            last_card.sameIndex = 1;
        }

        if (cardDatabase.containsKey(name)) {
            last_card = addReprintCard(str, cardDatabase.get(name));
        } else {
            CardInfo card = getNewCard(getEntry(str, "Card"));
            cardDatabase.put(name, card);
            card.reprints = new Vector<>();
            last_card = addReprintCard(str, card);
        }

        if (name.equals(last_name)) {
            same_count++;
            last_card.sameIndex = same_count + 1;
        } else {
            same_count = 0;
        }
        last_name = name;
    }

    public static void processSet(File file) {
        BufferedReader reader;
        try {
            reader = new BufferedReader(new FileReader(file));
            String str;
            String card = "";
            while ((str = reader.readLine()) != null) {
                if (str.isEmpty()) {
                    processCard(card);
                    card = "";
                } else {
                    card += str + "\n";
                }
            }
            reader.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static int checkCard(CardInfo card, ReprintInfo reprint, String script) {
        return LuaScript.checkCard(card, reprint, script);
    }

    public static String[] searchCard(String script) {
        if (allName == null) {
            initData();
        }

        Vector<ReprintInfo> cards = new Vector<>();
        for (String name : allName) {
            CardInfo card = cardDatabase.get(name);
            for (ReprintInfo reprint : card.reprints) {
                int result = checkCard(card, reprint, script);
                if (result == 1) {
                    cards.add(reprint);
                } else if(result == 2) {
                    return null;
                }
            }
        }

        Collections.sort(cards, new Comparator<ReprintInfo>() {
            @Override
            public int compare(ReprintInfo left, ReprintInfo right) {
                if (left.set.equals(right.set)) {
                    return left.formatedNumber.compareTo(right.formatedNumber);
                }
                return left.set.compareTo(right.set);
            }
        });

        String[] ret = new String[cards.size()];
        for (int i = 0; i < ret.length; i++) {
            ret[i] = cards.get(i).picture;
        }
        return ret;
    }

    public static class ReprintInfo {

        public CardInfo card;

        public int multiverseid;
        public float rating;
        public int votes;
        public String set;
        public String code;
        public String folder;
        public String altCode;
        public String number;
        public String flavor;
        public String artist;
        public String rarity;
        public String watermark;
        public String specialType;

        public String picture;
        public int sameIndex;
        public String formatedNumber;

        public String toString() {
            return multiverseid + " " + set
                    + (specialType == null ? "" : " [" + specialType + "]")
                    + " : " + number + " (" + rarity + ") " + artist + " (" + rating + "|" + votes + ") " + "\n";
        }
    }

    public static class CardInfo {

        public String name;
        public String otherPart;
        public int partIndex;
        public boolean isSplit;
        public boolean isDoubleFaced;
        public boolean isFlip;
        public boolean isFun;
        public boolean isInCore;

        public Vector<String> types;
        public Vector<String> subTypes;
        public Vector<String> superTypes;

        public String mana;
        public int converted;
        public String colorIndicator;

        public String power;
        public String toughness;
        public String loyalty;

        public String text;

        public String rules;
        public Vector<String> legal;
        public Vector<String> restricted;
        public Vector<String> banned;
        public boolean reserved;

        public Vector<ReprintInfo> reprints;
        public boolean rarityChanged;

        public String toString() {
            StringBuilder str = new StringBuilder();
            str.append(name + "\n");
            if (partIndex > 0) {
                String suffix = "th";
                if (Integer.toString(partIndex).endsWith("1")) {
                    suffix = "st";
                } else if (Integer.toString(partIndex).endsWith("2")) {
                    suffix = "nd";
                } else if (Integer.toString(partIndex).endsWith("3")) {
                    suffix = "rd";
                }
                str.append(partIndex + suffix
                        + " part of the card, other part is <" + otherPart
                        + ">\n");
            }
            if (superTypes.size() > 0) {
                for (int i = 0; i < superTypes.size(); i++) {
                    str.append(superTypes.get(i));
                    if (i < superTypes.size() - 1) {
                        str.append(" ");
                    }
                }
            }
            if (types.size() > 0) {
                if (superTypes.size() > 0) {
                    str.append(" ");
                }
                for (int i = 0; i < types.size(); i++) {
                    str.append(types.get(i));
                    if (i < types.size() - 1) {
                        str.append(" ");
                    }
                }
            }
            if (subTypes.size() > 0) {
                str.append(" — ");
                for (int i = 0; i < subTypes.size(); i++) {
                    str.append(subTypes.get(i));
                    if (i < subTypes.size() - 1) {
                        str.append(" ");
                    }
                }
            }
            if (power != null) {
                str.append(" " + power);
            }
            if (toughness != null) {
                str.append("/" + toughness);
            }
            if (loyalty != null) {
                str.append(" " + "(Loyalty: " + loyalty + ")");
            }
            str.append("\n");
            if (mana != null) {
                str.append(mana + " (" + converted + ")" + "\n");
            }
            if (colorIndicator != null) {
                str.append("(Color Indicator: " + colorIndicator + ")\n");
            }
            if (text != null) {
                str.append(text + "\n");
            }
            if (rules != null) {
                str.append(rules + "\n");
            }
            if (legal.size() > 0) {
                str.append("Legal in ");
                for (int i = 0; i < legal.size(); i++) {
                    str.append(legal.get(i));
                    if (i < legal.size() - 1) {
                        str.append("/");
                    }
                }
                str.append("\n");
            }
            if (restricted.size() > 0) {
                str.append("Restricted in ");
                for (int i = 0; i < restricted.size(); i++) {
                    str.append(restricted.get(i));
                    if (i < restricted.size() - 1) {
                        str.append("/");
                    }
                }
                str.append("\n");
            }
            if (banned.size() > 0) {
                str.append("Banned in ");
                for (int i = 0; i < banned.size(); i++) {
                    str.append(banned.get(i));
                    if (i < banned.size() - 1) {
                        str.append("/");
                    }
                }
                str.append("\n");
            }
            if (reserved) {
                str.append("In RESERVED list!\n");
            }
            for (ReprintInfo info : reprints) {
                if (info.watermark != null) {
                    str.append("(Watermark: " + info.watermark + ")\n");
                    break;
                }
            }
            long[] setInfos = new long[reprints.size()];
            for (int i = 0; i < setInfos.length; i++) {
                setInfos[i] = reprints.get(i).multiverseid;
            }
            Arrays.sort(setInfos);
            for (long num : setInfos) {
                for (ReprintInfo info : reprints) {
                    if (info.set.equals("Media Inserts")) {
                        continue;
                    }
                    if (num == info.multiverseid) {
                        str.append(info);
                        break;
                    }
                }
            }
            Vector<String> flavors = new Vector<>();
            for (ReprintInfo info : reprints) {
                if (info.set.equals("Media Inserts")) {
                    continue;
                }
                if (info.flavor != null) {
                    boolean flag = false;
                    for (String s : flavors) {
                        if (nearlyEquals(s, info.flavor)) {
                            flag = true;
                            break;
                        }
                    }
                    if (!flag) {
                        flavors.add(info.flavor);
                    }
                }
            }
            for (String s : flavors) {
                str.append(s + "\n");
            }

            return str.toString();
        }
    }
}
