package lu.cifer.mtgviewer;

public class LuaScript {

    static String output;

    static {
        System.loadLibrary("native-lib");
        initLua(MainActivity.SDPath + "/MTG/Script/global.lua");
    }

    static private void initCard(CardAnalyzer.CardInfo card, CardAnalyzer.ReprintInfo reprint) {
        luaPushString("name", card.name);
        luaPushString("otherPart", card.otherPart);
        luaPushInteger("partIndex", card.partIndex);
        luaPushBoolean("isSplit", card.isSplit);
        luaPushBoolean("isDoubleFaced", card.isDoubleFaced);
        luaPushBoolean("isFlip", card.isFlip);
        luaPushBoolean("isFun", card.isFun);
        luaPushBoolean("isInCore", card.isInCore);
        luaPushBoolean("isLegendary", card.isLegendary);
        luaPushStringArray("types", card.types.toArray(new String[card.types.size()]));
        luaPushStringArray("subTypes", card.subTypes.toArray(new String[card.subTypes.size()]));
        luaPushStringArray("superTypes", card.superTypes.toArray(new String[card.superTypes.size()]));
        luaPushString("mana", card.mana);
        luaPushInteger("converted", card.converted);
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
        luaPushBoolean("rarityChanged", card.rarityChanged);
        luaPushInteger("reprintTimes", card.reprintTimes);

        luaPushInteger("multiverseid", reprint.multiverseid);
        luaPushFloat("rating", reprint.rating);
        luaPushInteger("votes", reprint.votes);
        luaPushString("set", reprint.set);
        luaPushString("code", reprint.code);
        luaPushString("folder", reprint.folder);
        luaPushString("altCode", reprint.altCode);
        luaPushString("number", reprint.number);
        luaPushString("flavor", reprint.flavor);
        luaPushString("artist", reprint.artist);
        luaPushString("rarity", reprint.rarity);
        luaPushString("watermark", reprint.watermark);
        luaPushString("specialType", reprint.specialType);
        luaPushString("picture", reprint.picture);
        luaPushInteger("sameIndex", reprint.sameIndex);
        luaPushString("formatedNumber", reprint.formatedNumber);
        luaPushInteger("order", reprint.order);
        luaPushInteger("reprintIndex", reprint.reprintIndex);
        luaPushBoolean("latest", reprint.latest);
    }

    private static String composeScript(String script) {
        return "function checkCard()\n" + script + "\nend\nif(checkCard()) then\nresult = true\nelse result = false\nend";
    }

    public static int checkCard(CardAnalyzer.CardInfo card, CardAnalyzer.ReprintInfo reprint, String script) {
        initCard(card, reprint);
        output = runScript(composeScript(script), MainActivity.SDPath + "/MTG/Script/card.lua");
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
