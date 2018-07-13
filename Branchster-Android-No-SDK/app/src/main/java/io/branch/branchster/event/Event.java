package io.branch.branchster.event;

public enum Event {
    MONSTER_EDIT("monster_edit"),
    MONSTER_VIEW("monster_view"),
    MONSTER_SHARE_SUCCESS("monster_share_success"),
    DEEP_LINK_RECEIVED("deep_link_received"),
    SPLASH_ACTIVTY_STARTED("splash_activity_started");

    private String event;

    Event(String event){
        this.event = event;
    }

    public String getEvent(){
        return this.event;
    }
}
