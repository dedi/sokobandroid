/*  sokoban - a Sokoban game for midp-supporting mobile devices
 *  Copyright (C) 2007,2009 Dedi Hirschfeld
 *
 *  This program is free software; you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation; either version 2 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program; if not, write to the Free Software
 *  Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301  USA
 */


package com.xomzom.androidstuff.sokoban;

import java.io.IOException;
import java.util.Vector;

import android.app.Activity;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageButton;
import android.widget.TextView;


/**
 * The Sokban game activity.
 * 
 * @author Dedi Hirschfeld
 * 
 * TODO: Allow dragging.
 * TODO: Good images, and possibly themes.
 * TODO: Fix layout.
 * TODO: Remove unwanted prefrences.
 * TODO: Naming convention for constants, ids and strings.
 * TODO: Why is the buttons orange? and why is the 'up' button selected when
 * I use the trackball?
 * TODO: Simplify menu using intents.
 * 
 */
public class SokoGameActivity extends Activity implements OnClickListener
{
    //
    // Constants.
    //
    
    /**
     * The name of the 'level' key in the preferences.
     */
    private final static String LEVEL_PREF_NAME = "CURRENT_LEVEL";

    //
    // Members.
    //
    
    /**
     * The current level.
     */
    private int m_level = 1;
    
    /**
     * The game board.
     */
    private Board m_board = new Board();
    
    /**
     * The gameboard view.
     */
    private SokoView m_gameView;
    
    /**
     * The status view.
     */
    private TextView m_statusView;
    
    /**
     * The 'Undo' button.
     */
    private ImageButton m_undoButton;

    /**
     * The 'Up' button.
     */
    private ImageButton m_upButton;

    /**
     * The 'Down' button.
     */
    private ImageButton m_downButton;

    /**
     * The 'Left' button.
     */
    private ImageButton m_leftButton;

    /**
     * The 'Right' button.
     */
    private ImageButton m_rightButton;
    
    /**
     * The 'previous level' menu item.
     */
    private MenuItem m_previousLevelMenuItem;
    
    /**
     * The 'next level' menu item.
     */
    private MenuItem m_nextLevelMenuItem;

    /**
     * The list of moves done in the game.
     */
    private Vector<Move> m_moveList = new Vector<Move>();
    
    /**
     * The 'require trackball press' preference.
     */
    private boolean m_prefRequireTBPress;
    
    /**
     * The number of levels.
     */
    private int m_maxLevel;
    
    /**
     * The 'select level' dialog.
     */
    private SelectLevelDialog m_selectLevelDialog; 
    
    //
    // Operations.
    //

