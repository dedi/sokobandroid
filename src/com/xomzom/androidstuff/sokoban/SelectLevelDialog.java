package com.xomzom.androidstuff.sokoban;

import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;

/**
 * The level selection dialog.
 * 
 * @author dedi
 */
public class SelectLevelDialog extends Dialog 
    implements OnClickListener, TextWatcher
{
    //
    // Members.
    //
    
    /**
     * The Sokoban activity this dialog is associated with.
     */
    private SokoGameActivity m_owner;
    
    /**
     * The 'OK' button.
     */
    private Button m_okButton;

    /**
     * The 'Cancel' button.
     */
    private Button m_cancelButton;
    
    /**
     * The level input field.
     */
    private EditText m_levelInputField;
    

    //
    // Operations.
    //
    
    /**
     * Create a level selection dialog object, associated with a 
     * Sokoban activity.
     */
    public SelectLevelDialog(SokoGameActivity owner)
    {
        super(owner);
        m_owner = owner;
        setOwnerActivity(owner);
    }


    /**
     * Actually create the dialog.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.select_level_dialog);
        setTitle(R.string.SELECT_LEVEL_DIALOG_TITLE);
        m_okButton = (Button)findViewById(R.id.select_level_ok);
        m_okButton.setOnClickListener(this);
        m_cancelButton = (Button)findViewById(R.id.select_level_cancel);
        m_cancelButton.setOnClickListener(this);
        m_levelInputField = 
            (EditText)findViewById(R.id.select_level_level_field);
        m_levelInputField.addTextChangedListener(this);
        setOkButtonEnabledState();
    }
    
    /**
     * The 'onclick' event handler - for both OK and Cancel buttons.
     */
    @Override
    public void onClick(View source)
    {
        if (source == m_okButton)
        {
            int newLevel = getLevelFieldValue();
            if (newLevel > 0 && newLevel <= m_owner.getMaxLevel())
                m_owner.setLevel(newLevel);
            else
                doBadLevelError();
        }

        dismiss();
    }
    
    /**
     * Display a 'bad level selected' error message.
     */
    private void doBadLevelError()
    {
        int maxLevel = m_owner.getMaxLevel();
        String errMsg = 
            m_owner.getString(R.string.ERR_BAD_LEVEL_SELECTED, maxLevel);
        String okButtonCaption = m_owner.getString(R.string.OK_BUTTON_CAPTION);
        DialogFactory dialogFactory = DialogFactory.getInstance();
        DialogInterface.OnClickListener dismissAction = 
            dialogFactory.getDismissHandler();
        dialogFactory.messageBox(m_owner, errMsg, 
                                 okButtonCaption, dismissAction);
    }

    
    /**
     * Get the contents of the level field. Return -1 if the content of the 
     * field is not a valid integer.
     * @return
     */
    private int getLevelFieldValue()
    {
        CharSequence newLevelAsChars = m_levelInputField.getText(); 
        String newLevelAsString = newLevelAsChars.toString();
        int newLevel = -1;
        try {
            newLevel = Integer.parseInt(newLevelAsString);
        } catch (NumberFormatException e) {
            // The only way we should get here is if field is null, empty, or
            // non-numeric. None of these should happen, but still... Better to
            // return something then to die.
        }
        return newLevel;
    }

    /**
     * Handle the 'text change' event - enable or disable the 'OK' button,
     * depending on whether there's actual text in the field.
     */
    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count)
    {
        setOkButtonEnabledState();
    }
    
    /**
     * Enable or disable the 'OK' button,
     * depending on whether there's actual text in the field.
     */
    private void setOkButtonEnabledState()
    {
        CharSequence newLevelAsChars = m_levelInputField.getText();
        m_okButton.setEnabled(newLevelAsChars.length() > 0);
    }
    
    /**
     * Unused listener method.
     */
    @Override
    public void afterTextChanged(Editable s)
    {
    }


    /**
     * Unused listener method.
     */
    @Override
    public void beforeTextChanged(CharSequence s, int start, int count,
            int after)
    {
    }
}
