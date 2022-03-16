package com.amit.snakegame;



import android.annotation.*;
import android.content.*;
import android.content.res.*;
import android.graphics.*;
import android.media.*;
import android.view.*;

import com.amit.snakegame.AppConstants.*;
import com.amit.snakegame.SnakeHelper.*;

import java.io.*;
import java.util.*;

@SuppressLint("ViewConstructor")
public class SnakeView extends SurfaceView implements Runnable {

    // All the code will run separately to the UI
    private Thread m_Thread = null;
    // This variable determines when the game is playing
    // It is declared as volatile because
    // it can be accessed from inside and outside the thread
    private volatile boolean m_Playing;

    // This is what we draw on
    public Canvas m_Canvas;
    // This is required by the Canvas class to do the drawing
    private final SurfaceHolder m_Holder;
    // This lets us control colors etc
    private final Paint m_Paint;

    // This will be a reference to the Activity
    public Context m_context;

    // Sound
    private SoundPool m_SoundPool;
    private int m_get_mouse_sound = -1;
    private int m_dead_sound = -1;



    // What is the screen resolution
    public int m_ScreenWidth;
    public int m_ScreenHeight;

    // Control pausing between updates
    public long m_NextFrameTime;
    // Update the game 10 times per second
    public final long FPS = 10;
    // There are 1000 milliseconds in a second
    public final long MILLIS_IN_A_SECOND = 1000;
    // We will draw the frame much more often

    // The current m_Score
    private int m_Score;

    // The location in the grid of all the segments
    public int[] m_SnakeXs;
    public int[] m_SnakeYs;

    // How long is the snake at the moment
    public int m_SnakeLength;


    // Where is the mouse
    private int m_MouseX;
    private int m_MouseY;

    // The size in pixels of a snake segment
    public int m_BlockSize;// 48 for 16:9 screen

    // The size in segments of the playable area
    private final int NUM_BLOCKS_WIDE = 40;
    public int m_NumBlocksHigh; // determined dynamically 22 for 16:9 screen






    Boolean isSoundEnabled = true;

    public SharedPreferences preferences;
    public SnakeView(Context context, Point size) {
        super(context);

        m_context = context;
        preferences = m_context.getSharedPreferences("SnakePreferences", Context.MODE_PRIVATE);

        m_ScreenWidth = size.x;
        m_ScreenHeight = size.y;

        //Determine the size of each block/place on the game board
        m_BlockSize = m_ScreenWidth / NUM_BLOCKS_WIDE;
        // How many blocks of the same size will fit into the height
        m_NumBlocksHigh = m_ScreenHeight / m_BlockSize;

        // Set the sound up
        loadSound();

        // Initialize the drawing objects
        m_Holder = getHolder();
        m_Paint = new Paint();

        int arraySize = NUM_BLOCKS_WIDE * m_NumBlocksHigh;

        m_SnakeXs = new int[arraySize];
        m_SnakeYs = new int[arraySize];



    }

    // Start by heading to the right
    private Direction m_Direction = Direction.RIGHT;

    @Override
    public void run() {
        // The check for m_Playing prevents a crash at the start


        while (m_Playing) {

            // Update 10 times a second
            if(checkForUpdate()) {
                updateGame();
                drawGame();


            }

        }
    }

    public void pause() {
        m_Playing = false;
        try {
            m_Thread.join();
        } catch (InterruptedException e) {
            // Error
        }
    }

    public void resume() {
        m_Playing = true;
        m_Thread = new Thread(this);
        m_Thread.start();
    }

    public void loadSound() {
        m_SoundPool = new SoundPool(10, AudioManager.STREAM_MUSIC, 0);
        try {
            // Create objects of the 2 required classes
            // Use m_Context because this is a reference to the Activity
            AssetManager assetManager = m_context.getAssets();
            AssetFileDescriptor descriptor;

            // Prepare the two sounds in memory
            descriptor = assetManager.openFd("get_mouse_sound.ogg");
            m_get_mouse_sound = m_SoundPool.load(descriptor, 0);

            descriptor = assetManager.openFd("death_sound.ogg");
            m_dead_sound = m_SoundPool.load(descriptor, 0);

        } catch (IOException e) {
            // Error
        }
    }


    public void startGame() {

        m_SnakeLength = 1;
        m_SnakeXs[0] = NUM_BLOCKS_WIDE /2;
        m_SnakeYs[0] = m_NumBlocksHigh /2;


        spawnMouse();


        m_Score = 0;

        // Setup m_NextFrameTime so an update is triggered immediately
        m_NextFrameTime = System.currentTimeMillis();
    }



    public void spawnMouse() {
        Random random = new Random();
        m_MouseX = random.nextInt(NUM_BLOCKS_WIDE - 1) + 1;
        m_MouseY = random.nextInt(m_NumBlocksHigh - 1) + 1;

        // spawned in snake
        for (int i = m_SnakeLength - 1; i > 0; i--) {
            if ((i > 4) && (m_MouseX == m_SnakeXs[i]) && (m_MouseY == m_SnakeYs[i])) {
                spawnMouse();
            }
        }

    }

