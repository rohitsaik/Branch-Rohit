package io.branch.branchster;

import android.app.AlertDialog;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.content.ContextCompat;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.TextView;
import android.widget.Toast;

import java.util.HashMap;
import java.util.Map;

import io.branch.branchster.fragment.InfoFragment;
import io.branch.branchster.track.MonsterEventsTracker;
import io.branch.branchster.util.MonsterImageView;
import io.branch.branchster.util.MonsterObject;
import io.branch.branchster.util.MonsterPreferences;
import io.branch.indexing.BranchUniversalObject;
import io.branch.referral.Branch;
import io.branch.referral.BranchError;
import io.branch.referral.SharingHelper;
import io.branch.referral.util.ContentMetadata;
import io.branch.referral.util.LinkProperties;
import io.branch.referral.util.ShareSheetStyle;

import static io.branch.branchster.event.Event.MONSTER_SHARE_SUCCESS;
import static io.branch.branchster.event.Event.MONSTER_VIEW;

public class MonsterViewerActivity extends FragmentActivity implements InfoFragment.OnFragmentInteractionListener {
    static final int SEND_SMS = 12345;

    private static String TAG = MonsterViewerActivity.class.getSimpleName();
    public static final String MY_MONSTER_OBJ_KEY = "my_monster_obj_key";

    TextView monsterUrl;
    View progressBar;

