package com.amit.snakegame.SnakeHelper;

import android.view.*;

import com.amit.snakegame.*;



public class SnakeMovement {


    public AppConstants.Direction moveSnake(MotionEvent motionEvent, AppConstants.Control m_Control, AppConstants.Direction m_Direction, int m_ScreenWidth)
    {
        switch (m_Control)
        {
            case POV:
                m_Direction = moveSnakePOV(motionEvent,m_Control,m_Direction,m_ScreenWidth);
                break;
            case DUAL:
                m_Direction = moveSnakeDUAL(motionEvent,m_Control,m_Direction,m_ScreenWidth);
                break;
            case SPLIT:
            default:
                break;
        }
        return m_Direction;
    }

    private AppConstants.Direction moveSnakePOV(MotionEvent motionEvent, AppConstants.Control m_Control, AppConstants.Direction m_Direction, int m_ScreenWidth)
    {
        if ((motionEvent.getAction() & MotionEvent.ACTION_MASK) == MotionEvent.ACTION_UP) {
            if ((m_ScreenWidth ) > motionEvent.getX() ) {
                switch (m_Direction) {
                    case UP:
                        m_Direction = AppConstants.Direction.RIGHT;
                        break;
                    case RIGHT:
                        m_Direction = AppConstants.Direction.DOWN;
                        break;
                    case DOWN:
                        m_Direction = AppConstants.Direction.LEFT;
                        break;
                    case LEFT:
                        m_Direction = AppConstants.Direction.UP;
                        break;
                }
            }
        }
        return m_Direction;
    }
    private AppConstants.Direction moveSnakeDUAL(MotionEvent motionEvent, AppConstants.Control m_Control, AppConstants.Direction m_Direction, int m_ScreenWidth)
    {
        if ((motionEvent.getAction() & MotionEvent.ACTION_MASK) == MotionEvent.ACTION_UP) {
            if ((m_ScreenWidth /2) < motionEvent.getX() ) {
                switch (m_Direction) {
                    case UP:
                        m_Direction = AppConstants.Direction.RIGHT;
                        break;
                    case RIGHT:
                        m_Direction = AppConstants.Direction.DOWN;
                        break;
                    case DOWN:
                        m_Direction = AppConstants.Direction.LEFT;
                        break;
                    case LEFT:
                        m_Direction = AppConstants.Direction.UP;
                        break;
                }
            }
            else{
                switch (m_Direction) {
                    case UP:
                        m_Direction = AppConstants.Direction.LEFT;
                        break;
                    case RIGHT:
                        m_Direction = AppConstants.Direction.UP;
                        break;
                    case DOWN:
                        m_Direction = AppConstants.Direction.RIGHT;
                        break;
                    case LEFT:
                        m_Direction = AppConstants.Direction.DOWN;
                        break;
                }

            }
        }


        return m_Direction;
    }
}
