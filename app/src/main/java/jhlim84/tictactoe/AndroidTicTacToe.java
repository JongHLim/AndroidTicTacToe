package jhlim84.tictactoe;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.media.AudioManager;
import android.media.SoundPool;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

public class AndroidTicTacToe extends ActionBarActivity {

    //CLASS LEVEL FIELDS:
    // Represents the internal state of the game
    private TicTacToeGame mGame;

    private BoardView mBoardView;

    // for all the sounds we play
    private SoundPool mSounds;
    private int mHumanMoveSoundID;
    private int mComputerMoveSoundID;
    private int mTieSoundID;
    private int mHumanWinSoundID;
    private int mComputerWinSoundID;

    // Class level variable to store the status of the game...whether or not it's over:
    private boolean mGameOver;

    // Whose turn to go first
    private char mTurn = TicTacToeGame.COMPUTER_PLAYER;

    // Buttons making up the board
    private Button mBoardButtons[];

    // Various text displayed
    private TextView mInfoTextView;
    private TextView mHumanScoreTextView;
    private TextView mComputerScoreTextView;
    private TextView mTieScoreTextView;

    // Keep track of wins
    private int mHumanWins = 0;
    private int mComputerWins = 0;
    private int mTies = 0;

    static final int DIALOG_DIFFICULTY_ID = 0;
    static final int DIALOG_QUIT_ID = 1;

    //OVERRIDE METHODS:
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        mInfoTextView = (TextView) findViewById(R.id.information);
        mHumanScoreTextView = (TextView) findViewById(R.id.player_score);
        mComputerScoreTextView = (TextView) findViewById(R.id.computer_score);
        mTieScoreTextView = (TextView) findViewById(R.id.tie_score);

        mGame = new TicTacToeGame();
        mBoardView = (BoardView) findViewById(R.id.board);
        mBoardView.setGame(mGame);
        // Listen for touches on the board
        mBoardView.setOnTouchListener(mTouchListener);

