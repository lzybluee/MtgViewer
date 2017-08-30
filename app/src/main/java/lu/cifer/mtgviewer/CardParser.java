package lu.cifer.mtgviewer;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Vector;

public class CardParser {

    public static String[][] DefaultSetList = {
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

            {"Media Inserts", "Special/MBP", "MBP"},

            {"Unhinged", "Unset/UNH", "UH"},
            {"Unglued", "Unset/UGL", "UG"},

            {"Modern Masters 2015 Edition", "Reprint/MM2", "MM2"},
            {"Modern Masters", "Reprint/MMA", "MMA"},
            {"Vintage Masters", "Reprint/VMA", "VMA"},
            {"Eternal Masters", "Reprint/EMA", "EMA"},

            {"Duel: Kiora vs Elspeth", "Reprint/DDO", "DDO"},
            {"Duel: Speed vs Cunning", "Reprint/DDN", "DDN"},
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

            {"Conspiracy Conspiracies", "Conspiracy/Conspiracy", "Conspiracy"},
            {"Archenemy Schemes", "Archenemy/Scheme", "Scheme"},
            {"Planechase 2012 Planes", "Planechase/Plane2012", "Plane2012"},
            {"Planechase Planes", "Planechase/Plane", "Plane"},
    };

    public static String[][] SetList;

    public static int rulePage = 0;
    public static String oracleFolder = "Oracle";

    private static File loadOracle(String set) {
        File file = new File(MainActivity.SDPath
                + "/MTG/" + oracleFolder + "/MtgOracle_" + set + ".txt");
        if (!file.exists()) {
            return new File(MainActivity.SDPath
                    + "/MTG/Oracle/MtgOracle_" + set + ".txt");
        }
        return file;
    }

    public static String getCardInfo(String url, boolean justRule) {
        String setInfo = "";
        boolean isModern = false;

        File file = null;
        int idx = 0;
        for (String[] strs : SetList) {
            int index;
            if ((index = url.lastIndexOf(strs[1] + "/")) >= 0 && !url.contains("/Misc/")) {
                if (url.substring(index + strs[1].length() + 1).contains("/")
                        && strs.length >= 4) {
                    file = loadOracle(strs[3]);
                    setInfo = strs[3].toLowerCase();
                    if (idx <= 48) {
                        isModern = true;
                    }
                } else {
                    file = loadOracle(strs[2]);
                    setInfo = strs[2].toLowerCase();
                    if (idx <= 48) {
                        isModern = true;
                    }
                }
                break;
            }
            idx++;
        }

        if (url.contains("/Conspiracy/")) {
            setInfo = "cns";
        }

        if (url.contains("/Special/") && !url.contains("/MBP/")) {
            return url;
        }

        if (file == null) {
            String s = url.substring(0, url.lastIndexOf("/"));
            s = s.substring(s.lastIndexOf("/") + 1);
            file = loadOracle(s);
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
            boolean isRule = false;
            boolean isBasic = false;
            while ((str = reader.readLine()) != null) {
                if (str.equals("<No>" + num + "</No>")) {
                    flag = true;
                    rating = "";
                    if (!justRule) {
                        if (setInfo.equals("")) {
                            MainActivity.urlInfo = "";
                        } else {
                            MainActivity.urlInfo = "http://magiccards.info/" + setInfo + "/en/" + num + ".html";
                        }
                    }
                    isRule = justRule;
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
                    if (!isRule && str.startsWith("<Name>")) {
                        isBasic = str.contains(">Plains<") || str.contains(">Island<") || str.contains(">Swamp<")
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
                    if (!isRule && str.startsWith("<ColorIndicator>")) {
                        card += "Color: " + str + "\n";
                        continue;
                    }
                    if (!isRule && str.startsWith("<OtherPart>")) {
                        card += "Other: " + str + "\n";
                        continue;
                    }
                    if (!str.startsWith("<Block>") && !str.startsWith("<Standard>")
                            && !str.startsWith("<Extended>")
                            && !str.startsWith("<Classic>")
                            && !str.startsWith("<Multiverseid>")) {
                        if (str.startsWith("<Rulings>")) {
                            isRule = !justRule;
                        }
                        if (!isRule) {
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
                                } else if (str.contains("Modern>Legal<") && !isModern && !isBasic) {
                                    card += "Legal in Modern\n";
                                }
                            } else {
                                card += str + "\n";
                            }
                        }
                        if (str.contains("</Rulings>")) {
                            isRule = justRule;
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

    public static void initOracle() {
        File file = new File(MainActivity.SDPath + "/MTG/Script/oracle.txt");
        if (file.exists()) {
            try {
                BufferedReader reader = new BufferedReader(new FileReader(file));
                String s = reader.readLine();
                if (new File(MainActivity.SDPath + "/MTG/" + s).exists()) {
                    oracleFolder = s;
                }
                reader.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        file = new File(MainActivity.SDPath + "/MTG/Script/list.txt");
        if (file.exists()) {
            try {
                BufferedReader reader = new BufferedReader(new FileReader(file));
                String s = null;
                Vector<String[]> vector = new Vector<>();
                while ((s = reader.readLine()) != null) {
                    s = s.trim();
                    if (s.isEmpty() || s.startsWith("#")) {
                        continue;
                    }
                    vector.add(s.split(","));
                }
                reader.close();
                SetList = new String[vector.size()][];
                for (int i = 0; i < vector.size(); i++) {
                    SetList[i] = vector.get(i);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            SetList = DefaultSetList;
        }
    }
}
