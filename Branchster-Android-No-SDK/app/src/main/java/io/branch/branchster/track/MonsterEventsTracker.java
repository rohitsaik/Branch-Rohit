package io.branch.branchster.track;

import android.content.Context;

import org.json.JSONObject;

import java.util.Map;

import io.branch.branchster.event.Event;
import io.branch.referral.Branch;
import io.branch.referral.util.BranchEvent;

public class MonsterEventsTracker {

    public static void track(Context context, Event event, Map<String, String> monsterMetaData){
        Branch branch = Branch.getInstance(context);
            //or
        BranchEvent branchEvent = new BranchEvent(event.getEvent());

        if(null == monsterMetaData){
            branch.userCompletedAction(event.getEvent());
                //or
            branchEvent.logEvent(context);
        }else{
            branch.userCompletedAction(event.getEvent(), new JSONObject(monsterMetaData));
                //or
            for (Map.Entry<String, String> entry : monsterMetaData.entrySet()) {
                branchEvent.addCustomDataProperty(entry.getKey(), entry.getValue());
            }
            branchEvent.logEvent(context);
        }
    }
}