        startNewGame();
    }//end onCreate override method

    protected Dialog onCreateDialog(int id) {
        Dialog dialog = null;
        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        switch (id) {
            case DIALOG_DIFFICULTY_ID:
                builder.setTitle(R.string.difficulty_choose);
                final CharSequence[] levels = {
                        getResources().getString(R.string.difficulty_easy),
                        getResources().getString(R.string.difficulty_harder),
                        getResources().getString(R.string.difficulty_expert)};

                // TODO: Set selected, an integer (0 to n-1), for the Difficulty dialog.
                // selected is the radio button that should be selected.
                int selected = mGame.getDifficultyLevel().ordinal();

                builder.setSingleChoiceItems(levels, selected,
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int item) {
                                dialog.dismiss(); // Close dialog

                                // TODO: Set the diff level of mGame based on which item was selected.
                                TicTacToeGame.DifficultyLevel[] difficulties = TicTacToeGame.DifficultyLevel.values();
                                TicTacToeGame.DifficultyLevel difficulty = difficulties[item];
                                mGame.setDifficultyLevel(difficulty);

                                // Display the selected difficulty level
                                Toast.makeText(getApplicationContext(), levels[item],
                                        Toast.LENGTH_SHORT).show();
                            }
                        });
                dialog = builder.create();

                break;

            case DIALOG_QUIT_ID:
                // Create the quit confirmation dialog

                builder.setMessage(R.string.quit_question)
                        .setCancelable(false)
                        .setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                AndroidTicTacToe.this.finish();
                            }
                        })
                        .setNegativeButton(R.string.no, null);
                dialog = builder.create();

                break;
        }


        return dialog;
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        super.onCreateOptionsMenu(menu);

        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_main, menu);
        return true;
    }//end onCreateOptionsMenu override method

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.new_game:
                startNewGame();
                return true;
            case R.id.ai_difficulty:
                showDialog(DIALOG_DIFFICULTY_ID);
                return true;
            case R.id.quit:
                showDialog(DIALOG_QUIT_ID);
                return true;
        }
        return false;
    }

    //OTHER METHODS:
    public void startNewGamePressed(View v) {
        if (!mGameOver) {
            mComputerWins++;
            mComputerScoreTextView.setText(Integer.toString(mComputerWins));
            mInfoTextView.setText(R.string.result_computer_wins);
        }
        startNewGame();
    }//end startNewGamePressed method

    private void startNewGame() {
        mGameOver = false;

        //clears the internal representation of the game:
        mGame.clearBoard();

        mGame.clearBoard();
        mBoardView.invalidate(); // Leads to a redraw of the board view

        // Alternate who goes first
        if (mTurn == TicTacToeGame.HUMAN_PLAYER) {
            mTurn = TicTacToeGame.COMPUTER_PLAYER;
            mInfoTextView.setText(R.string.first_computer);
            int move = mGame.getComputerMove();
            setMove(TicTacToeGame.COMPUTER_PLAYER, move);
            mInfoTextView.setText(R.string.turn_human);
        } else {
            mTurn = TicTacToeGame.HUMAN_PLAYER;
            mInfoTextView.setText(R.string.first_human);
        }//end if else
    }//end startNewGame method

    private boolean setMove(char player, int location) {
        if (mGame.setMove(player, location)) {
            if (mGame.getBoardOccupant(location) == mGame.HUMAN_PLAYER)
                // soundID, leftVolume, rightVolume, priority, loop, rate
                mSounds.play(mHumanMoveSoundID, 1, 1, 1, 0, 1);
            if (mGame.getBoardOccupant(location) == mGame.COMPUTER_PLAYER)
                // soundID, leftVolume, rightVolume, priority, loop, rate
                mSounds.play(mComputerMoveSoundID, 1, 1, 1, 0, 1);
            mBoardView.invalidate(); // Redraw the board
            return true;
        }
        return false;

    }//end setMove method

    // when game is over, disable all buttons and set flag
    private void gameOver() {
        mGameOver = true;
    }//end gameOver method

    // Listen for touches on the board
    private View.OnTouchListener mTouchListener = new View.OnTouchListener() {
        public boolean onTouch(View v, MotionEvent event) {
            // Determine which cell was touched
            int col = (int) event.getX() / mBoardView.getBoardCellWidth();
            int row = (int) event.getY() / mBoardView.getBoardCellHeight();
            int pos = row * 3 + col;

            if (!mGameOver && setMove(TicTacToeGame.HUMAN_PLAYER, pos)) {

                setMove(TicTacToeGame.HUMAN_PLAYER, pos);

                // If no winner yet, let the computer make a move
                int winner = mGame.checkForWinner();
                if (winner == 0) {
                    mInfoTextView.setText(R.string.turn_computer);
                    int move = mGame.getComputerMove();
                    setMove(TicTacToeGame.COMPUTER_PLAYER, move);
                    winner = mGame.checkForWinner();
                }//end if

                if (winner == 0) {
                    mInfoTextView.setText(R.string.turn_human);
                } else {
                    if (winner == 1) {
                        mTies++;
                        mTieScoreTextView.setText(Integer.toString(mTies));
                        mInfoTextView.setText(R.string.result_tie);
                        // soundID, leftVolume, rightVolume, priority, loop, rate
                        mSounds.play(mTieSoundID, 1, 1, 1, 0, 1);
                    } else if (winner == 2) {
                        mHumanWins++;
                        mHumanScoreTextView.setText(Integer.toString(mHumanWins));
                        mInfoTextView.setText(R.string.result_human_wins);
                        // soundID, leftVolume, rightVolume, priority, loop, rate
                        mSounds.play(mHumanWinSoundID, 1, 1, 1, 0, 1);
                    } else {
                        mComputerWins++;
                        mComputerScoreTextView.setText(Integer.toString(mComputerWins));
                        mInfoTextView.setText(R.string.result_computer_wins);
                        // soundID, leftVolume, rightVolume, priority, loop, rate
                        mSounds.play(mComputerWinSoundID, 1, 1, 1, 0, 1);
                    }//end inner if else
                    gameOver();
                }//end outer if else

                // If no winner yet, let the computer make a move
            }

// So we aren't notified of continued events when finger is moved
            return false;
        }
    };//end AndroidTicTacToe Class.

    @Override
    protected void onResume() {
        super.onResume();
        mSounds = new SoundPool(2, AudioManager.STREAM_MUSIC, 0);
// 2 = maximum sounds ot play at the same time,
// AudioManager.STREAM_MUSIC is the stream type typically used for games
// 0 is the "the sample-rate converter quality. Currently has no effect. Use 0 for the default."
        mHumanMoveSoundID = mSounds.load(this, R.raw.human_move, 1);
// Context, id of resource, priority (currently no effect)
        mComputerMoveSoundID = mSounds.load(this, R.raw.computer_move, 1);

        mComputerWinSoundID = mSounds.load(this, R.raw.computer_win, 1);
// Context, id of resource, priority (currently no effect)
        mHumanWinSoundID = mSounds.load(this, R.raw.human_win, 1);

        mTieSoundID = mSounds.load(this, R.raw.tie, 1);
    }
    @Override
    protected void onPause() {
        super.onPause();
        //Log.d(TAG, "in onPause");
        if(mSounds != null) {
            mSounds.release();
            mSounds = null;
        }
    }

}
