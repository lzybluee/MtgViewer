package lu.cifer.mtgviewer;

import android.util.Log;

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

    public static String[] LegalList = {"Modern", "Legacy", "Vintage", "Commander"};
    public static String[] TypeList = {"Artifact", "Creature", "Enchantment",
            "Instant", "Land", "Planeswalker", "Sorcery", "Tribal"};
    public static String[] SpecialTypeList = {"Conspiracy", "Phenomenon", "Plane", "Scheme", "Dungeon"};

    public static String[] results;
    public static int exclude;

    static Hashtable<String, CardInfo> cardDatabase = new Hashtable<>();
    static int reprintCards = 0;
    static String[] allName;
    static int[] landIndex = new int[5];
    static HashMap<String, Integer> cardNameInSet = new HashMap<>();
    static String wrongCard;
    static String filterString = "";
    static Vector<String> lastFilter;
    static Vector<String> filter = new Vector<>();
    static Vector<String> setList = new Vector<>();
    static int progress;
    static int foundCards;
    static boolean reverse;
    static int sortType = 0;
    static String[] sortName = new String[]{"Random", "Edition", "Name", "Value", "Color"};
    static boolean stop;
    static Vector<ReprintInfo> resultCards;
    static String lastCode;
    static boolean unique = true;
    static boolean showResults = false;

    static Comparator<ReprintInfo> editionComparator = new Comparator<ReprintInfo>() {
        @Override
        public int compare(ReprintInfo left, ReprintInfo right) {
            int ret;
            if (left.setOrder == right.setOrder) {
                ret = left.formattedNumber.compareTo(right.formattedNumber);
            } else {
                ret = left.setOrder - right.setOrder;
            }
            return reverse ? -ret : ret;
        }
    };
    static Comparator<ReprintInfo> nameComparator = new Comparator<ReprintInfo>() {
        @Override
        public int compare(ReprintInfo left, ReprintInfo right) {
            int ret;
            if (left.card.name.equals(right.card.name)) {
                if (left.setOrder == right.setOrder) {
                    ret = left.formattedNumber.compareTo(right.formattedNumber);
                } else {
                    ret = left.setOrder - right.setOrder;
                }
            } else {
                ret = left.card.name.compareTo(right.card.name);
            }
            return reverse ? -ret : ret;
        }
    };
    static Comparator<ReprintInfo> valueComparator = new Comparator<ReprintInfo>() {
        @Override
        public int compare(ReprintInfo left, ReprintInfo right) {
            int ret;
            if (left.card.value == right.card.value) {
                if (left.setOrder == right.setOrder) {
                    ret = left.formattedNumber.compareTo(right.formattedNumber);
                } else {
                    ret = left.setOrder - right.setOrder;
                }
            } else {
                ret = left.card.value - right.card.value;
            }
            return reverse ? -ret : ret;
        }
    };
    static Comparator<ReprintInfo> colorComparator = new Comparator<ReprintInfo>() {
        @Override
        public int compare(ReprintInfo left, ReprintInfo right) {
            int ret;
            int leftColorMask = getColorMask(left.card);
            int rightColorMask = getColorMask(right.card);
            if (leftColorMask == rightColorMask) {
                if (left.card.value == right.card.value) {
                    if (left.setOrder == right.setOrder) {
                        ret = left.formattedNumber.compareTo(right.formattedNumber);
                    } else {
                        ret = left.setOrder - right.setOrder;
                    }
                } else {
                    ret = left.card.value - right.card.value;
                }
            } else {
                ret = leftColorMask - rightColorMask;
            }
            return reverse ? -ret : ret;
        }
    };

    public static CardInfo get(String name) {
        return cardDatabase.get(name);
    }

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

    public static boolean getUniqueMode() {
        return unique;
    }

    public static boolean switchUniqueMode() {
        unique = !unique;
        return unique;
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

    private static String getFormattedNumber(String name) {
        Pattern pattern = Pattern.compile("([^\\d]*)(\\d+)([^\\d]*)");
        Matcher matcher = pattern.matcher(name);

        if (matcher.find()) {
            String text = matcher.group(2);
            if (text != null) {
                int num = Integer.parseInt(text);
                return matcher.group(1) + String.format("%03d", num) + matcher.group(3);
            }
        }
        return null;
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
        if (inResult && resultCards != null) {
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

    private static void sortReprint(CardInfo card) {
        card.reprintTimes = card.reprints.size();

        if (card.reprints.size() == 1) {
            card.reprints.get(0).reprintIndex = 1;
            return;
        }
        Collections.sort(card.reprints, (left, right) -> {
            if (left.multiverseId == right.multiverseId) {
                return left.formattedNumber.compareTo(right.formattedNumber);
            }
            return left.multiverseId - right.multiverseId;
        });
    }

    public static String initData() {
        if (lastFilter != null && compareFilter()) {
            return setList.size() + " Sets and " + allName.length
                    + " Cards" + " (" + reprintCards + " Reprints)";
        }

        lastCode = null;

        stop = false;
        progress = 0;
        foundCards = 0;

        setList.clear();
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
                setList.add(s[2]);
                processSet(new File(MainActivity.SDPath + "/MTG/" + CardParser.oracleFolder
                        + "/MtgOracle_" + s[2] + ".txt"));
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
            if (info != null) {
                sortReprint(info);
                allName[count] = info.name;
                count++;
            }
        }

        Arrays.sort(allName);

        return setList.size() + " Sets and " + allName.length
                + " Cards" + " (" + reprintCards + " Reprints)";
    }

    public static CardInfo getNewCard(String str) {
        String entry;

        CardInfo card = new CardInfo();
        CardInfo otherCard;

        card.name = getEntry(str, "Name");

        if (card.name != null) {
            card.simpleName = card.name.replaceAll(" \\(.+/.+\\)", "").replaceAll("®", "")
                    .replaceAll("[àáâ]", "a").replaceAll("é", "e").replaceAll("í", "i")
                    .replaceAll("ö", "o").replaceAll("[úû]", "u").replaceAll("Æ", "AE");
        }

        card.otherPart = new Vector<>();

        entry = getEntry(str, "OtherPart");
        if (entry != null) {
            card.otherPart.add(entry);
            String num = getEntry(str, "No");
            if (num != null && num.charAt(num.length() - 1) >= 'a') {
                card.partIndex = num.charAt(num.length() - 1) - 'a' + 1;
            }
            if (card.name.contains("(") && card.name.contains("/")) {
                card.isSplit = true;
            } else if (card.partIndex == 2) {
                String code = getEntry(str, "SetId");
                if (code != null) {
                    switch (code) {
                        case "ZNR":
                        case "KHM":
                        case "STX":
                            card.isModalDoubleFaced = true;
                            card.isDoubleFaced = true;
                            otherCard = cardDatabase.get(entry);
                            if (otherCard != null) {
                                otherCard.isModalDoubleFaced = true;
                                otherCard.isDoubleFaced = true;
                            }
                            break;
                        case "CHK":
                        case "BOK":
                        case "SOK":
                        case "CMD":
                        case "C18":
                        case "CM2":
                            card.isFlip = true;
                            otherCard = cardDatabase.get(entry);
                            if (otherCard != null) {
                                otherCard.isFlip = true;
                            }
                            break;
                        case "ELD":
                            card.isAdventure = true;
                            otherCard = cardDatabase.get(entry);
                            if (otherCard != null) {
                                otherCard.isAdventure = true;
                            }
                            break;
                        default:
                            card.isDoubleFaced = true;
                            for (String other : entry.split(" ; ")) {
                                otherCard = cardDatabase.get(other);
                                if (otherCard != null) {
                                    otherCard.isDoubleFaced = true;
                                }
                            }
                            break;
                    }
                }
            }
        }

        entry = getEntry(str, "Type");

        if (entry != null) {
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
                String text = matcher.group(0);
                if (text != null) {
                    card.loyalty = matcher.group(1);
                    entry = entry.substring(0, entry.indexOf(text));
                }
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
                    for (String t : SpecialTypeList) {
                        if (s.equals(t)) {
                            card.types.add(s);
                            flag = true;
                            break;
                        }
                    }
                    if (flag) {
                        continue;
                    }
                    if (s.equals("Legendary")) {
                        card.isLegendary = true;
                    }
                    card.superTypes.add(s);
                }
            }

            if (subtypes != null) {
                card.subTypes.addAll(Arrays.asList(subtypes.trim().split(" ")));
            }
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
                card.mana = entry;
                card.value = 0;

                for (String mana : entry.substring(1).split("[{}]+")) {
                    if (mana.matches("\\d+")) {
                        card.value += Integer.parseInt(mana);
                    } else if (mana.matches("[WUBRGCS]")) {
                        card.value += 1;
                    } else if (mana.matches("[WUBRG]/[WUBRGP]")) {
                        card.value += 1;
                    } else if (mana.matches("[XYZ]")) {
                        card.value += 0;
                    } else if (mana.matches("2/[WUBRGP]")) {
                        card.value += 2;
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
        reprint.watermark = getEntry(str, "Watermark");

        String idEntry = getEntry(str, "Multiverseid");
        if (idEntry != null) {
            reprint.multiverseId = Integer.parseInt(idEntry);
        }

        if (reprint.rarity == null) {
            reprint.rarity = "Special";
        }

        for (String[] set : CardParser.SetList) {
            if (reprint.set.equals(set[0])) {
                reprint.code = set[2];
                reprint.folder = set[1];
                break;
            }
        }

        reprint.formattedNumber = getFormattedNumber(reprint.number);

        if (reprint.folder == null || reprint.formattedNumber == null) {
            Log.e("MtgViewer", "No picture " + reprint.set + " " + reprint.number);
        }

        reprint.picture = reprint.folder + "/" + reprint.formattedNumber + ".jpg";

        if (reprint.picture.contains("Funny/")) {
            if (!card.name.equals("Plains") && !card.name.equals("Island")
                    && !card.name.equals("Swamp")
                    && !card.name.equals("Mountain")
                    && !card.name.equals("Forest")) {
                card.isFun = true;
            }
        }

        reprint.setOrder = setList.indexOf(reprint.code);

        reprint.card = card;
        card.reprints.add(reprint);

        return reprint;
    }

    public static void processCard(String str, HashMap<String, Object> map) {
        ReprintInfo reprint;
        String name = getEntry(str, "Name");

        if (cardDatabase.containsKey(name)) {
            reprint = addReprintCard(str, cardDatabase.get(name));
        } else {
            CardInfo card = getNewCard(getEntry(str, "Card"));
            cardDatabase.put(name, card);
            card.reprints = new Vector<>();
            reprint = addReprintCard(str, card);
        }

        if (map.containsKey(name)) {
            Object obj = map.get(name);
            if (obj instanceof ReprintInfo) {
                ReprintInfo info = (ReprintInfo) obj;
                info.sameIndex = 1;
                reprint.sameIndex = 2;
                map.put(name, 2);
            } else {
                int n = (Integer) map.get(name);
                n++;
                reprint.sameIndex = n;
                map.put(name, n);
            }
        } else {
            map.put(name, reprint);
        }
    }

    public static void processSet(File file) {
        System.out.println("Process Oracle: " + file);
        BufferedReader reader;
        HashMap<String, Object> map = new HashMap<>();
        try {
            reader = new BufferedReader(new FileReader(file));
            String str;
            StringBuilder card = new StringBuilder();
            while ((str = reader.readLine()) != null) {
                if (str.isEmpty()) {
                    processCard(card.toString(), map);
                    card = new StringBuilder();
                } else {
                    card.append(str).append("\n");
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
        String[] set = search.contains("|") ? search.split("\\|") : search.split(" ");

        if (anyWord) {
            for (String s : set) {
                if (!s.isEmpty() && containsText(text, s)) {
                    return true;
                }
            }
        } else {
            for (String s : set) {
                if (!s.isEmpty()) {
                    if (s.startsWith("^")) {
                        if (!containsText(text, s.substring(1))) {
                            ret = true;
                        } else {
                            ret = false;
                            break;
                        }
                    } else if (containsText(text, s)) {
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
            showResults = true;
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

        if (!script.isEmpty() && script.equals(lastCode)) {
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
                    if (card != null) {
                        for (ReprintInfo reprint : card.reprints) {
                            if (!checkCard(reprint, script, cards)) {
                                return -1;
                            }
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
            case "Random":
                if (!unique) {
                    Collections.shuffle(cards);
                }
                break;
            case "Edition":
                Collections.sort(cards, editionComparator);
                break;
            case "Name":
                Collections.sort(cards, nameComparator);
                break;
            case "Value":
                Collections.sort(cards, valueComparator);
                break;
            case "Color":
                Collections.sort(cards, colorComparator);
                break;
        }

        if (unique) {
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

        showResults = true;
        return results.length;
    }

    public static class ReprintInfo {

        public CardInfo card;

        public int multiverseId;
        public String set;
        public String code;
        public String folder;
        public String number;
        public String flavor;
        public String artist;
        public String rarity;
        public String watermark;

        public String picture;
        public int sameIndex;
        public String formattedNumber;
        public int setOrder;
        public int reprintIndex;

        public String toString() {
            return multiverseId + " " + set + " : " + number
                    + " (" + rarity + ") " + artist;
        }
    }

    public static class CardInfo {

        public String name;
        public String simpleName;
        public Vector<String> otherPart;
        public int partIndex;
        public boolean isSplit;
        public boolean isDoubleFaced;
        public boolean isModalDoubleFaced;
        public boolean isFlip;
        public boolean isAdventure;
        public boolean isLegendary;
        public boolean isFun;

        public Vector<String> types;
        public Vector<String> subTypes;
        public Vector<String> superTypes;

        public String mana;
        public int value;
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

        public int reprintTimes;

        public String toString() {
            StringBuilder str = new StringBuilder();
            str.append(name).append("\n");
            if (partIndex > 0) {
                String suffix = "th";
                if (Integer.toString(partIndex).endsWith("1")) {
                    suffix = "st";
                } else if (Integer.toString(partIndex).endsWith("2")) {
                    suffix = "nd";
                } else if (Integer.toString(partIndex).endsWith("3")) {
                    suffix = "rd";
                }
                if (otherPart.size() >= 2) {
                    str.append(partIndex).append(suffix).append(" part of the card, other parts are ");
                    for (int i = 0; i < otherPart.size(); i++) {
                        str.append("<").append(otherPart.get(i)).append(">");
                        if (i == otherPart.size() - 1) {
                            str.append("\n");
                        } else if (i == otherPart.size() - 2) {
                            str.append(" and ");
                        } else {
                            str.append(" , ");
                        }
                    }
                } else if (otherPart.size() == 1) {
                    str.append(partIndex).append(suffix).append(" part of the card, other part is <").append(otherPart.get(0)).append(">\n");
                }
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
                str.append(" ").append(power);
            }
            if (toughness != null) {
                str.append("/").append(toughness);
            }
            if (loyalty != null) {
                str.append(" (Loyalty: ").append(loyalty).append(")");
            }
            str.append("\n");
            if (mana != null) {
                str.append(mana).append(" (").append(value).append(")").append("\n");
            }
            if (colorIndicator != null) {
                str.append("(Color Indicator: ").append(colorIndicator).append(")\n");
            }
            if (text != null) {
                str.append(text).append("\n");
            }
            if (rules != null) {
                str.append(rules).append("\n");
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
                    str.append("(Watermark: ").append(info.watermark).append(")\n");
                    break;
                }
            }
            for (ReprintInfo info : reprints) {
                str.append(info).append("\n");
            }
            Vector<String> flavors = new Vector<>();
            for (ReprintInfo info : reprints) {
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
                str.append(s).append("\n");
            }

            return str.toString();
        }
    }
}
