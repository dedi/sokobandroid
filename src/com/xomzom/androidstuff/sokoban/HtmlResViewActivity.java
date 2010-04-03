package com.xomzom.androidstuff.sokoban;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.webkit.WebView;

/**
 * A viewer for HTML resources
 * 
 * @author dedi
 */
public class HtmlResViewActivity extends Activity
{
    /**
     * Create an HtmlResViewActivity, associated with the given document.
     */
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        Intent ourIntent = getIntent();
        String resourceUri = ourIntent.getDataString();

        WebView webView = new WebView(this);
        webView.loadUrl(resourceUri);
        setContentView(webView);
    }
}
