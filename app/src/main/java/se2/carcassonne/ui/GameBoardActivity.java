package se2.carcassonne.ui;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Vibrator;
import android.os.VibratorManager;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.constraintlayout.widget.ConstraintSet;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.Objects;

import io.github.controlwear.virtual.joystick.android.JoystickView;
import se2.carcassonne.R;
import se2.carcassonne.databinding.GameboardActivityBinding;
import se2.carcassonne.helper.resize.FullscreenHelper;
import se2.carcassonne.model.Coordinates;
import se2.carcassonne.model.GameBoard;
import se2.carcassonne.model.Meeple;
import se2.carcassonne.model.PlacedTileDto;
import se2.carcassonne.model.Player;
import se2.carcassonne.model.PointCalculator;
import se2.carcassonne.model.RoadResult;
import se2.carcassonne.model.Tile;
import se2.carcassonne.repository.PlayerRepository;
import se2.carcassonne.viewmodel.GameSessionViewModel;

// fixme avoid a god class here (eg, meeple and tile placement, turn logic etc)
public class GameBoardActivity extends AppCompatActivity {
    GameboardActivityBinding binding;
    ObjectMapper objectMapper;
    Player currentPlayer;
    private GridView gridView;
    private GameboardAdapter gameboardAdapter;
    private MeepleAdapter meepleAdapter;
    private Tile tileToPlace;
    private Button buttonLeft;
    private Button buttonRight;
    private Button buttonUp;
    private Button buttonDown;
    private Button buttonConfirm;
    private GameBoard gameBoard;
    private ImageView previewTileToPlace;
    private GameSessionViewModel gameSessionViewModel;
    private Intent intent;
    private Button zoomInBtn;
    private Button zoomOutBtn;
    private PointCalculator roadCalculator;

    Animation scaleAnimation = null;

    // fixme extract methods!
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = GameboardActivityBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        FullscreenHelper.setFullscreenAndImmersiveMode(this);

//        Animation for scaling the buttons
        scaleAnimation = AnimationUtils.loadAnimation(this, R.anim.scale);

        //Bind all UI elements
        bindGameBoardUiElements();


        objectMapper = new ObjectMapper();
        currentPlayer = PlayerRepository.getInstance().getCurrentPlayer();

//        Create a new game board and place a random tile on it
        gameBoard = new GameBoard();
        // TODO: CHECK THIS FURTHER, JUST AN IDEA AS OF RIGHT NOW
        roadCalculator = new PointCalculator(gameBoard);


//        Set up the grid view
        gridView.setScaleX(3.0f);
        gridView.setScaleY(3.0f);
        gridView.setStretchMode(GridView.NO_STRETCH);

//        Instantiate gameBoardActivityViewModel
        gameSessionViewModel = new GameSessionViewModel();

//        Get the next turn message from the previous activity
        intent = getIntent();
        String currentLobbyAdmin = intent.getStringExtra("LOBBY_ADMIN_ID");

        gameboardAdapter = new GameboardAdapter(this, gameBoard, tileToPlace);
        gridView.setAdapter(gameboardAdapter);

        String resourceName = "meeple_" + currentPlayer.getPlayerColour().name().toLowerCase();
        binding.ivMeepleWithPlayerColor.setImageResource(getResources().getIdentifier(resourceName, "drawable", getPackageName()));


        /*
         * placed tile observable
         */
        gameSessionViewModel.getPlacedTileLiveData().observe(this, tilePlaced -> {
            Tile tilePlacedByOtherPlayer = gameboardAdapter.getGameBoard().getAllTiles().get(Math.toIntExact(tilePlaced.getTileId()));
            tilePlacedByOtherPlayer.setRotation(tilePlaced.getRotation());
            tilePlacedByOtherPlayer.setPlacedMeeple(tilePlaced.getPlacedMeeple());
            gameboardAdapter.getGameBoard().placeTile(tilePlacedByOtherPlayer, tilePlaced.getCoordinates());
            gameboardAdapter.notifyDataSetChanged();
        });


