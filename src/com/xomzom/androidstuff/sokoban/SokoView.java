/*
 *  sokoban - a Sokoban game for midp-supporting mobile devices
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

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;

/**
 * The sokoban game view. The game view is a view that is associated with a 
 * board, and can draw it.
 * 
 * @author Dedi Hirschfeld
 */
public class SokoView extends View
{
    //
    // Color constants.
    //
    
    /**
     * The background color.
     */
    private final static int BACKGROUND_COLOR = 0xffe0e0e0;

    /**
     * The wall color.
     */
    private final static int WALL_COLOR = 0xff0000ff;
    
    /**
     * The box color.
     */
    private final static int BOX_COLOR = 0xffff0000;
    
    /**
     * The box target position color.
     */
    private final static int TARGET_COLOR = 0xff808080;
    
    /**
     * The player color.
     */
    private final static int PLAYER_COLOR = 0xff008000;
    
    /**
     * Color for the top-left corner of the box - lighted.
     */
    private final static int LIGHTED_COLOR = 0xffcfcfcf;

    /**
     * Color for the lower-right corner of the box - shaded.
     */
    private final static int SHADED_COLOR = 0xff404040;


    
    //
    // Members.
    //
    
    /**
     * The associated game activity.
     */
    private SokoGameActivity m_game;
    
    /**
     * The paint object to use (for now).
     */
    private Paint m_paint = new Paint();

    //
    // Operations.
    //
    
    /**
     * Create a view. In order to make the view actually show anything,
     * call setGame() to set the actual game activity (and board).
     */
    public SokoView(Context context, AttributeSet attrs, int defStyle)
    {
        super(context, attrs, defStyle);
    }

    /**
     * Create a view. In order to make the view actually show anything,
     * call setGame() to set the actual game activity (and board).
     */
    public SokoView(Context context, AttributeSet attrs)
    {
        super(context, attrs);
    }
    
    /**
     * set the game activity object associated with this view.
     */
    public void setGame(SokoGameActivity game)
    {
        m_game = game;
        invalidate();
    }

    /**
     * Refresh the canvas.
     */
    @Override
    protected void onDraw(Canvas canvas)
    {
        super.onDraw(canvas);

        canvas.drawColor(BACKGROUND_COLOR);
        
        m_paint = new Paint();
        m_paint.setAntiAlias(true);
        m_paint.setTextSize(16);
        setPadding(3, 3, 3, 3);

        if (m_game == null)
            return;

        int left = 5;
        int top = 5;
        int scrWidth = getWidth() - 5;
        int scrHeight = getHeight() - 5;
        
        Board board = m_game.getBoard();
        int boardWidth = board.getBoardWidth();
        int boardHeight = board.getBoardHeight();

        int squareWidth = (scrWidth - left) / boardWidth;
        int squareHeight = (scrHeight - top) / board.getBoardHeight();
        // Since we want all squares to be - well, square - we need to choose
        // just one size for width and height.
        int squareRealSize = (squareWidth < squareHeight) ? 
                squareWidth : squareHeight;
        
        for (int row = 0; row < boardHeight; row++)
        {
            for (int column = 0; column < boardWidth; column++)
            {
                int squareTop = top + row * squareRealSize;
                int squareLeft = left + column * squareRealSize;

                BoardSquare square = board.getSquare(column, row);
                if (square != null)
                {
                    paintSquare(squareLeft, squareTop, 
                            squareRealSize, square, canvas);
                }
            }
        }

        int playerX = board.getPlayerX();
        int playerY = board.getPlayerY();
        int playerTop = top + playerY * squareRealSize;
        int playerLeft = left + playerX * squareRealSize;
        
        
        m_paint.setColor(PLAYER_COLOR);
        RectF playerRect = new RectF(playerLeft, playerTop, 
                playerLeft + squareRealSize - 1, playerTop + squareRealSize - 1);
        canvas.drawArc(playerRect, 0, 360, true, m_paint);

        m_paint.setColor(0x000000);
    }

    /**
     * Paint a square at the given position.
     * 
     * @param squareLeft The square left coordinate.
     * @param squareTop The square top coordinate.
     * @param squareSize The square width and height.
     * @param square The square to paint.
     * @param g The graphics object to draw on.
     */
    private void paintSquare(int squareLeft, int squareTop, 
                             int squareSize, BoardSquare square, 
                             Canvas canvas)
    {
        if (square.isWall())
        {
            draw3DBox(squareLeft, squareTop, squareSize - 1, WALL_COLOR, 
                      true, canvas);
        }
        else if (square.hasBox())
        {
            draw3DBox(squareLeft, squareTop, squareSize - 1, BOX_COLOR, 
                      true, canvas);
        }
        else if (square.isTarget())
        {
            draw3DBox(squareLeft, squareTop, squareSize - 1, TARGET_COLOR, 
                      false, canvas);
        }
    }
    
    /**
     * Draw a 3d box of the given color at the given position.
     * @param boxLeft the box's left coordinate.
     * @param boxTop The box's top coordinate.
     * @param size The box size
     * @param color The box's color.
     * @param outward true if the box should stick out, false if it should be
     * pressed in.
     * @param canvas The canvas to draw on.
     */
    private void draw3DBox(int boxLeft, int boxTop, 
                         int size, int color, boolean outward, Canvas canvas)
    {
        // g.setColor(color);
        m_paint.setColor(color);
        int boxRight = boxLeft + size - 1;
        int boxBottom  = boxTop + size - 1;
        canvas.drawRect(boxLeft, boxTop, boxRight, boxBottom, 
                m_paint);


        m_paint.setColor(outward ? LIGHTED_COLOR : SHADED_COLOR);
        canvas.drawLine(boxLeft, boxTop, boxRight, boxTop, m_paint);
        canvas.drawLine(boxLeft, boxTop, boxLeft, boxBottom, m_paint);
        m_paint.setColor(outward ? SHADED_COLOR: LIGHTED_COLOR);
        canvas.drawLine(boxLeft + 1, boxBottom, boxRight, boxBottom, m_paint);
        canvas.drawLine(boxRight, boxTop + 1, boxRight, boxBottom, m_paint);
    }
 }
