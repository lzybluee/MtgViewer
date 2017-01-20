package lu.cifer.mtgviewer;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

class ReprintSimpleInfo {

    public int multiverseid;
    public String set;
    public String number;
    public String flavor;
    public String artist;
    public String rarity;
    public String rating;
    public String votes;

    public String picture;

    public String toString() {
        return multiverseid + " " + set + " : " + number + " (" + rarity + ") "
                + artist + " [" + picture + "]" + "\n";
    }
}

class CardSimpleInfo {

    public String name;
    public String otherPart;
    public int partIndex;
    public boolean isSplit;
    public boolean isDoubleFaced;
    public boolean isFlip;

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
    public boolean reversed;

    public Vector<ReprintSimpleInfo> reprints;

    public String toString() {
        StringBuilder str = new StringBuilder();
        str.append(name + "\n");
        if (partIndex > 0) {
            String suffix = "ed";
            if (Integer.toString(partIndex).endsWith("1")) {
                suffix = "st";
            } else if (Integer.toString(partIndex).endsWith("2")) {
                suffix = "nd";
            } else if (Integer.toString(partIndex).endsWith("3")) {
                suffix = "rd";
            }
            str.append(partIndex + suffix
                    + " part of the card, other part is <" + otherPart + ">\n");
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
        if (reversed) {
            str.append("In REVERSED list!\n");
        }
        Vector<String> flavors = new Vector<>();
        for (ReprintSimpleInfo info : reprints) {
            str.append(info);
            if (info.flavor != null) {
                boolean flag = false;
                for (String s : flavors) {
                    if (s.equals(info.flavor)) {
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

public class CardAnalyzer {

    public static String[] LegalList = {"Block", "Standard", "Extended",
            "Modern", "Legacy", "Vintage", "Commander", /* "Classic" */};
    public static String[] TypeList = {"Artifact", "Creature", "Enchantment",
            "Instant", "Land", "Planeswalker", "Sorcery", "Tribal"};
    public static String prefix = "";
    public static int rulePage = 0;
    public static String[][] SetList = {
            {"Magic Origins", "Modern/ORI", "ORI"},
            {"Dragons of Tarkir", "Modern/DTK", "DTK"},
            {"Fate Reforged", "Modern/FRF", "FRF"},
            {"Khans of Tarkir", "Modern/KTK", "KTK"},
            {"Magic 2015", "Modern/M15", "M15"},

            {"Journey into Nyx", "Modern/JOU", "JOU"},
            {"Born of the Gods", "Modern/BNG", "BNG"},
            {"Theros", "Modern/THS", "THS"},
            {"Magic 2014", "Modern/M14", "M14"},
            {"Dragon's Maze", "Modern/DGM", "DGM"},
            {"Gatecrash", "Modern/GTC", "GTC"},
            {"Return to Ravnica", "Modern/RTR", "RTR"},
            {"Magic 2013", "Modern/M13", "M13"},
            {"Avacyn Restored", "Modern/AVR", "AVR"},
            {"Dark Ascension", "Modern/DKA", "DKA"},
            {"Innistrad", "Modern/ISD", "ISD"},
            {"Magic 2012", "Modern/M12", "M12"},
            {"New Phyrexia", "Modern/NPH", "NPH"},
            {"Mirrodin Besieged", "Modern/MBS", "MBS"},
            {"Scars of Mirrodin", "Modern/SOM", "SOM"},
            {"Magic 2011", "Modern/M11", "M11"},
            {"Rise of the Eldrazi", "Modern/ROE", "ROE"},
            {"Worldwake", "Modern/WWK", "WWK"},
            {"Zendikar", "Modern/ZEN", "ZEN"},
            {"Magic 2010", "Modern/M10", "M10"},
            {"Alara Reborn", "Modern/ARB", "ARB"},
            {"Conflux", "Modern/CFX", "CFX"},
            {"Shards of Alara", "Modern/ALA", "ALA"},
            {"Eventide", "Modern/EVE", "EVE"},
            {"Shadowmoor", "Modern/SHM", "SHM"},
            {"Morningtide", "Modern/MOR", "MT"},
            {"Lorwyn", "Modern/LRW", "LW"},
            {"Tenth Edition", "Modern/10E", "10E"},
            {"Future Sight", "Modern/FUT", "FUT"},
            {"Planar Chaos", "Modern/PLC", "PC"},
            {"Time Spiral \"Timeshifted\"", "Modern/TSB", "TSTS"},
            {"Time Spiral", "Modern/TSP", "TS"},
            {"Coldsnap", "Modern/CSP", "CS"},
            {"Dissension", "Modern/DIS", "DI"},
            {"Guildpact", "Modern/GPT", "GP"},
            {"Ravnica: City of Guilds", "Modern/RAV", "RAV"},
            {"Ninth Edition", "Modern/9ED", "9E", "9EB"},
            {"Saviors of Kamigawa", "Modern/SOK", "SOK"},
            {"Betrayers of Kamigawa", "Modern/BOK", "BOK"},
            {"Champions of Kamigawa", "Modern/CHK", "CHK"},
            {"Fifth Dawn", "Modern/5DN", "5DN"},
            {"Darksteel", "Modern/DST", "DS"},
            {"Mirrodin", "Modern/MRD", "MI"},
            {"Eighth Edition", "Modern/8ED", "8E", "8EB"},

            {"Scourge", "Ancient/SCG", "SC"},
            {"Legions", "Ancient/LGN", "LE"},
            {"Onslaught", "Ancient/ONS", "ON"},
            {"Judgment", "Ancient/JUD", "JU"},
            {"Torment", "Ancient/TOR", "TR"},
            {"Odyssey", "Ancient/ODY", "OD"},
            {"Apocalypse", "Ancient/APC", "AP"},
            {"Seventh Edition", "Ancient/7ED", "7E"},
            {"Planeshift", "Ancient/PLS", "PS"},
            {"Invasion", "Ancient/INV", "IN"},
            {"Prophecy", "Ancient/PCY", "PR"},
            {"Nemesis", "Ancient/NMS", "NE"},
            {"Mercadian Masques", "Ancient/MMQ", "MM"},
            {"Urza's Destiny", "Ancient/UDS", "UD"},
            {"Classic Sixth Edition", "Ancient/6ED", "6E"},
            {"Urza's Legacy", "Ancient/ULG", "UL"},
            {"Urza's Saga", "Ancient/USG", "US"},
            {"Exodus", "Ancient/EXO", "EX"},
            {"Stronghold", "Ancient/STH", "SH"},
            {"Tempest", "Ancient/TMP", "TP"},
            {"Weatherlight", "Ancient/WTH", "WL"},
            {"Fifth Edition", "Ancient/5ED", "5E"},
            {"Visions", "Ancient/VIS", "VI"},
            {"Mirage", "Ancient/MIR", "MR"},
            {"Alliances", "Ancient/ALL", "AI"},
            {"Homelands", "Ancient/HML", "HL"},
            {"Ice Age", "Ancient/ICE", "IA"},
            {"Fourth Edition", "Ancient/4ED", "4E"},
            {"Fallen Empires", "Ancient/FEM", "FE"},
            {"The Dark", "Ancient/DRK", "DK"},
            {"Legends", "Ancient/LEG", "LG"},
            {"Revised Edition", "Ancient/3ED", "RV"},
            {"Antiquities", "Ancient/ATQ", "AQ"},
            {"Arabian Nights", "Ancient/ARN", "AN"},
            {"Unlimited Edition", "Ancient/2ED", "UN"},
            {"Limited Edition Beta", "Ancient/LEB", "BE"},
            {"Limited Edition Alpha", "Ancient/LEA", "AL"},

            {"Commander 2014 Edition", "Commander/C14", "C14"},
            {"Commander 2013 Edition", "Commander/C13", "C13"},
            {"Commander", "Commander/CMD", "CMD"},
            {"Planechase 2012 Edition", "Planechase/PC2", "PC2"},
            {"Planechase", "Planechase/HOP", "PCH"},
            {"Archenemy", "Archenemy/ARC", "ARC"},
            {"Conspiracy", "Conspiracy/CNS", "CNS"},
            {"Portal Three Kingdoms", "Starter/PTK", "P3K"},
            {"Portal Second Age", "Starter/PO2", "PO2"},
            {"Portal", "Starter/POR", "PO"},
            {"Starter 1999", "Starter/S99", "ST"},

            {"Modern Masters 2015 Edition", "Reprint/MM2", "MM2"},
            {"Modern Masters", "Reprint/MMA", "MMA"},
            {"Vintage Masters", "Reprint/VMA", "VMA"},
            {"Eternal Masters", "Reprint/EMA", "EMA"},
            {"Duel: Kiora vs. Elspeth", "Reprint/DDO", "DDO"},
            {"Duel: Speed vs. Cunning", "Reprint/DDN", "DDN"},
            {"Duel: Jace vs Vraska", "Reprint/DDM", "DDM"},
            {"Duel: Heroes vs Monsters", "Reprint/DDL", "DDL"},
            {"Duel: Sorin vs Tibalt", "Reprint/DDK", "DDK"},
            {"Duel: Izzet vs Golgari", "Reprint/DDJ", "DDJ"},
            {"Duel: Venser vs Koth", "Reprint/DDI", "DDI"},
            {"Duel: Ajani vs Nicol Bolas", "Reprint/DDH", "DDH"},
            {"Duel: Knights vs Dragons", "Reprint/DDG", "DDG"},
            {"Duel: Elspeth vs Tezzeret", "Reprint/DDF", "DDF"},
            {"Duel: Phyrexia vs Coalition", "Reprint/DDE", "PVC"},
            {"Duel: Garruk vs Liliana", "Reprint/DDD", "GVL"},
            {"Duel: Divine vs Demonic", "Reprint/DDC", "DVD"},
            {"Duel: Jace vs Chandra", "Reprint/DD2", "JVC"},
            {"Duel: Elves vs Goblins", "Reprint/EVG", "EVG"},
            {"Premium: Graveborn", "Reprint/PD3", "PD3"},
            {"Premium: Fire and Lightning", "Reprint/PD2", "PD2"},
            {"Premium: Slivers", "Reprint/H09", "PDS"},

            {"From the Vault: Angels", "Reprint/V15", "V15"},
            {"From the Vault: Annihilation", "Reprint/V14", "V14"},
            {"From the Vault: Twenty", "Reprint/V13", "V13"},
            {"From the Vault: Realms", "Reprint/V12", "V12"},
            {"From the Vault: Legends", "Reprint/V11", "FVL"},
            {"From the Vault: Relics", "Reprint/V10", "FVR"},
            {"From the Vault: Exiled", "Reprint/V09", "FVE"},
            {"From the Vault: Dragons", "Reprint/DRB", "FVD"},
            {"MTGO Masters Edition IV", "Reprint/ME4", "ME4"},
            {"MTGO Masters Edition III", "Reprint/ME3", "ME3"},
            {"MTGO Masters Edition II", "Reprint/ME2", "ME2"},
            {"MTGO Masters Edition", "Reprint/MED", "MED"},
            {"Chronicles", "Reprint/CHR", "CH"},
            {"Commander's Arsenal", "Reprint/CMA", "CMA"},

            {"Unhinged", "Unset/UNH", "UH"},
            {"Unglued", "Unset/UGL", "UG"},
    };
    public Hashtable<String, CardSimpleInfo> cardDatabase = new Hashtable<>();

    public CardAnalyzer(String str) {
        prefix = str;
        analyzeData();
    }

    public static String getCardInfo(String url, boolean justRule) {
        String setInfo = "";
        boolean isModern = false;

        File file = null;
        int idx = 0;
        for (String[] strs : SetList) {
            int index;
            if ((index = url.lastIndexOf(strs[1])) >= 0 && !url.contains("/Misc/")) {
                if (url.substring(index + strs[1].length() + 1).contains("/")
                        && strs.length >= 4) {
                    file = new File(MainActivity.SDPath
                            + "/MTG/Oracle/MtgOracle_" + strs[3] + ".txt");
                    setInfo = strs[3].toLowerCase();
                    if (idx <= 48) {
                        isModern = true;
                    }
                } else {
                    file = new File(MainActivity.SDPath
                            + "/MTG/Oracle/MtgOracle_" + strs[2] + ".txt");
                    setInfo = strs[2].toLowerCase();
                    if (idx <= 48) {
                        isModern = true;
                    }
                }
                break;
            }
            idx++;
        }

        if (setInfo.equals("") && url.contains("/Conspiracy/")) {
            setInfo = "cns";
        }

        if (url.contains("/Special/") && !url.contains("/MBP/")) {
            return url;
        }

        if (file == null) {
            String s = url.substring(0, url.lastIndexOf("/"));
            s = s.substring(s.lastIndexOf("/") + 1);
            file = new File(MainActivity.SDPath + "/MTG/Oracle/MtgOracle_" + s
                    + ".txt");
        }

        if (file != null && !file.exists()) {
            return url;
        }

        String num = url.substring(0, url.lastIndexOf("."));
        num = num.substring(num.lastIndexOf("/") + 1);
        while (num.startsWith("0")) {
            num = num.substring(1);
        }

        if (num.length() == 0 || !(num.charAt(0) >= '0' && num.charAt(0) <= '9')) {
            return url;
        }

        String card = url.substring(MainActivity.SDPath.toString().length())
                + "\n\n";

        if (justRule) {
            card = "";
        }

        String rating = "";

        BufferedReader reader;
        try {
            reader = new BufferedReader(new FileReader(file));
            String str;
            boolean flag = false;
            boolean isrule = false;
            boolean isbasic = false;
            while ((str = reader.readLine()) != null) {
                if (str.equals("<No>" + num + "</No>")) {
                    flag = true;
                    rating = "";
                    if (!justRule) {
                        if (setInfo.equals("")) {
                            MainActivity.urlInfo = "";
                        } else {
                            MainActivity.urlInfo = "http://magiccards.info/"
                                    + setInfo + "/en/" + num + ".html";
                        }
                    }
                    isrule = justRule;
                }
                if (flag) {
                    if (str.equals("")) {
                        break;
                    }
                    if (str.startsWith("<Rating>") || str.startsWith("<Votes>")) {
                        if (!justRule) {
                            continue;
                        } else {
                            if (str.startsWith("<Rating>")) {
                                rating = "Rating: " + str.replaceAll("<[^>]+>", "") + "/5";
                            } else {
                                if (str.contains(">0<")) {
                                    rating = "";
                                } else {
                                    rating += " (" + str.replaceAll("<[^>]+>", "") + " votes)\n \n";
                                }
                            }
                        }
                    }
                    if (!isrule && str.startsWith("<Name>")) {
                        isbasic = str.contains(">Plains<") || str.contains(">Island<") || str.contains(">Swamp<")
                                || str.contains(">Mountain<") || str.contains(">Forest<");
                    }
                    if (str.startsWith("<Multiverseid>")) {
                        if (justRule) {
                            String id = str.replaceAll("<Multiverseid>", "")
                                    .replaceAll("</Multiverseid>", "");
                            if (!id.equals("0")) {
                                MainActivity.urlInfo = "http://gatherer.wizards.com/Pages/Card/Discussion.aspx?multiverseid=" + id;
                            } else {
                                MainActivity.urlInfo = "";
                            }
                        }
                    }
                    if (!isrule && str.startsWith("<ColorIndicator>")) {
                        card += "Color: " + str + "\n";
                        continue;
                    }
                    if (!isrule && str.startsWith("<OtherPart>")) {
                        card += "Other: " + str + "\n";
                        continue;
                    }
                    if (!str.startsWith("<Block>") && !str.startsWith("<Standard>")
                            && !str.startsWith("<Extended>")
                            && !str.startsWith("<Classic>")
                            && !str.startsWith("<Multiverseid>")) {
                        if (str.startsWith("<Rulings>")) {
                            isrule = !justRule;
                        }
                        if (!isrule) {
                            if (str.contains(">Banned<")
                                    || str.contains(">Restricted<")
                                    || str.contains(">Legal<")) {
                                if (!str.contains(">Legal<")) {
                                    String[] legals = new String[]{"Modern",
                                            "Legacy", "Vintage", "Commander"};
                                    for (String s : legals) {
                                        if (str.contains(s)) {
                                            if (str.contains(">Banned<")) {
                                                card += "Banned in " + s + "\n";
                                            } else if (str
                                                    .contains(">Restricted<")) {
                                                card += "Restricted in " + s
                                                        + "\n";
                                            }
                                            break;
                                        }
                                    }
                                } else if (str.contains("Modern>Legal<") && !isModern && !isbasic) {
                                    card += "Legal in Modern\n";
                                }
                            } else {
                                card += str + "\n";
                            }
                        }
                        if (str.contains("</Rulings>")) {
                            isrule = justRule;
                            if (justRule) {
                                card = card.replaceAll("<Rulings>", "")
                                        .replaceAll("</Rulings>", "");
                            }
                        }
                    }
                }
            }
            reader.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        if (justRule) {
            if (card.equals("")) {
                card = rating + "No Rulings.";
            } else {
                Vector<String> vector = new Vector<>();
                card = rating + card;
                String[] rules = card.split("\\n");
                String page = "";
                int lines = 0;
                for (int i = 0; i < rules.length; i++) {
                    if (rules[i].equals("")) {
                        continue;
                    }

                    int n = rules[i].length() / 40 + 1;
                    if (lines + n > 20) {
                        if (!page.equals("")) {
                            vector.add(page);
                        }
                        page = rules[i] + "\n";
                        lines = n;
                    } else {
                        lines += n;
                        page += rules[i] + "\n";
                    }

                    if (i == rules.length - 1) {
                        vector.add(page);
                    }
                }
                if (vector.size() > 1) {
                    rulePage %= vector.size();
                    String s = vector.get(rulePage);
                    card = s + ((s.endsWith("\n \n") || s.endsWith("\n\n")) ? "" : "\n") + "[" + (rulePage + 1) + "/"
                            + vector.size() + "]";
                }
            }
        } else {
            card = card.replaceAll("<Flavor>", "[")
                    .replaceAll("</Flavor>", "]").replaceAll("<[^>]+>", "");
        }

        while (card.endsWith("\n")) {
            card = card.substring(0, card.length() - 1);
        }

        return card;
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

    public void analyzeData() {

        File[] dir = new File(prefix + "Oracle/").listFiles();

        for (File file : dir) {
            processSet(file);
        }

        String[] allName = new String[cardDatabase.size()];

        System.out.println(dir.length + " Sets and " + allName.length
                + " Cards");

        Enumeration<String> keys = cardDatabase.keys();
        int count = 0;
        while (keys.hasMoreElements()) {
            CardSimpleInfo info = cardDatabase.get(keys.nextElement());
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

        for (String s : allName) {
            CardSimpleInfo info = cardDatabase.get(s);
            System.out.println(info.name);
            // writer.write(info.toString());
            // writer.newLine();
        }

    }

    public CardSimpleInfo getNewCard(String str) {
        String entry;

        CardSimpleInfo card = new CardSimpleInfo();
        card.name = getEntry(str, "Name");

        card.reprints = new Vector<>();
        addReprintCard(str, card);

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
                cardDatabase.get(entry).isDoubleFaced = true;
            } else if (card.partIndex == 2) {
                card.isFlip = true;
                cardDatabase.get(entry).isFlip = true;
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

        card.reversed = (getEntry(str, "Reserved") != null);

        return card;
    }

    public void addReprintCard(String str, CardSimpleInfo card) {
        ReprintSimpleInfo reprint = new ReprintSimpleInfo();
        reprint.set = getEntry(str, "Set");
        reprint.number = getEntry(str, "No");
        reprint.flavor = getEntry(str, "Flavor");
        reprint.artist = getEntry(str, "Artist");
        reprint.rarity = getEntry(str, "Rarity");
        reprint.multiverseid = Integer.parseInt(getEntry(str, "Multiverseid"));
        reprint.rating = getEntry(str, "Rating");
        reprint.votes = getEntry(str, "Votes");
        for (String[] strs : SetList) {
            if (reprint.set.equals(strs[0])) {
                String s = reprint.number;
                if (s.charAt(s.length() - 1) >= 'a') {
                    if (s.length() == 2) {
                        s = "00" + s;
                    } else if (s.length() == 3) {
                        s = "0" + s;
                    }
                } else {
                    if (s.length() == 1) {
                        s = "00" + s;
                    } else if (s.length() == 2) {
                        s = "0" + s;
                    }
                }
                reprint.picture = prefix + strs[1] + "/" + s + ".jpg";
                File file = new File(reprint.picture);
                if (!file.exists()) {
                    reprint.picture = prefix + strs[1] + "/" + s + "a.jpg";
                }
                break;
            }
        }
        card.reprints.add(reprint);
    }

    public void processCard(String str) {
        String name = getEntry(str, "Name");

        if (cardDatabase.containsKey(name)) {
            addReprintCard(str, cardDatabase.get(name));
        } else {
            cardDatabase.put(name, getNewCard(getEntry(str, "Card")));
        }
    }

    public void processSet(File file) {
        System.out.println("process file " + file.getAbsolutePath());
        BufferedReader reader;
        try {
            reader = new BufferedReader(new FileReader(file));
            String str;
            String card = "";
            while ((str = reader.readLine()) != null) {
                if (str.equals("")) {
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
}