        /*
         * next turn observable
         */
        gameSessionViewModel.getNextTurnMessageLiveData().observe(this, nextTurn -> {
            if (Objects.equals(nextTurn.getPlayerId(), currentPlayer.getId())) {
                Vibrator vibrator;
                if (android.os.Build.VERSION.SDK_INT >= 31) {
                    VibratorManager vibratorManager = (VibratorManager) getSystemService(Context.VIBRATOR_MANAGER_SERVICE);
                    vibrator = vibratorManager.getDefaultVibrator();
                } else {
                    vibrator = (Vibrator) getSystemService(VIBRATOR_SERVICE);
                }

                // Vibrate for 500 milliseconds to inform user that ii is his/her turn
                if (vibrator.hasVibrator()) {
                    vibrator.vibrate(500); // for 500 ms
                }
                tileToPlace = gameBoard.getAllTiles().get(Math.toIntExact(nextTurn.getTileId()));
                previewTileToPlace.setRotation(0);
                gameboardAdapter.setCanPlaceTile(true);
                gameboardAdapter.setYourTurn(true);
                gameboardAdapter.setTileToPlace(tileToPlace);
                previewTileToPlace.setImageResource(
                        getResources().getIdentifier(tileToPlace.getImageName() + "_0", "drawable", getPackageName()));
                binding.buttonConfirmTilePlacement.setVisibility(View.VISIBLE);
                binding.buttonRotateClockwise.setVisibility(View.VISIBLE);
                binding.buttonRotateCounterClockwise.setVisibility(View.VISIBLE);
                binding.previewTileToPlace.setVisibility(View.VISIBLE);
                binding.backgroundRight.setVisibility(View.VISIBLE);

                moveButtonsLeft();
            } else {
                tileToPlace = null;
                gameboardAdapter.setYourTurn(false);
                gameboardAdapter.setCanPlaceTile(false);
                previewTileToPlace.setRotation(0);
                previewTileToPlace.setImageResource(R.drawable.backside);
                buttonConfirm.setVisibility(View.GONE);
                binding.buttonRotateClockwise.setVisibility(View.GONE);
                binding.buttonRotateCounterClockwise.setVisibility(View.GONE);
                binding.previewTileToPlace.setVisibility(View.GONE);
                binding.backgroundRight.setVisibility(View.GONE);

                moveButtonsRight();

            }
            if (gameboardAdapter != null) {
                gameboardAdapter.notifyDataSetChanged();
            }
        });

        if (currentPlayer.getId().toString().equals(currentLobbyAdmin)) {
            gameSessionViewModel.getNextTurn(currentPlayer.getGameSessionId());
        }

        // GameLogic
        confirmTilePlacement();
        confirmMeeplePlacement();
        setupRotationButtons();

        JoystickView joystick = (JoystickView) findViewById(R.id.joystickView);
        joystick.setOnMoveListener((angle, strength) -> {
//            TODO: Future adjust strength based on the scale of the gridView
//               Move across the gridView
            // Convert angle to radians
            double rad = Math.toRadians(angle);

            // Calculate change in X and Y coordinates
            float deltaX = (float) (-strength * Math.cos(rad)); // Negate this to reverse the direction
            float deltaY = (float) (strength * Math.sin(rad));

            // Check if gridView is not null
            if (gridView != null) {
                // Get current translations
                float currentTranslationX = gridView.getTranslationX();
                float currentTranslationY = gridView.getTranslationY();

                // Update translations
                float newX = currentTranslationX + deltaX;
                float newY = currentTranslationY + deltaY;
                gridView.setTranslationX(newX);
                gridView.setTranslationY(newY);
            }
        });

        // Navigating across the game board with buttons
        moveRight();
        moveLeft();
        moveUp();
        moveDown();

        // Zooming In and Out of the game board
        zoomIn();
        zoomOut();

