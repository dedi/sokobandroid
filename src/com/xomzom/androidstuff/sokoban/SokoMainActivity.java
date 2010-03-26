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

import java.io.File;
import java.io.IOException;
import java.text.FieldPosition;
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
import android.webkit.WebView;
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
 */
public class SokoMainActivity extends Activity implements OnClickListener
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
     * The list of moves done in the game.
     */
    private Vector<Move> m_moveList = new Vector<Move>();
    
    /**
     * The 'require trackball press' preference.
     */
    private boolean m_prefRequireTBPress;
    
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
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.mainmenu, menu);
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
     * Display the given error alert, and exit when the user clicks OK.
     */
    private void alertAndExit(String text)
    {
        DialogFactory dialogFactory = DialogFactory.getInstance();
        DialogInterface.OnClickListener exitAction = 
            new DialogInterface.OnClickListener() {
              public void onClick(DialogInterface dialog, int id) {
                  writeCurrentLevelNumber();
                  finish();
              }
          };
          
        dialogFactory.messageBox(this, text, 
                getText(R.string.OK_BUTTON_CAPTION), exitAction);
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
        m_statusView = (TextView)findViewById(R.id.status_view);
        m_undoButton = initButton(R.id.undo_button);
        m_upButton = initButton(R.id.up_button);
        m_downButton = initButton(R.id.down_button);
        m_leftButton = initButton(R.id.left_button);
        m_rightButton = initButton(R.id.right_button);

        
        int level = readCurrentLevelNumber();
        setLevel(level, false);
        
        m_gameView = (SokoView)findViewById(R.id.game_view);
        assert(m_gameView != null);
        m_gameView.setGame(this);
    }
    
    /**
     * Set the level. This can be used to specify an absolute level, or an
     * offset relative to the current level. (So you could, for example go
     * to the previous level by calling setLevel(-1, true).
     * 
     * @param level, the new level (or offset).
     * @param isOffset true if the level argument should be treated as an 
     * offset relative to the current one. 
     */
    public void setLevel(int newLevel, boolean isOffset)
    {
        if (isOffset)
        {
            newLevel += m_level;
        }
        
        try
        {
            m_board.read(newLevel, getAssets());
        }
        catch (IOException e)
        {
            e.printStackTrace();
            String errMsg = getString(R.string.ERR_LEVEL_LOAD, m_level);
            alertAndExit(errMsg);
            // We should never really get here.
            assert(false);
            return;
        }
        m_level = newLevel;
        String statusText = getString(R.string.LEVEL_TEXT, m_level);
        m_statusView.setText(statusText);
        m_moveList.removeAllElements();
        setUndoButtonState();
        if (m_gameView != null)
            m_gameView.invalidate();
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
            setLevel(1, true);
            return true;
          case R.id.MENU_ITEM_PREV:
            setLevel(-1, true);
            return true;
          case R.id.MENU_ITEM_SELECT_LEVEL:
            // TODO: Do select level dialog.
            return false;
          case R.id.MENU_ITEM_SETUP:
            doSetupActivity();
            return true;
          case R.id.MENU_ITEM_RESTART:
            setLevel(m_level, false);
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
            setLevel(1, true);
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
        WebView webView = new WebView(this);
        String url = getString(dialogUrlResId);
        webView.loadUrl(url);
        CharSequence okCaption = getText(R.string.OK_BUTTON_CAPTION);
        DialogFactory dialogFct = DialogFactory.getInstance();
        dialogFct.messageBoxFromURI(this, webView, okCaption, 
                dialogFct.getDismissHandler());
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
        // TODO: Fix license page.
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
            SokoMainActivity.this.undoMove();
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
}