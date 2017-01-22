package lu.cifer.mtgviewer;

public class LuaScript {

    static {
        System.loadLibrary("native-lib");
        initLua();
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
    }

    public static boolean checkCard(CardAnalyzer.CardInfo card, CardAnalyzer.ReprintInfo reprint) {
        initCard(card, reprint);
        System.out.println("lzy:" + card.name);
        String result = runScript("print('lzy lua:' .. name)");
        return true;
    }

    public static native void initLua();

    public static native String runScript(String code);

    public static native void luaPushString(String key, String value);

    public static native void luaPushInteger(String key, int value);

    public static native void luaPushBoolean(String key, boolean value);

    public static native void luaPushFloat(String key, float value);

    static private void luaPushStringArray(String key, String[] value) {

    }
}