        binding.tvMeepleCount.setText(gameboardAdapter.getMeepleCount() + "x");


    }

    private void moveButtonsRight() {
        ConstraintLayout constraintLayout = binding.main;
        ConstraintSet constraintSet = new ConstraintSet();
        constraintSet.clone(constraintLayout);
        constraintSet.connect(zoomInBtn.getId(), ConstraintSet.END, binding.main.getId(), ConstraintSet.END, 0);
        constraintSet.connect(zoomOutBtn.getId(), ConstraintSet.END, binding.main.getId(), ConstraintSet.END, 0);
        constraintSet.applyTo(constraintLayout);
    }

    private void moveButtonsLeft() {
        ConstraintLayout constraintLayout = binding.main;
        ConstraintSet constraintSet = new ConstraintSet();
        constraintSet.clone(constraintLayout);
        constraintSet.connect(zoomInBtn.getId(), ConstraintSet.END, binding.backgroundRight.getId(), ConstraintSet.START, 0);
        constraintSet.connect(zoomOutBtn.getId(), ConstraintSet.END, binding.backgroundRight.getId(), ConstraintSet.START, 0);
        constraintSet.applyTo(constraintLayout);
    }


    private void confirmNextTurnToStart() {
        gameSessionViewModel.getNextTurn(currentPlayer.getGameSessionId());
        gameboardAdapter.setYourTurn(false);
    }

    private void bindGameBoardUiElements() {
        // Init Grid View
        gridView = binding.gridview;

        // Init Movement Buttons
        buttonLeft = binding.leftScrlBtn;
        buttonRight = binding.rightScrlBtn;
        buttonDown = binding.downScrlBtn;
        buttonUp = binding.upScrlBtn;

        // Init Zoom Buttons
        zoomInBtn = binding.btnZoomIn;
        zoomOutBtn = binding.btnZoomOut;

        // Init Control Elements
        buttonConfirm = binding.buttonConfirmTilePlacement;
        previewTileToPlace = binding.previewTileToPlace;
    }

    private void confirmTilePlacement() {
        buttonConfirm.setOnClickListener(v -> {
            if (gameboardAdapter.getToPlaceCoordinates() != null && gameboardAdapter.isYourTurn() && gameboardAdapter.isCanPlaceTile()) {
                v.startAnimation(scaleAnimation);
                // get the x and y coordinates of the field where the tile should be placed
                int xToPlace = gameboardAdapter.getToPlaceCoordinates().getXPosition();
                int yToPlace = gameboardAdapter.getToPlaceCoordinates().getYPosition();

                tileToPlace.setCoordinates(gameboardAdapter.getToPlaceCoordinates());

                // place the Tile on the gameBoard
                PlacedTileDto placedTileDto = new PlacedTileDto(currentPlayer.getGameSessionId(), tileToPlace.getId(), new Coordinates(xToPlace, yToPlace), tileToPlace.getRotation(), null);
                gameSessionViewModel.sendPlacedTile(placedTileDto);

                buttonConfirm.setVisibility(View.GONE);
                binding.buttonRotateClockwise.setVisibility(View.GONE);
                binding.buttonRotateCounterClockwise.setVisibility(View.GONE);


                gameboardAdapter.setCanPlaceTile(false);
                gameboardAdapter.setCanPlaceMeeple(true);
                gameboardAdapter.notifyDataSetChanged();
                showMeepleGrid();

            } else {
                if (!gameboardAdapter.isYourTurn()) {
                    Toast.makeText(this, "It's not your turn. Please wait.", Toast.LENGTH_SHORT).show();

                } else {
                    Toast.makeText(this, "Please select a valid position", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void confirmMeeplePlacement() {
        binding.buttonConfirmMeeplePlacement.setOnClickListener(v -> {
            if (gameboardAdapter.isYourTurn() && gameboardAdapter.getMeepleCount() > 0) {
                if (meepleAdapter.getPlaceMeepleCoordinates() != null) {
                    // TODO: Dynamically adjust player color - done?
                    Meeple placedMeeple = new Meeple();
                    placedMeeple.setColor(currentPlayer.getPlayerColour());
                    placedMeeple.setPlayerId(currentPlayer.getId());
                    placedMeeple.setPlaced(true);
                    placedMeeple.setCoordinates(meepleAdapter.getPlaceMeepleCoordinates());

                    tileToPlace.setPlacedMeeple(placedMeeple);

                    gameboardAdapter.setMeepleCount(gameboardAdapter.getMeepleCount() - 1);
                    String formattedString = String.format(getString(R.string.meepleCount), gameboardAdapter.getMeepleCount());
                    binding.tvMeepleCount.setText(formattedString);

                    int xToPlace = gameboardAdapter.getToPlaceCoordinates().getXPosition();
                    int yToPlace = gameboardAdapter.getToPlaceCoordinates().getYPosition();
                    PlacedTileDto placedTileDto = new PlacedTileDto(currentPlayer.getGameSessionId(), tileToPlace.getId(), new Coordinates(xToPlace, yToPlace), tileToPlace.getRotation(), placedMeeple);
                    gameSessionViewModel.sendPlacedTile(placedTileDto);
                }

                gameboardAdapter.setCanPlaceMeeple(false);
                gameboardAdapter.notifyDataSetChanged();

                binding.buttonConfirmMeeplePlacement.setVisibility(View.GONE);

                hideMeepleGrid();
                gameboardAdapter.setToPlaceCoordinates(null);

                confirmNextTurnToStart();
            }
        });
    }

    private void showMeepleGrid() {
        if (gameboardAdapter.getMeepleCount() > 0) {
            binding.overlayGridview.setVisibility(View.VISIBLE);
            RoadResult roadResult = roadCalculator.getAllTilesThatArePartOfRoad(tileToPlace);
            meepleAdapter = new MeepleAdapter(this, tileToPlace, !roadResult.hasMeepleOnRoad());
            binding.overlayGridview.setAdapter(meepleAdapter);
            binding.buttonConfirmMeeplePlacement.setVisibility(View.VISIBLE);
        } else {
            gameboardAdapter.setCanPlaceMeeple(false);
            gameboardAdapter.setToPlaceCoordinates(null);
            hideMeepleGrid();
            confirmNextTurnToStart();
            gameboardAdapter.notifyDataSetChanged();
        }
    }

    private void hideMeepleGrid() {
        binding.overlayGridview.setVisibility(View.GONE);
        // gameboardAdapter.notifyDataSetChanged();
    }

    private void zoomOut() {
        zoomOutBtn.setOnClickListener(v -> {
            v.startAnimation(scaleAnimation);
            float currentScaleX = gridView.getScaleX();
            float currentScaleY = gridView.getScaleY();
            if (gridView != null && currentScaleX > 1.0f && currentScaleY > 1.0f) {
                float newScaleX = currentScaleX - 0.5f;
                float newScaleY = currentScaleY - 0.5f;
                gridView.setScaleX(newScaleX);
                gridView.setScaleY(newScaleY);
            }
        });
    }

    private void zoomIn() {
        zoomInBtn.setOnClickListener(v -> {
            v.startAnimation(scaleAnimation);
            float currentScaleX = gridView.getScaleX();
            float currentScaleY = gridView.getScaleY();
            if (gridView != null && currentScaleX < 5.0f && currentScaleY < 5.0f) {
                float newScaleX = currentScaleX + 0.5f;
                float newScaleY = currentScaleY + 0.5f;
                gridView.setScaleX(newScaleX);
                gridView.setScaleY(newScaleY);
            }
        });
    }


    private void moveDown() {
        buttonDown.setOnClickListener(v -> {
            if (gridView != null) {
                v.startAnimation(scaleAnimation);
                float currentTranslationY = gridView.getTranslationY();
                float newY = currentTranslationY - 300;
                gridView.setTranslationY(newY);
            }
        });
    }

    private void moveUp() {
        buttonUp.setOnClickListener(v -> {
            if (gridView != null) {
                v.startAnimation(scaleAnimation);
                float currentTranslationY = gridView.getTranslationY();
                float newY = currentTranslationY + 300;
                gridView.setTranslationY(newY);
            }
        });
    }

    private void moveLeft() {
        buttonLeft.setOnClickListener(v -> {
            if (gridView != null) {
                v.startAnimation(scaleAnimation);
                float currentTranslationX = gridView.getTranslationX();
                float newX = currentTranslationX + 300;
                gridView.setTranslationX(newX);
            }
        });
    }

    private void moveRight() {
        buttonRight.setOnClickListener(v -> {
            if (gridView != null) {
                v.startAnimation(scaleAnimation);
                float currentTranslationX = gridView.getTranslationX();
                float newX = currentTranslationX - 300;
                gridView.setTranslationX(newX);
            }
        });
    }

    private void setupRotationButtons() {
        final ImageRotator imageRotator = new ImageRotator(previewTileToPlace);
        binding.buttonRotateClockwise.setOnClickListener(v -> {
            if (gameboardAdapter.isYourTurn() && gameboardAdapter.isCanPlaceTile()) {
                v.startAnimation(scaleAnimation);
                imageRotator.rotateRight();
                tileToPlace.rotate(true);
                gameboardAdapter.setCurrentTileRotation(tileToPlace);
            }
        });

        binding.buttonRotateCounterClockwise.setOnClickListener(v -> {
            if (gameboardAdapter.isYourTurn() && gameboardAdapter.isCanPlaceTile()) {
                v.startAnimation(scaleAnimation);
                imageRotator.rotateLeft();
                tileToPlace.rotate(false);
                gameboardAdapter.setCurrentTileRotation(tileToPlace);
            }
        });
    }
}
