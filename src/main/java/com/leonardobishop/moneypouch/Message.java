package com.leonardobishop.moneypouch;

public enum Message {

    FULL_INV("full-inv", "&c%player%'s inventory is full!"),
    GIVE_ITEM("give-item", "&6Given &e%player% %item%&6."),
    RECEIVE_ITEM("receive-item", "&6You have been given %item%&6."),
    PRIZE_MESSAGE("prize-message", "&6You have received &c%prefix%%prize%%suffix%&6!"),
    ALREADY_OPENING("already-opening", "&cPlease wait for your current pouch opening to complete first!");

    private final String id;
    private final String def; // (default message if undefined)

    Message(String id, String def) {
        this.id = id;
        this.def = def;
    }

    public String get() {
        return StringUtil.color(PouchPlugin.getInstance().getConfig().getString("messages." + getId(), getDef()));
    }

    public String getId() {
        return id;
    }

    public String getDef() {
        return def;
    }
}
