package lu.cifer.mtgviewer;

public class LuaScript {

    static String output;

    static {
        System.loadLibrary("native-lib");
        initLua(MainActivity.SDPath + "/MTG/Script/global.lua");
    }

    static private void initCard(CardAnalyzer.CardInfo card, CardAnalyzer.ReprintInfo reprint) {
        luaPushString("name", card.name);
        luaPushString("simpleName", card.simpleName);
        luaPushStringArray("otherPart", card.otherPart.toArray(new String[card.otherPart.size()]));
        luaPushInteger("partIndex", card.partIndex);
        luaPushBoolean("isSplit", card.isSplit);
        luaPushBoolean("isDoubleFaced", card.isDoubleFaced);
        luaPushBoolean("isMDFC", card.isModalDoubleFaced);
        luaPushBoolean("isFlip", card.isFlip);
        luaPushBoolean("isAdventure", card.isAdventure);
        luaPushBoolean("isFun", card.isFun);
        luaPushBoolean("isLegendary", card.isLegendary);
        luaPushStringArray("types", card.types.toArray(new String[card.types.size()]));
        luaPushStringArray("subTypes", card.subTypes.toArray(new String[card.subTypes.size()]));
        luaPushStringArray("superTypes", card.superTypes.toArray(new String[card.superTypes.size()]));
        luaPushString("mana", card.mana);
        luaPushInteger("value", card.value);
        luaPushString("colorIndicator", card.colorIndicator);
        luaPushString("power", card.power);
        luaPushString("toughness", card.toughness);
        luaPushString("loyalty", card.loyalty);
        luaPushString("text", card.text);
        luaPushString("rules", card.rules);
        luaPushStringArray("legal", card.legal.toArray(new String[card.legal.size()]));
        luaPushStringArray("restricted", card.restricted.toArray(new String[card.restricted.size()]));
        luaPushStringArray("banned", card.banned.toArray(new String[card.banned.size()]));
        luaPushBoolean("reserved", card.reserved);
        luaPushInteger("reprintTimes", card.reprintTimes);

        luaPushInteger("multiverseId", reprint.multiverseId);
        luaPushString("set", reprint.set);
        luaPushString("code", reprint.code);
        luaPushString("folder", reprint.folder);
        luaPushString("number", reprint.number);
        luaPushString("flavor", reprint.flavor);
        luaPushString("artist", reprint.artist);
        luaPushString("rarity", reprint.rarity);
        luaPushString("watermark", reprint.watermark);
        luaPushString("picture", reprint.picture);
        luaPushInteger("sameIndex", reprint.sameIndex);
        luaPushString("formattedNumber", reprint.formattedNumber);
        luaPushInteger("setOrder", reprint.setOrder);
        luaPushInteger("reprintIndex", reprint.reprintIndex);
    }

    private static String composeScript(String script) {
        return "function checkCard()\n" + script + "\nend\nif(checkCard()) then\nresult = true\nelse result = false\nend";
    }

    public static int checkCard(CardAnalyzer.ReprintInfo reprint, String script) {
        initCard(reprint.card, reprint);
        output = reprint.picture + " -> " + runScript(composeScript(script), MainActivity.SDPath + "/MTG/Script/card.lua");
        return getResult();
    }

    public static String getOutput() {
        return output;
    }

    public static native void closeLua();

    public static native void initLua(String file);

    public static native String runScript(String code, String file);

    public static native void luaPushString(String key, String value);

    public static native void luaPushInteger(String key, int value);

    public static native void luaPushBoolean(String key, boolean value);

    public static native void luaPushFloat(String key, float value);

    public static native void luaPushStringArray(String key, String[] value);

    public static native int getResult();
}