    /**
     * Create the game.
     */
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        refreshPreferences();
        init();
    }
    
    /**
     * We're going to pause. Write the level number, since we might be 
     * killed at any moment.
     */
    @Override
    public void onPause()  
    {
        super.onPause();
        writeCurrentLevelNumber();
    }

    /**
     * Create the options menu.
     * 
     * @see android.app.Activity#onCreateOptionsMenu(android.view.Menu)
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        super.onCreateOptionsMenu(menu);
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.mainmenu, menu);
        
        m_previousLevelMenuItem = (MenuItem)menu.findItem(R.id.MENU_ITEM_PREV);
        assert(m_previousLevelMenuItem != null);
        
        m_nextLevelMenuItem = (MenuItem)menu.findItem(R.id.MENU_ITEM_NEXT);
        assert(m_nextLevelMenuItem != null);
        
        setMenuButtonsState();
        
        return true;
    }

    /**
     * onCreateDialog event - forward requests to the factory.
     */
    @Override
    protected Dialog onCreateDialog(int id)
    {
        return DialogFactory.getInstance().getPendingDialog(id);
    }

    /**
     * Display a 'couldn't load level' error message.
     * @param level The level that failed to load.
     */
    private void doLevelLoadError(int level)
    {
        String errMsg = getString(R.string.ERR_LEVEL_LOAD, level);
        String okButtonCaption = getString(R.string.OK_BUTTON_CAPTION);
        DialogFactory dialogFactory = DialogFactory.getInstance();
        DialogInterface.OnClickListener dismissAction = 
            dialogFactory.getDismissHandler();
        dialogFactory.messageBox(this, errMsg, okButtonCaption, dismissAction);
    }

    /**
     * Helper method - get a button identified by an ID, and set it's click
     * listener while at it.
     */
    private ImageButton initButton(int id)
    {
        ImageButton button =  (ImageButton)findViewById(id);
        button.setOnClickListener(this);
        return button;
    }
    
    /**
     * Initialize the game.
     */
    private void init()
    {
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);

        Resources res = getResources();
        m_maxLevel = res.getInteger(R.attr.num_levels);
        m_statusView = (TextView)findViewById(R.id.status_view);
        m_undoButton = initButton(R.id.undo_button);
        m_upButton = initButton(R.id.up_button);
        m_downButton = initButton(R.id.down_button);
        m_leftButton = initButton(R.id.left_button);
        m_rightButton = initButton(R.id.right_button);

        int level = readCurrentLevelNumber();
        if (!setLevel(level))
        {
            if (!setLevel(1))
                finish();
        }

        m_gameView = (SokoView)findViewById(R.id.game_view);
        assert(m_gameView != null);
        m_gameView.setGame(this);
    }
    
    /**
     * Advance one level.
     */
    public void advanceLevel()
    {
        setLevel(m_level + 1);
    }
    
    /**
     * Set the level.
     * 
     * @param level, the new level (or offset).
     * @return true if the level was read successfully, false otherwise. If the
     * level was not read, an error has already been displayed.
     */
    public boolean setLevel(int newLevel)
    {
        try
        {
            m_board.read(newLevel, getAssets());
        }
        catch (IOException e)
        {
            Log.e(this.getClass().toString(), Log.getStackTraceString(e));
            doLevelLoadError(newLevel);
            return false;
        }
        m_level = newLevel;
        String statusText = getString(R.string.LEVEL_TEXT, m_level);
        m_statusView.setText(statusText);
        m_moveList.removeAllElements();
        setUndoButtonState();
        setMenuButtonsState();
        if (m_gameView != null)
            m_gameView.invalidate();
        return true;
    }

    /**
     * An options menu item was selected.
     * @see android.app.Activity#onOptionsItemSelected(android.view.MenuItem)
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        switch (item.getItemId())
        {
          case R.id.MENU_ITEM_NEXT:
            advanceLevel();
            return true;
          case R.id.MENU_ITEM_PREV:
            setLevel(m_level - 1);
            return true;
          case R.id.MENU_ITEM_SELECT_LEVEL:
            doSelectLevelDialog();
            return true;
          case R.id.MENU_ITEM_SETUP:
            doSetupActivity();
            return true;
          case R.id.MENU_ITEM_RESTART:
            setLevel(m_level);
            return true;
          case R.id.MENU_ITEM_ABOUT:
            doAbout();
            return true;
          case R.id.MENU_ITEM_LICENSE:
            doShowLicense();
            return true;
          case R.id.MENU_ITEM_EXIT:
            writeCurrentLevelNumber();
            finish();
            return true;
        }
        return false;
    }

    private void doSelectLevelDialog()
    {
        if (m_selectLevelDialog == null)
            m_selectLevelDialog = new SelectLevelDialog(this);

        DialogFactory.getInstance().doActivityDialog(this, m_selectLevelDialog);
    }

    /**
     * Start the setup activity.
     */
    private void doSetupActivity()
    {
        Intent intent = new Intent(this, SettingsActivity.class);
        startActivity(intent);
    }

    /**
     * Get the current level.
     * @return The current level.
     */
    public int getLevel()
    {
        return m_level;
    }

    /**
     * Get the board object for this game.
     * @return A board object.
     */
    public Board getBoard()
    {
        return m_board;
    }

    /**
     * Perform a game move.
     * @param move The move to perform.
     */
    public void doMove(Move move)
    {
        boolean moveOk = m_board.move(move);
        if (moveOk)
        {
            m_moveList.addElement(move);
            setUndoButtonState();
            m_gameView.invalidate();
        }
        if (m_board.isSolved())
        {
            advanceLevel();
        }
    }

    /**
     * Undo the last move done.
     */
    public void undoMove()
    {
        if (m_moveList.size() > 0)
        {
            Move move = m_moveList.lastElement();
            m_moveList.setSize(m_moveList.size() - 1);
            m_board.undoMove(move);
            setUndoButtonState();
            m_gameView.invalidate();
        }
    }

    /**
     * Enable/disable the undo state depending on whether there are moves
     * in the undo buffer.
     */
    private void setUndoButtonState()
    {
        m_undoButton.setEnabled(!m_moveList.isEmpty());
    }

    /**
     * Enable/disable the menu buttons according to the current level.
     */
    private void setMenuButtonsState()
    {
        Log.d("Dedi", "m_previousLevelMenuItem = " + m_previousLevelMenuItem + 
                ", m_nextLevelMenuItem = " + m_nextLevelMenuItem + 
                ", m_level = " + m_level);

        // If the previous level item is null, the menu has not yet been 
        // initialized.
        if (m_previousLevelMenuItem == null)
            return;
        
        m_previousLevelMenuItem.setEnabled(m_level > 1);
        m_nextLevelMenuItem.setEnabled(m_level < m_maxLevel);
    }

    /**
     * Read the level from the store.
     */
    private int readCurrentLevelNumber()
    {
        SharedPreferences preferences = getPreferences(MODE_PRIVATE);
        return preferences.getInt(LEVEL_PREF_NAME, 1);
    }
    
    /**
     * Write the level to the store. 
     */
    private void writeCurrentLevelNumber()
    {
        SharedPreferences preferences = getPreferences(MODE_PRIVATE);
        SharedPreferences.Editor prefEditor = preferences.edit();
        prefEditor.putInt(LEVEL_PREF_NAME, m_level);
        prefEditor.commit();
    }
    
    /**
     * Helper method: open a dialog with the given URL (identified as a 
     * string constant resource ID)
     */
    private void doHtmlDocDialog(int dialogUrlResId)
    {
        String uriString = getString(dialogUrlResId);
        Uri uri = Uri.parse(uriString);
        Intent intent = new Intent(this, HtmlResViewActivity.class);
        intent.setData(uri);
        startActivity(intent);
    }

    /**
     * Show the 'about' dialog.
     */
    private void doAbout()
    {
        doHtmlDocDialog(R.string.about_url);
    }

    /**
     * Show the 'license' dialog.
     */
    private void doShowLicense()
    {
        doHtmlDocDialog(R.string.gpl_url);
    }
    
    /**
     * A keyboard move event.
     */
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event)
    {
        int moveDir = -1;
        Log.d("Sokoban", "Got keycode: " + keyCode);
        switch (keyCode)
        {
            case KeyEvent.KEYCODE_DPAD_UP:
                moveDir = Move.DIR_UP;
                break;
            case KeyEvent.KEYCODE_DPAD_DOWN:
                moveDir = Move.DIR_DOWN;
                break;
            case KeyEvent.KEYCODE_DPAD_LEFT:
                moveDir = Move.DIR_LEFT;
                break;
            case KeyEvent.KEYCODE_DPAD_RIGHT:
                moveDir = Move.DIR_RIGHT;
                break;
        }
        if (moveDir != -1)
        {
            Move move = new Move(moveDir);
            Log.d("Sokoban", "Moving to: " + moveDir);
            doMove(move);
            return true;
        }
        Log.d("Sokoban", "Unhandled key event.");
        return super.onKeyDown(keyCode, event);
    }

    @Override
    public void onClick(View src)
    {
        if (src == m_undoButton)
            SokoGameActivity.this.undoMove();
        else if (src == m_upButton)
            doMove(new Move(Move.DIR_UP));
        else if (src == m_downButton)
            doMove(new Move(Move.DIR_DOWN));
        else if (src == m_leftButton)
            doMove(new Move(Move.DIR_LEFT));
        else if (src == m_rightButton)
            doMove(new Move(Move.DIR_RIGHT));
    }

    /* (non-Javadoc)
     * @see android.app.Activity#onConfigurationChanged(android.content.res.Configuration)
     */
    @Override
    public void onConfigurationChanged(Configuration newConfig)
    {
        super.onConfigurationChanged(newConfig);
        if (m_gameView != null)
            m_gameView.invalidate();
    }

    /**
     * Read  the preferences from the preference table.
     */
    private void refreshPreferences()
    {
        m_prefRequireTBPress = 
            getBoolPrefByKeyID(R.string.require_trackball_press_key,
                    R.attr.require_trackball_press_def_value);
    }

    /**
     * Helper method: get a string preference, identified by it's key string ID.
     * 
     * @param keyStrId The resource ID of the string identifying the key.
     * @param defValueResId The resource ID of the string specifying the 
     * default value.
     */
    private Boolean getBoolPrefByKeyID(int keyStrId, int defValueResId)
    {
        SharedPreferences prefs = 
            PreferenceManager.getDefaultSharedPreferences(getBaseContext());

        String key = getString(keyStrId);
        if (key == null || prefs == null)
            return null;
        Resources res = getResources();
        Boolean defaultValue = res.getBoolean(defValueResId);

        return prefs.getBoolean(key, defaultValue);
    }

    /**
     * Return the highest valid level number.
     * @return
     */
    public int getMaxLevel()
    {
        return m_maxLevel;
    }
}