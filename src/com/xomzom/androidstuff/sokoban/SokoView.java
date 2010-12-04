/*
 *  sokoban - a Sokoban game for android devices
 *  Copyright (C) 2010 Dedi Hirschfeld
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
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
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
     * Board background color.
     */
    private final static int BOARD_COLOR = 0xffb49056;

    /**
     * The wall color.
     */
    private final static int WALL_COLOR = 0xff9d5c3d;

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

    /**
     * The resource manager.
     */
    private GameResourceManager m_resourceManager;


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
        m_resourceManager = new GameResourceManager(getResources());
    }

    /**
     * Create a view. In order to make the view actually show anything,
     * call setGame() to set the actual game activity (and board).
     */
    public SokoView(Context context, AttributeSet attrs)
    {
        this(context, attrs, 0);
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
        // This will draw the background for the view.
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

        for (int row = 0; row < boardHeight; row++)
        {
            for (int column = 0; column < boardWidth; column++)
            {
                BoardSquare square = board.getSquare(column, row);
                if (square != null)
                {
                    drawSquare(column, row, squareRealSize, square, canvas);
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
     * draw the contents of the given square.
     *
     * @param column The square column.
     * @param row The square row.
     * @param squareSize The square width and height.
     * @param square The square to paint.
     * @param g The graphics object to draw on.
     */
    private void drawSquare(int column, int row,
                             int squareSize, BoardSquare square,
                             Canvas canvas)
    {
        if (square.isWall())
        {
            drawBox(column, row, squareSize, WALL_COLOR,
                      3, canvas);
            return;
        }

        if (square.isInsideBoard())
        {
            drawBox(column, row, squareSize, BOARD_COLOR,
                    1, canvas);
        }

        if (square.isTarget())
        {
            drawBitmap(m_resourceManager.getTargetBitmap(), column, row,
                       squareSize, canvas);
        }

        if (square.hasBox())
        {
            drawBitmap(m_resourceManager.getBoxBitmap(), column, row,
                    squareSize, canvas);
        }


        Board board = m_game.getBoard();
        if ((row == board.getPlayerY()) && (column == board.getPlayerX()))
        {
            drawBitmap(m_resourceManager.getPlayerBitmap(),
                    column, row, squareSize, canvas);
        }
    }

    /**
     * Draw the given bitmap in the given position.
     *
     * @param column
     * @param row
     * @param squareSize
     * @param canvas
     */
    private void drawBitmap(Bitmap bitmap, int column, int row, int squareSize,
                Canvas canvas)
    {
        int squareLeft = column * squareSize;
        int squareTop = row * squareSize;
        Rect rect = new Rect(squareLeft, squareTop,
                squareLeft + squareSize - 1, squareTop + squareSize - 1);
        canvas.drawBitmap(bitmap, null, rect, null);
    }

    /**
     * Draw a box of the given color at the given position.
     * @param column The square column.
     * @param row The square row.
     * @param size The box size
     * @param color The box's color.
     * @param height The height to simulate using 3d light effects - 0 and up to
     *               around 3 will look OK, depending on the screen resolution.
     * @param canvas The canvas to draw on.
     */
    private void drawBox(int column, int row,
                         int size, int color,
                         int height, Canvas canvas)
    {
        int boxLeft = column * size;
        int boxTop = row * size;
        m_paint.setColor(color);
        int boxRight = boxLeft + size - 1;
        int boxBottom  = boxTop + size - 1;
        canvas.drawRect(boxLeft, boxTop,
                boxRight, boxBottom,
                m_paint);

        // light effect...
        for (int lightLine = 0; lightLine < height; lightLine ++)
        {
            m_paint.setColor(LIGHT_COLOR);
            canvas.drawLine(boxLeft, boxTop, boxRight, boxTop, m_paint);
            canvas.drawLine(boxLeft, boxTop, boxLeft, boxBottom, m_paint);
            m_paint.setColor(SHADED_COLOR);
            canvas.drawLine(boxLeft + 1, boxBottom, boxRight, boxBottom, m_paint);
            canvas.drawLine(boxRight, boxTop + 1, boxRight, boxBottom, m_paint);
            boxLeft++;
            boxTop++;
            boxRight--;
            boxBottom--;
        }
    }
}
