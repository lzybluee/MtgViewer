package lu.cifer.mtgviewer;

import android.util.Log;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CardParser {

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
        boolean isModern = false;

        File file = null;
        for (String[] strs : SetList) {
            if (url.lastIndexOf(strs[1] + "/") >= 0 && !url.contains("/Misc/")) {
                file = loadOracle(strs[2]);
                if (strs[1].startsWith("Modern/")) {
                    isModern = true;
                }
                break;
            }
        }

        if (file == null) {
            String s = url.substring(0, url.lastIndexOf("/"));
            s = s.substring(s.lastIndexOf("/") + 1);
            file = loadOracle(s);
        }

        if (file != null && !file.exists()) {
            return url;
        }

        String fileName = url.substring(url.lastIndexOf("/") + 1, url.lastIndexOf("."));
        Pattern pattern = Pattern.compile("([^\\d]*)0*(\\d+)([^\\d]*)");
        Matcher matcher = pattern.matcher(fileName);

        String num = "";
        if (matcher.find()) {
            num = matcher.group(1) + matcher.group(2) + matcher.group(3);
        } else {
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
                        if(str.contains("]<")) {
                            str = str.replaceAll(" \\[(\\w)\\]</Name>", "</Name>\n<Variant>(Variant $1)</Variant>");
                        }
                    }
                    if (str.startsWith("<Multiverseid>")) {
                        String id = str.replaceAll("<Multiverseid>", "")
                                .replaceAll("</Multiverseid>", "");
                        if (!id.equals("0")) {
                            if (!justRule) {
                                MainActivity.urlInfo = "http://gatherer.wizards.com/Pages/Card/Discussion.aspx?multiverseid=" + id;
                            } else {
                                MainActivity.urlInfo = "http://gatherer.wizards.com/Pages/Card/Details.aspx?multiverseid=" + id;
                            }
                        } else {
                            MainActivity.urlInfo = "";
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
                    if (!isRule && str.startsWith("<Hand>")) {
                        card += "Hand Modifier: " + str + "\n";
                        continue;
                    }
                    if (!isRule && str.startsWith("<Life>")) {
                        card += "Life Modifier: " + str + "\n";
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
                                } else if (str.contains("Modern>Legal<") && !isModern && !isBasic && !url.contains("/Misc/")) {
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

        return card.trim();
    }

    public static void initOracle() {
        File file = new File(MainActivity.SDPath + "/MTG/Script/list.txt");
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
        }
    }

    public static boolean containsCode(String code) {
        for(String[] strs : SetList) {
            if(strs[2].equals(code)) {
                return true;
            }
        }
        return false;
    }
}