    //control
    private AppConstants.Control m_Control = AppConstants.Control.DUAL;
    SnakeMovement m_SnakeMovement = new SnakeMovement();


    private void moveSnake(){

        for (int i = m_SnakeLength; i > 0; i--) {
            // Start at the back and move it
            // to the position of the segment in front of it
            m_SnakeXs[i] = m_SnakeXs[i - 1];
            m_SnakeYs[i] = m_SnakeYs[i - 1];

            // Exclude the head because
            // the head has nothing in front of it
        }


        switch (m_Direction) {
            case UP:
                m_SnakeYs[0]--;
                break;

            case RIGHT:
                m_SnakeXs[0]++;
                break;

            case DOWN:
                m_SnakeYs[0]++;
                break;

            case LEFT:
                m_SnakeXs[0]--;
                break;
        }
    }

    public void setControl(Control control)
    {
        m_Control = control;
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public boolean onTouchEvent(MotionEvent motionEvent) {


        m_Direction = m_SnakeMovement.moveSnake(motionEvent,m_Control,m_Direction,m_ScreenWidth);
        return true;
    }

    private void eatMouse(){

        m_SnakeLength++;
        //replace the mouse
        spawnMouse();
        //add to the m_Score
        m_Score = m_Score+1;
        if(isSoundEnabled)
            m_SoundPool.play(m_get_mouse_sound, 1, 1, 0, 0, 1);

        updateHighScore();
    }

    public void updateHighScore()
    {
        int highScore = preferences.getInt("HighScore", 0);

        if(m_Score > highScore)
        {
            SharedPreferences.Editor editor = preferences.edit();
            editor.putInt("HighScore", m_Score);
            editor.apply();
        }
    }


    public void updateGame() {
        // Did the head of the snake touch the mouse?
        if (m_SnakeXs[0] == m_MouseX && m_SnakeYs[0] == m_MouseY) {
            eatMouse();
        }

        moveSnake();

        if (detectDeath()) {
            //start again

            if(isSoundEnabled)
                m_SoundPool.play(m_dead_sound, 1, 1, 0, 0, 1);

            ((com.amit.snakegame.SnakeActivity) m_context).finish();

        }
    }

    private boolean detectDeath(){
        // Has the snake died?
        boolean dead = false;

        // Hit a wall?
        if (m_SnakeXs[0] == -1) dead = true;
        if (m_SnakeXs[0] >= NUM_BLOCKS_WIDE) dead = true;
        if (m_SnakeYs[0] == -1) dead = true;
        if (m_SnakeYs[0] >= m_NumBlocksHigh) dead = true;

        // Eaten itself?
        for (int i = m_SnakeLength - 1; i > 0; i--) {
            if ((i > 4) && (m_SnakeXs[0] == m_SnakeXs[i]) && (m_SnakeYs[0] == m_SnakeYs[i])) {
                dead = true;
                break;
            }
        }

        return dead;
    }


    public boolean checkForUpdate() {

        // Are we due to update the frame
        if(m_NextFrameTime <= System.currentTimeMillis()){
            // Tenth of a second has passed

            // Setup when the next update will be triggered
            m_NextFrameTime =System.currentTimeMillis() + MILLIS_IN_A_SECOND / FPS;

            // Return true so that the update and draw
            // functions are executed
            return true;
        }

        return false;
    }


    public void drawGame() {
        // Prepare to draw
        if (m_Holder.getSurface().isValid()) {
            m_Canvas = m_Holder.lockCanvas();

            // Clear the screen with my favorite color
            m_Canvas.drawColor(Color.argb(255, 0, 0, 0));



            //Apple color
            m_Paint.setColor(Color.argb(255,  255, 0, 0));
            //draw the mouse

            int size = m_BlockSize/2;
            m_Canvas.drawCircle((m_MouseX*m_BlockSize)+size,(m_MouseY*m_BlockSize)+size,size,m_Paint);



            // Set the color of the paint to draw the snake with
            m_Paint.setColor(Color.argb(255,  255,193 , 7));
            //Draw the snake
            for (int i = 0; i < m_SnakeLength; i++) {
                m_Canvas.drawRect(m_SnakeXs[i] * m_BlockSize,
                        (m_SnakeYs[i] * m_BlockSize),
                        (m_SnakeXs[i] * m_BlockSize) + m_BlockSize,
                        (m_SnakeYs[i] * m_BlockSize) + m_BlockSize,
                        m_Paint);
            }

            //Text color
            m_Paint.setColor(Color.argb(255, 255, 255, 255));

            // Choose how big the score will be
            m_Paint.setTextSize(50);
            m_Canvas.drawText("Score:" + m_Score, 10, 50, m_Paint);

            int highScore = preferences.getInt("HighScore", 0);
            m_Canvas.drawText("High Score:" + highScore, m_ScreenWidth/2, 50, m_Paint);

            m_Canvas.drawLine(0.0f,(float)m_NumBlocksHigh*m_BlockSize,(float)m_ScreenWidth,(float)(m_NumBlocksHigh*m_BlockSize)+1,m_Paint);

            // Draw the whole frame
            m_Holder.unlockCanvasAndPost(m_Canvas);

        }
    }




}