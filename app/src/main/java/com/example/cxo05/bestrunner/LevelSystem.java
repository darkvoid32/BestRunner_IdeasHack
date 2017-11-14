package com.example.cxo05.bestrunner;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * Created by zh on 11/11/17.
 */

public final class LevelSystem{

    private Context context;
    private SharedPreferences sharedPref;

    private final int LEVELUP = 5000;
    private final int WINEXP = 2500;
    private final int LOSSEXP = 1000;

    public LevelSystem(Context context){
        this.context = context;
        sharedPref = context.getSharedPreferences("Preferences", Context.MODE_PRIVATE);
    }

    public int getDistance(){
        return sharedPref.getInt("Distance", 5);
    }

    public int getWins(){
        return sharedPref.getInt("Wins",0);
    }

    public int getLoss(){
        return sharedPref.getInt("Losses",0);
    }

    public final class PlayerProgress {
        int level; int fexp; //floating experience points
        int wins; int losses; //sprint battle stats
        //int dedication_curr; int dedication_highest; //consecutive running days //currently unimplemented

        public void setFexp(int fexp) {
            this.fexp = fexp;
        }

        public void setLevel(int level) {
            this.level = level;
        }

        public int getFexp() {
            return fexp;
        }

        public int getLevel() {
            return level;
        }
    }

    public PlayerProgress getPlayerLevel(){
        PlayerProgress pL = new PlayerProgress();
        pL.setLevel(
                (getDistance() + WINEXP * getWins() + LOSSEXP * getLoss())/LEVELUP
        );
        pL.setFexp(
                (getDistance() + WINEXP * getWins() + LOSSEXP * getLoss())%LEVELUP
        );
        return pL;
    }
}
