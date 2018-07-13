package io.branch.branchster;

import io.branch.referral.Branch;
import io.branch.referral.BranchApp;

public class MonsterApplication extends BranchApp{
    @Override
    public void onCreate() {
        super.onCreate();

        // Branch logging for debugging
        Branch.enableLogging();

        // Branch object initialization
        Branch.getAutoInstance(this);
    }
}