    MonsterImageView monsterImageView_;
    MonsterObject myMonsterObject;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_monster_viewer);

        MonsterEventsTracker.track(getApplicationContext(), MONSTER_VIEW, null);

        monsterImageView_ = (MonsterImageView) findViewById(R.id.monster_img_view);
        monsterUrl = (TextView) findViewById(R.id.shareUrl);
        progressBar = findViewById(R.id.progress_bar);

        // Change monster
        findViewById(R.id.cmdChange).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(getApplicationContext(), MonsterCreatorActivity.class);
                startActivity(i);
                finish();
            }
        });

        // More info
        findViewById(R.id.infoButton).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                FragmentManager fm = getFragmentManager();
                FragmentTransaction ft = fm.beginTransaction();
                InfoFragment infoFragment = InfoFragment.newInstance();
                ft.replace(R.id.container, infoFragment).addToBackStack("info_container").commit();
            }
        });

        //Share monster
        findViewById(R.id.share_btn).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                shareMyMonster();
            }
        });

        initUI();
    }

    @Override
    protected void onResume(){
        super.onResume();
        MonsterEventsTracker.track(getApplicationContext(), MONSTER_VIEW, myMonsterObject.monsterMetaData());
    }

    private void initUI() {
        myMonsterObject = getIntent().getParcelableExtra(MY_MONSTER_OBJ_KEY);

        if (myMonsterObject != null) {
            String monsterName = getString(R.string.monster_name);

            MonsterPreferences monsterPreferences = MonsterPreferences.getInstance(getApplicationContext());
            final Map<String, String> branchDict = new HashMap<>();
            branchDict.put(MonsterPreferences.KEY_BODY_INDEX, monsterPreferences.getBodyIndex()+"");
            branchDict.put(MonsterPreferences.KEY_COLOR_INDEX, monsterPreferences.getColorIndex()+"");
            branchDict.put(MonsterPreferences.KEY_FACE_INDEX, monsterPreferences.getFaceIndex()+"");
            branchDict.put(MonsterPreferences.KEY_MONSTER_DESCRIPTION, monsterPreferences.getMonsterDescription()+"");
            branchDict.put(MonsterPreferences.KEY_MONSTER_NAME, monsterPreferences.getMonsterName()+"");
            branchDict.putAll(myMonsterObject.prepareBranchDict());

            ContentMetadata contentMetadata = new ContentMetadata();
            for (Map.Entry<String, String> entry : branchDict.entrySet()) {
                contentMetadata.addCustomMetadata(entry.getKey(), entry.getValue());
            }
            BranchUniversalObject branchUniversalObject = new BranchUniversalObject()
                    .setTitle("My Avatar/Monster")
                    .setContentDescription("The mighty gansta")
                    .setContentIndexingMode(BranchUniversalObject.CONTENT_INDEX_MODE.PUBLIC)
                    .setContentMetadata(contentMetadata);

            LinkProperties linkProperties = new LinkProperties()
                    .setChannel("sms")
                    .setFeature("sharing");

            if (!TextUtils.isEmpty(myMonsterObject.getMonsterName())) {
                monsterName = myMonsterObject.getMonsterName();
            }

            ((TextView) findViewById(R.id.txtName)).setText(monsterName);
            String description = MonsterPreferences.getInstance(this).getMonsterDescription();

            if (!TextUtils.isEmpty(myMonsterObject.getMonsterDescription())) {
                description = myMonsterObject.getMonsterDescription();
            }

            ((TextView) findViewById(R.id.txtDescription)).setText(description);

            // set my monster image
            monsterImageView_.setMonster(myMonsterObject);

            branchUniversalObject.generateShortUrl(getApplicationContext(), linkProperties, new Branch.BranchLinkCreateListener() {
                @Override
                public void onLinkCreate(String url, BranchError error) {
                    if(error == null){
                        Log.i("short url", url);
                        monsterUrl.setText(url);
                    }else{
                        Log.i("Branch SDK", error.toString());
                    }
                    progressBar.setVisibility(View.GONE);
                }
            });
        } else {
            Log.e(TAG, "Monster is null. Unable to view monster");
        }
    }

    /**
     * Method to share my custom monster with sharing with Branch Share sheet
     */
    private void shareMyMonster() {
        progressBar.setVisibility(View.VISIBLE);

        ContentMetadata contentMetadata = new ContentMetadata();
        Map<String, String> branchDict = myMonsterObject.prepareBranchDict();

        for (Map.Entry<String, String> entry : branchDict.entrySet()) {
            contentMetadata.addCustomMetadata(entry.getKey(), entry.getValue());
        }

        BranchUniversalObject branchUniversalObject = new BranchUniversalObject()
//                .setCanonicalIdentifier("rohitksai/12345")
                .setTitle("My Avatar/Monster")
                .setContentDescription("The mighty gansta")
                .setContentIndexingMode(BranchUniversalObject.CONTENT_INDEX_MODE.PUBLIC)
                .setContentMetadata(contentMetadata);

        LinkProperties linkProperties = new LinkProperties()
                .setChannel("sms")
                .setFeature("sharing");

        branchUniversalObject.generateShortUrl(getApplicationContext(), linkProperties, new Branch.BranchLinkCreateListener() {
            @Override
            public void onLinkCreate(String url, BranchError error) {
                if(error == null){
                    Log.i("Deep link url", url);

                    Intent i = new Intent(Intent.ACTION_SEND);
                    i.setType("text/plain");
                    i.putExtra(Intent.EXTRA_TEXT, String.format("Check out my Branchster named %s at %s", myMonsterObject.getMonsterName(), url));
                    startActivityForResult(i, SEND_SMS);
                }else{
                    Log.i("Branch SDK", error.toString());
                }
            }
        });

        progressBar.setVisibility(View.GONE);
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (SEND_SMS == requestCode) {
            if (RESULT_OK == resultCode) {
                MonsterEventsTracker.track(getApplicationContext(), MONSTER_SHARE_SUCCESS, null);
            }
        }
    }

    @Override
    public void onBackPressed() {
        FragmentManager fm = getFragmentManager();
        if (fm.getBackStackEntryCount() > 0) {
            fm.popBackStack();
        } else {
            new AlertDialog.Builder(this)
                    .setTitle("Exit")
                    .setMessage("Are you sure you want to exit?")
                    .setNegativeButton(android.R.string.no, null)
                    .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            finish();
                        }
                    }).create().show();
        }
    }


    @Override
    public void onFragmentInteraction() {
        //no-op
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
        initUI();
    }
}
