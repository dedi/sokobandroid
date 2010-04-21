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
    private final static int LIGHT_COLOR = 0xffcfcfcf;

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
        // This will draw the background for the resource.
        super.onDraw(canvas);
        
        if (m_game == null)
            return;

        m_paint = new Paint();
        m_paint.setAntiAlias(true);
        setPadding(3, 3, 3, 3);

        Board board = m_game.getBoard();
        int boardWidth = board.getBoardWidth();
        int boardHeight = board.getBoardHeight();

        int squareRealSize = getSquareSize();
        
        int playerX = board.getPlayerX();
        int playerY = board.getPlayerY();

        for (int row = 0; row < boardHeight; row++)
        {
            for (int column = 0; column < boardWidth; column++)
            {
                BoardSquare square = board.getSquare(column, row);
                if (square != null)
                {
                    paintSquare(column, row, 
                            squareRealSize, square, canvas);
                }
                if ((row == playerY) && (column == playerX))
                {
                    drawPlayer(column, row, squareRealSize, canvas);
                }
            }
        }
    }
    
    /**
     * Calculate the best size for a game square, based on the current board
     * coordinates and view size.
     * @return
     */
    private int getSquareSize()
    {
        int scrWidth = getWidth();
        int scrHeight = getHeight();
        
        Board board = m_game.getBoard();
        int boardWidth = board.getBoardWidth();
        int boardHeight = board.getBoardHeight();

        int squareWidth = scrWidth / boardWidth;
        int squareHeight = scrHeight / boardHeight;
        // Since we want all squares to be - well, square - we need to choose
        // just one size for width and height.
        int squareBestSize = (squareWidth < squareHeight) ? 
                squareWidth : squareHeight;
        return squareBestSize;
    }

    /**
     * Draw the player in the given position.
     * 
     * @param column
     * @param row
     * @param squareSize
     * @param canvas
     */
    private void drawPlayer(int column, int row, int squareSize,
            Canvas canvas)
    {
        int squareLeft = column * squareSize;
        int squareTop = row * squareSize;

        m_paint.setColor(PLAYER_COLOR);
        RectF playerRect = new RectF(squareLeft, squareTop, 
                squareLeft + squareSize - 1, squareTop + squareSize - 1);
        canvas.drawArc(playerRect, 0, 360, true, m_paint);
    }

    /**
     * Paint a square at the given position.
     * 
     * @param column The square column.
     * @param row The square row.
     * @param squareSize The square width and height.
     * @param square The square to paint.
     * @param g The graphics object to draw on.
     */
    private void paintSquare(int column, int row, 
                             int squareSize, BoardSquare square, 
                             Canvas canvas)
    {
        
        if (square.isWall())
        {
            draw3DBox(column, row, squareSize, WALL_COLOR, 
                      true, canvas);
        }
        else if (square.hasBox())
        {
            draw3DBox(column, row, squareSize, BOX_COLOR, 
                      true, canvas);
        }
        else if (square.isTarget())
        {
            draw3DBox(column, row, squareSize, TARGET_COLOR, 
                      false, canvas);
        }
    }
    
    /**
     * Draw a 3d box of the given color at the given position.
     * @param column The square column.
     * @param row The square row.
     * @param size The box size
     * @param color The box's color.
     * @param outward true if the box should stick out, false if it should be
     * pressed in.
     * @param canvas The canvas to draw on.
     */
    private void draw3DBox(int column, int row, 
                         int size, int color, boolean outward, Canvas canvas)
    {
        int boxLeft = column * size;
        int boxTop = row * size;
        m_paint.setColor(color);
        int boxRight = boxLeft + size - 1;
        int boxBottom  = boxTop + size - 1;
        canvas.drawRect(boxLeft, boxTop, boxRight, boxBottom, 
                m_paint);


        m_paint.setColor(outward ? LIGHT_COLOR : SHADED_COLOR);
        canvas.drawLine(boxLeft, boxTop, boxRight, boxTop, m_paint);
        canvas.drawLine(boxLeft, boxTop, boxLeft, boxBottom, m_paint);
        m_paint.setColor(outward ? SHADED_COLOR: LIGHT_COLOR);
        canvas.drawLine(boxLeft + 1, boxBottom, boxRight, boxBottom, m_paint);
        canvas.drawLine(boxRight, boxTop + 1, boxRight, boxBottom, m_paint);
    }
}
