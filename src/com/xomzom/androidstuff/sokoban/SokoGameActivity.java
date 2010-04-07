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
import android.content.SharedPreferences.Editor;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
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
 * TODO: Split about to 'instructions' and 'about', and allow
 * internationalization.
 * TODO: Allow dragging.
 * TODO: Good images, and possibly themes.
 * TODO: Landscape thing.
 */
public class SokoGameActivity extends Activity 
    implements OnClickListener, OnSharedPreferenceChangeListener
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
     * The navigation bar 'Undo' button. See bellow in refreshPreferences for
     * explanation on why there are two undo buttons behaving exactly the same.
     * @see #refreshPreferences()
     */
    private ImageButton m_navUndoButton;
    
    /**
     * The standalone undo button.
     */
    private ImageButton m_standaloneUndoButton;

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
     * The 'undo' menu item.
     */
    private MenuItem m_undoMenuItem;

    /**
     * The list of moves done in the game.
     */
    private Vector<Move> m_moveList = new Vector<Move>();
    
    /**
     * The number of levels.
     */
    private int m_maxLevel;
    
    /**
     * The 'select level' dialog.
     */
    private SelectLevelDialog m_selectLevelDialog;
    
    /**
     * Our preference object, shared with the settings activity.
     */
    private SharedPreferences m_prefs;
    

    //
    // activity lifecycle.
    //

    /**
     * Create the game.
     */
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
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
        // TODO: Save the entire state. And use a bundle to do it.
        writeCurrentLevelNumber();
    }
    
    
    //
    // Operations.
    //

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
        setUndoButtonsState();
        setLevelButtonsState();
        if (m_gameView != null)
            m_gameView.invalidate();
        return true;
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
            setUndoButtonsState();
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
            setUndoButtonsState();
            m_gameView.invalidate();
        }
    }

    /**
     * Return the highest valid level number.
     * @return
     */
    public int getMaxLevel()
    {
        return m_maxLevel;
    }

    
    //
    // Events (non-lifecycle ones, that is).
    //
    
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
        m_nextLevelMenuItem = (MenuItem)menu.findItem(R.id.MENU_ITEM_NEXT);
        m_undoMenuItem = (MenuItem)menu.findItem(R.id.MENU_ITEM_UNDO);

        setUrlToShowOnMenuItem(menu, R.id.MENU_ITEM_ABOUT, R.string.ABOUT_URL);
        setUrlToShowOnMenuItem(menu, R.id.MENU_ITEM_LICENSE, R.string.GPL_URL);

        Intent intent = new Intent(this, SettingsActivity.class);
        setIntentForMenuItem(menu, R.id.MENU_ITEM_SETUP, intent);

        setUndoButtonsState();
        setLevelButtonsState();
        
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
          case R.id.MENU_ITEM_UNDO:
            SokoGameActivity.this.undoMove();
            return true;
          case R.id.MENU_ITEM_NEXT:
            advanceLevel();
            return true;
          case R.id.MENU_ITEM_PREV:
            setLevel(m_level - 1);
            return true;
          case R.id.MENU_ITEM_SELECT_LEVEL:
            doSelectLevelDialog();
            return true;
          case R.id.MENU_ITEM_RESTART:
            setLevel(m_level);
            return true;
          case R.id.MENU_ITEM_EXIT:
            writeCurrentLevelNumber();
            finish();
            return true;
        }
        return false;
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
            Log.d(this.getClass().getName(), "Moving to: " + moveDir);
            doMove(move);
            return true;
        }
        Log.d(this.getClass().getName(), "Unhandled key event.");
        return super.onKeyDown(keyCode, event);
    }

    /**
     * A button was clicked.
     */
    @Override
    public void onClick(View src)
    {
        if (src == m_navUndoButton || src == m_standaloneUndoButton)
            undoMove();
        else if (src == m_upButton)
            doMove(new Move(Move.DIR_UP));
        else if (src == m_downButton)
            doMove(new Move(Move.DIR_DOWN));
        else if (src == m_leftButton)
            doMove(new Move(Move.DIR_LEFT));
        else if (src == m_rightButton)
            doMove(new Move(Move.DIR_RIGHT));
    }

    /**
     * A notification that a preference was changed.
     */
    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences,
            String key)
    {
        // We could optimize by only looking at the preference that actually
        // changed, but what the hell...
        refreshPreferences();
    }

    /**
     * onCreateDialog event - forward requests to the factory.
     */
    @Override
    protected Dialog onCreateDialog(int id)
    {
        return DialogFactory.getInstance().getPendingDialog(id);
    }


    //
    // Helpers.
    //

    /**
     * Initialize the game.
     */
    private void init()
    {
        m_prefs = 
            PreferenceManager.getDefaultSharedPreferences(this);
        m_prefs.registerOnSharedPreferenceChangeListener(this);

        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);

        Resources res = getResources();
        m_maxLevel = res.getInteger(R.attr.NUM_LEVELS);
        m_statusView = (TextView)findViewById(R.id.status_view);
        m_navUndoButton = initButton(R.id.nav_undo_button);
        m_standaloneUndoButton = initButton(R.id.standalone_undo_button);
        m_upButton = initButton(R.id.up_button);
        m_downButton = initButton(R.id.down_button);
        m_leftButton = initButton(R.id.left_button);
        m_rightButton = initButton(R.id.right_button);

        initPreferencesDefault();
        refreshPreferences();

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
     * Helper method - associate a menu item (identified by an ID), with a
     * URL to show (identified by the resource ID of the string constant 
     * containing the actual URL).
     */
    private void setUrlToShowOnMenuItem(Menu menu, int menuItemId, int urlResId)
    {
        Intent intent = createShowHtmlIntent(urlResId);
        setIntentForMenuItem(menu, menuItemId, intent);
    }
    
    /**
     * Helper method - associate a menu item (identified by an ID), with an
     * intent to launch when the menu item is selected.
     */
    private void setIntentForMenuItem(Menu menu, int menuItemId, Intent intent)
    {
        MenuItem item = (MenuItem)menu.findItem(menuItemId);
        item.setIntent(intent);
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
    
    private void doSelectLevelDialog()
    {
        if (m_selectLevelDialog == null)
            m_selectLevelDialog = new SelectLevelDialog(this);

        DialogFactory.getInstance().doActivityDialog(this, m_selectLevelDialog);
    }

    /**
     * Enable/disable the undo state depending on whether there are moves
     * in the undo buffer.
     */
    private void setUndoButtonsState()
    {
        boolean enabled = !m_moveList.isEmpty();
        m_navUndoButton.setEnabled(enabled);
        m_standaloneUndoButton.setEnabled(enabled);
        
        if (m_undoMenuItem != null)
            m_undoMenuItem.setEnabled(enabled);
    }

    /**
     * Enable/disable the level select options according to the current level.
     */
    private void setLevelButtonsState()
    {
        // If the previous level item is null, the menu has not yet been 
        // initialized, so there's no need to set the menu options.
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
     * Helper method - create an intent to view the URL identified as a string
     * constant resource ID.
     * @param dialogUrlResId The resource ID of the string containing the URL
     */
    private Intent createShowHtmlIntent(int dialogUrlResId)
    {
        String uriString = getString(dialogUrlResId);
        Uri uri = Uri.parse(uriString);
        Intent intent = new Intent(this, HtmlResViewActivity.class);
        intent.setData(uri);
        return intent;
    }

    /**
     * Initialize preferences to default values based on hardware properties.
     * That is, default buttons nav buttons ot 'show' if there's a 
     * touchscreen and no dpad, default undo button to show if there's a 
     * touchscreen. In any case, if a preference has already been set, do
     * nothing.
     */
    private void initPreferencesDefault()
    {
        Configuration config = getResources().getConfiguration();
        
        boolean hasDpad = 
            (config.navigation == Configuration.NAVIGATION_DPAD);
        boolean hasTouchScreen = 
            (config.touchscreen != Configuration.TOUCHSCREEN_NOTOUCH);
        
        boolean showUndoButton = hasTouchScreen; 
        boolean showNavButtons = (hasTouchScreen && !hasDpad);
        setBoolPrefDefault(R.string.PREF_SHOW_NAV_BUTTONS_KEY, showNavButtons);
        setBoolPrefDefault(R.string.PREF_SHOW_UNDO_BUTTON_KEY, showUndoButton);
    }

    /**
     * Set a default value to the given boolean pref. If the pref is already
     * defined, do nothing.
     * 
     * @param keyStrId The resource ID of the key string.
     * @param value The default value to set.
     */
    private void setBoolPrefDefault(int keyStrId, boolean value)
    {
        String key = getString(keyStrId);
        if (!m_prefs.contains(key))
        {
            Editor prefEditor = m_prefs.edit();
            prefEditor.putBoolean(key, value);
            prefEditor.commit();
        }
    }

    /**
     * Read the preferences from the preference table.
     */
    private void refreshPreferences()
    {
        boolean showNavButtonsPref = 
            getBoolPrefByKeyID(R.string.PREF_SHOW_NAV_BUTTONS_KEY, false);
        boolean showUndoButtonPref = 
            getBoolPrefByKeyID(R.string.PREF_SHOW_UNDO_BUTTON_KEY, false);

        View navButtonView = findViewById(R.id.nav_button_view);

        m_standaloneUndoButton.setVisibility(View.GONE);
        m_navUndoButton.setVisibility(View.INVISIBLE);
        navButtonView.setVisibility(View.GONE);

        // The trick is, we have two undo buttons, one inside the nav view, and
        // one right besides it. we show either the standalone view or the
        // stand-alone undo button (or neither), but never borth of them 
        // together.
        if (showNavButtonsPref)
        {
            navButtonView.setVisibility(View.VISIBLE);
            if (showUndoButtonPref)
                m_navUndoButton.setVisibility(View.VISIBLE);
        }
        else
        {
            if (showUndoButtonPref) 
            {
                m_standaloneUndoButton.setVisibility(View.VISIBLE);
            }
        }
        if (m_gameView != null)
            m_gameView.invalidate();
    }

    /**
     * Helper method: get a boolean preference, identified by it's key 
     * string ID. If the preference is not defined, return the default value.
     * 
     * @param keyStrId The resource ID of the string identifying the key.
     * @param defValue The default value for this field.
     */
    private boolean getBoolPrefByKeyID(int keyStrId, boolean defValue)
    {
        String key = getString(keyStrId);
        return m_prefs.getBoolean(key, defValue);
    }
}