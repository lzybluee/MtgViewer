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

    public static String[] results;
    public static int exclude;

    static String last_name = "";
    static ReprintInfo last_card;
    static int same_count = 0;
    static Hashtable<String, CardInfo> cardDatabase = new Hashtable<>();
    static int reprintCards = 0;
    static String[] allName;
    static int[] landIndex = new int[5];
    static HashMap<String, Integer> cardNameInSet = new HashMap<>();
    static String wrongCard;
    static String filterString = "";
    static Vector<String> lastFilter;
    static Vector<String> filter = new Vector<>();
    static Vector<String> setOrder = new Vector<>();
    static int progress;
    static int foundCards;
    static boolean reverse;
    static int sortType = 0;
    static String[] sortName = new String[]{"Edition", "Name", "Cmc", "Color", "Rating", "Random"};
    static boolean stop;
    static Vector<ReprintInfo> resultCards;
    static String lastCode;
    static boolean single;

    static Comparator<ReprintInfo> editionComparator = new Comparator<ReprintInfo>() {
        @Override
        public int compare(ReprintInfo left, ReprintInfo right) {
            int ret;
            if (left.order == right.order) {
                ret = left.formatedNumber.compareTo(right.formatedNumber);
            } else {
                ret = left.order - right.order;
            }
            return reverse ? -ret : ret;
        }
    };

    static Comparator<ReprintInfo> nameComparator = new Comparator<ReprintInfo>() {
        @Override
        public int compare(ReprintInfo left, ReprintInfo right) {
            int ret;
            if (left.card.name.equals(right.card.name)) {
                if (left.order == right.order) {
                    ret = left.formatedNumber.compareTo(right.formatedNumber);
                } else {
                    ret = left.order - right.order;
                }
            } else {
                ret = left.card.name.compareTo(right.card.name);
            }
            return reverse ? -ret : ret;
        }
    };

    static Comparator<ReprintInfo> cmcComparator = new Comparator<ReprintInfo>() {
        @Override
        public int compare(ReprintInfo left, ReprintInfo right) {
            int ret;
            if (left.card.converted == right.card.converted) {
                if (left.order == right.order) {
                    ret = left.formatedNumber.compareTo(right.formatedNumber);
                } else {
                    ret = left.order - right.order;
                }
            } else {
                ret = left.card.converted - right.card.converted;
            }
            return reverse ? -ret : ret;
        }
    };

    static Comparator<ReprintInfo> ratingComparator = new Comparator<ReprintInfo>() {
        @Override
        public int compare(ReprintInfo left, ReprintInfo right) {
            float ret;
            if (left.rating == right.rating) {
                if (left.order == right.order) {
                    ret = left.formatedNumber.compareTo(right.formatedNumber);
                } else {
                    ret = left.order - right.order;
                }
            } else {
                ret = left.rating - right.rating;
            }
            if (reverse) {
                return ret > 0.0f ? 1 : -1;
            } else {
                return ret > 0.0f ? -1 : 1;
            }
        }
    };

    static Comparator<ReprintInfo> colorComparator = new Comparator<ReprintInfo>() {
        @Override
        public int compare(ReprintInfo left, ReprintInfo right) {
            int ret;
            int leftColorMask = getColorMask(left.card);
            int rightColorMask = getColorMask(right.card);
            if (leftColorMask == rightColorMask) {
                if (left.card.converted == right.card.converted) {
                    if (left.order == right.order) {
                        ret = left.formatedNumber.compareTo(right.formatedNumber);
                    } else {
                        ret = left.order - right.order;
                    }
                } else {
                    ret = left.card.converted - right.card.converted;
                }
            } else {
                ret = leftColorMask - rightColorMask;
            }
            return reverse ? -ret : ret;
        }
    };

    static boolean hasColor(CardInfo card, String longColor, String shortColor) {
        return (card.colorIndicator != null && card.colorIndicator.contains(longColor)) ||
                (card.mana != null && card.mana.contains(shortColor));
    }

    static boolean hasLandColor(CardInfo card, String manaColor, String land) {
        return (card.types != null && card.types.contains(land)) ||
                (card.text != null && (card.text.contains("{" + manaColor) || card.text.contains(manaColor + "}")));
    }

    static int getColorMask(CardInfo card) {
        int mask = 0;
        int colors = 0;
        if (hasColor(card, "White", "W")) {
            mask |= 0x1;
            colors++;
        }
        if (hasColor(card, "Blue", "U")) {
            mask |= 0x10;
            colors++;
        }
        if (hasColor(card, "Black", "B")) {
            mask |= 0x100;
            colors++;
        }
        if (hasColor(card, "Red", "R")) {
            mask |= 0x1000;
            colors++;
        }
        if (hasColor(card, "Green", "G")) {
            mask |= 0x10000;
            colors++;
        }
        mask += 0x100000 * colors;
        if (mask == 0) {
            if (card.types.contains("Land")) {
                mask = 0x10000000;
                if (hasLandColor(card, "W", "Plains")) {
                    mask |= 0x1;
                    colors++;
                }
                if (hasLandColor(card, "U", "Island")) {
                    mask |= 0x10;
                    colors++;
                }
                if (hasLandColor(card, "B", "Swamp")) {
                    mask |= 0x100;
                    colors++;
                }
                if (hasLandColor(card, "R", "Mountain")) {
                    mask |= 0x1000;
                    colors++;
                }
                if (hasLandColor(card, "G", "Forest")) {
                    mask |= 0x10000;
                    colors++;
                }
                mask += 0x100000 * colors;
                if (card.superTypes.contains("Basic")) {
                    mask += 0x10000000;
                    if (card.name.equals("Plains") || card.name.equals("Island")
                            || card.name.equals("Swamp")
                            || card.name.equals("Mountain")
                            || card.name.equals("Forest")) {
                        mask += 0x10000000;
                    }
                }
                if (card.types.contains("Artifact")) {
                    mask = 0x2000000 + mask - 0x10000000;
                }
            } else if (card.types.contains("Artifact")) {
                mask = 0x1000000;
            }
        }
        return mask;
    }

    public static String switchSortType(boolean increment) {
        if (increment) {
            sortType++;
            if (sortType >= sortName.length) {
                sortType = 0;
            }
        } else {
            sortType--;
            if (sortType < 0) {
                sortType = sortName.length - 1;
            }
        }
        return sortName[sortType];
    }

    public static String getSortType() {
        return sortName[sortType];
    }

    public static boolean getSingleMode() {
        return single;
    }

    public static boolean switchSingleMode() {
        single = !single;
        return single;
    }

    public static boolean isReverse() {
        return reverse;
    }

    public static void setReverse(boolean r) {
        reverse = r;
    }

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

    private static boolean compareFilter() {
        if (lastFilter.size() != filter.size()) {
            return false;
        } else {
            for (String set : filter) {
                if (!lastFilter.contains(set)) {
                    return false;
                }
            }
        }
        return true;
    }

    private static void copyFilter() {
        lastFilter = new Vector<>();
        lastFilter.addAll(filter);
    }

    public static int getInitProgressMax() {
        if (filter.isEmpty()) {
            return CardParser.SetList.length;
        }
        return filter.size();
    }

    public static int getSearchProgressMax(boolean inResult) {
        if(inResult && resultCards != null) {
            return resultCards.size();
        } else {
            return reprintCards;
        }
    }

    public static int getProgress() {
        return progress;
    }

    public static int getFoundCards() {
        return foundCards;
    }

    public static void initProgress() {
        progress = 0;
        foundCards = 0;
        stop = false;
    }

    private static void SortReprint(CardInfo card) {
        card.reprintTimes = card.reprints.size();

        if (card.reprints.size() == 1) {
            card.reprints.get(0).reprintIndex = 1;
            card.reprints.get(0).latest = true;
            return;
        }
        Collections.sort(card.reprints, new Comparator<ReprintInfo>() {
            @Override
            public int compare(ReprintInfo left, ReprintInfo right) {
                return left.multiverseid - right.multiverseid;
            }
        });
        for (int i = 0; i < card.reprintTimes; i++) {
            card.reprints.get(i).reprintIndex = i + 1;
            if (i == card.reprintTimes - 1) {
                card.reprints.get(i).latest = true;
            }
        }
    }

    public static String initData() {
        if (lastFilter != null && compareFilter()) {
            return setOrder.size() + " Sets and " + allName.length
                    + " Cards" + " (" + reprintCards + " Reprints)";
        }

        lastCode = null;

        stop = false;
        progress = 0;
        foundCards = 0;

        setOrder.clear();
        cardDatabase.clear();
        reprintCards = 0;

        for (String[] s : CardParser.SetList) {
            landIndex = new int[5];
            cardNameInSet = new HashMap<>();
            if (filter.isEmpty() || filter.contains(s[0])) {
                if (stop) {
                    break;
                }
                progress++;
                for (int i = 2; i < s.length; i++) {
                    setOrder.add(s[i]);
                    processSet(new File(MainActivity.SDPath + "/MTG/Oracle/MtgOracle_" + s[i] + ".txt"));
                }
            }
        }

        if (!stop) {
            copyFilter();
        }

        allName = new String[cardDatabase.size()];

        Enumeration<String> keys = cardDatabase.keys();
        int count = 0;
        while (keys.hasMoreElements()) {
            CardInfo info = cardDatabase.get(keys.nextElement());
            SortReprint(info);
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

        return setOrder.size() + " Sets and " + allName.length
                + " Cards" + " (" + reprintCards + " Reprints)";
    }

    public static CardInfo getNewCard(String str) {
        String entry;

        CardInfo card = new CardInfo();
        CardInfo otherCard;

        card.name = getEntry(str, "Name");

        card.simpleName = card.name.replaceAll(" \\(.+/.+\\)", "").replaceAll("®", "")
                .replaceAll("[àáâ]", "a").replaceAll("é", "e").replaceAll("í", "i")
                .replaceAll("ö", "o").replaceAll("[úû]", "u").replaceAll("Æ", "AE");

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
                    if (s.equals("Legendary")) {
                        card.isLegendary = true;
                    }
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

        if (reprint.specialType == null) {
            switch (reprint.set) {
                case "Eighth Edition Box Set":
                    reprint.code = "8EB";
                    reprint.folder = "Modern/8ED/8EB";
                    reprint.altCode = "8EB";
                    break;
                case "Ninth Edition Box Set":
                    reprint.code = "9EB";
                    reprint.folder = "Modern/9ED/9EB";
                    reprint.altCode = "9EB";
                    break;
                default:
                    for (String[] strs : CardParser.SetList) {
                        String set = reprint.set.replace("Premium Deck Series:", "Premium:")
                                .replace("Duel Decks:", "Duel:").replace("The Coalition", "Coalition").replace(" vs. ", " vs ");
                        if (set.equals(strs[0])) {
                            reprint.code = strs[1].substring(strs[1].lastIndexOf("/") + 1);
                            reprint.folder = strs[1];
                            reprint.altCode = strs[2];
                            break;
                        }
                    }
            }
        } else {
            int index = 0;
            switch (reprint.set) {
                case "Conspiracy":
                    index = CardParser.SetList.length - 4;
                    break;
                case "Archenemy":
                    index = CardParser.SetList.length - 3;
                    break;
                case "Planechase 2012 Edition":
                    index = CardParser.SetList.length - 2;
                    break;
                case "Planechase":
                    index = CardParser.SetList.length - 1;
                    break;
            }
            String[] strs = CardParser.SetList[index];
            reprint.code = strs[1].substring(strs[1].lastIndexOf("/") + 1);
            reprint.folder = strs[1];
            reprint.altCode = strs[2];
        }

        reprint.picture = reprint.folder + "/" + reprint.formatedNumber + ".jpg";

        reprint.order = setOrder.indexOf(reprint.altCode);

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
        System.out.println("Process Oracle: " + file);
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

    public static String getFilterString() {
        return filterString;
    }

    public static String getWrongCard() {
        String card = wrongCard;
        wrongCard = null;
        return card;
    }

    public static void setFilter(String sets) {
        if (sets == null || sets.equals("")) {
            filterString = "All";
            filter.clear();
        } else if (sets.equals("Back")) {
            if (filterString.isEmpty()) {
                filterString = "Back";
            }
        } else {
            Vector<String> v = new Vector<>();
            String[] paths = sets.split("\\|");
            for (String[] s : CardParser.SetList) {
                for (String path : paths) {
                    if ((s[1] + "/").contains(path + "/")) {
                        v.add(s[0]);
                        break;
                    }
                }
            }
            if (!v.isEmpty()) {
                filterString = sets;
                filter = v;
            }
        }
    }

    public static void setStop() {
        stop = true;
    }

    public static void clearResults() {
        resultCards = null;
    }

    public static boolean containsUpperCase(String text) {
        for (char c : text.toCharArray()) {
            if (c >= 'A' && c <= 'Z') {
                return true;
            }
        }
        return false;
    }

    public static boolean containsText(String text, String search) {
        if (search.startsWith("\"") && search.endsWith("\"")) {
            String s = search.substring(1, search.length() - 1);
            if (s.isEmpty()) {
                return false;
            }
            Pattern pattern = Pattern.compile("\\b" + Pattern.quote(s) + "\\b");
            Matcher matcher = pattern.matcher(text);
            return matcher.find();
        } else {
            return text.contains(search);
        }
    }

    public static boolean checkStringGroup(String text, String search, boolean anyWord) {
        if (search.isEmpty()) {
            return true;
        }

        if (!containsUpperCase(search)) {
            text = text.toLowerCase();
            search = search.toLowerCase();
        }

        boolean ret = false;
        String[] strs = search.contains("|") ? search.split("\\|") : search.split(" ");

        if (anyWord) {
            for (String s : strs) {
                if (!s.isEmpty() && containsText(text, s)) {
                    return true;
                }
            }
        } else {
            for (String s : strs) {
                if (!s.isEmpty()) {
                    if (containsText(text, s)) {
                        ret = true;
                    } else {
                        ret = false;
                        break;
                    }
                }
            }
        }
        return ret;
    }

    private static boolean checkCard(ReprintInfo reprint, String script, Vector<ReprintInfo> cards) {
        progress++;
        int result;

        if (script.startsWith("@")) {
            result = checkStringGroup(reprint.card.simpleName, script.substring(1).trim(), false) ? 1 : 0;
        } else {
            result = LuaScript.checkCard(reprint, script);
        }

        if (result == 1) {
            foundCards++;
            cards.add(reprint);
        } else if (result == 2) {
            wrongCard = reprint.picture;
            results = new String[]{wrongCard};
            return false;
        }
        return true;
    }

    public static int searchCard(String script, boolean searchResult) {
        Vector<ReprintInfo> cards = new Vector<>();
        boolean skipSearch = false;
        boolean noResult = false;

        wrongCard = null;
        stop = false;
        progress = 0;
        foundCards = 0;
        exclude = 0;

        if (lastCode != null && !script.isEmpty() && script.equals(lastCode)) {
            skipSearch = true;
        }

        if (resultCards == null) {
            noResult = true;
            searchResult = false;
            skipSearch = false;
            resultCards = new Vector<>();
        }

        if (skipSearch) {
            cards = resultCards;
        } else {
            if (searchResult) {
                if (resultCards.isEmpty()) {
                    return -2;
                }
                for (ReprintInfo reprint : resultCards) {
                    if (stop) {
                        break;
                    }
                    if (!checkCard(reprint, script, cards)) {
                        return -1;
                    }
                }
            } else {
                for (String name : allName) {
                    if (stop) {
                        break;
                    }
                    CardInfo card = cardDatabase.get(name);
                    for (ReprintInfo reprint : card.reprints) {
                        if (!checkCard(reprint, script, cards)) {
                            return -1;
                        }
                    }
                }
            }

            if (!stop) {
                resultCards = cards;
            } else if (noResult) {
                resultCards = null;
            }
        }

        if (!stop) {
            lastCode = script;
        }

        switch (sortName[sortType]) {
            case "Edition":
                Collections.sort(cards, editionComparator);
                break;
            case "Name":
                Collections.sort(cards, nameComparator);
                break;
            case "Cmc":
                Collections.sort(cards, cmcComparator);
                break;
            case "Color":
                Collections.sort(cards, colorComparator);
                break;
            case "Rating":
                Collections.sort(cards, ratingComparator);
                break;
            case "Random":
                if(!single) {
                    Collections.shuffle(cards);
                }
                break;
        }

        if (single) {
            Vector<String> names = new Vector<>();
            Vector<ReprintInfo> singleCards = new Vector<>();

            if (sortName[sortType].equals("Random")) {
                Collections.shuffle(cards);
            }

            for (ReprintInfo info : cards) {
                if (names.contains(info.card.name)) {
                    continue;
                }
                singleCards.add(info);
                names.add(info.card.name);
            }

            if (sortName[sortType].equals("Random")) {
                Collections.shuffle(singleCards);
            }

            results = new String[singleCards.size()];

            for (int i = 0; i < results.length; i++) {
                results[i] = singleCards.get(i).picture;
            }
        } else {
            results = new String[cards.size()];

            for (int i = 0; i < results.length; i++) {
                results[i] = cards.get(i).picture;
            }
        }

        exclude = cards.size() - results.length;

        return results.length;
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
        public int order;
        public int reprintIndex;
        public boolean latest;

        public String toString() {
            return multiverseid + " " + set
                    + (specialType == null ? "" : " [" + specialType + "]")
                    + " : " + number + " (" + rarity + ") " + artist + " (" + rating + "|" + votes + ") " + "\n";
        }
    }

    public static class CardInfo {

        public String name;
        public String simpleName;
        public String otherPart;
        public int partIndex;
        public boolean isSplit;
        public boolean isDoubleFaced;
        public boolean isFlip;
        public boolean isLegendary;
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

        public int reprintTimes;

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
