package com.csse3200.game.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.csse3200.game.GdxGame;
import com.csse3200.game.areas.MapHandler;
import com.csse3200.game.components.minigames.MiniGameConstants;
import com.csse3200.game.components.minigames.MiniGameMedals;
import com.csse3200.game.components.minigames.MiniGameNames;
import com.csse3200.game.components.player.PlayerInventoryDisplay;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.inventory.items.lootbox.configs.EarlyGameLootTable;
import com.csse3200.game.inventory.items.lootbox.configs.LateGameLootTable;
import com.csse3200.game.inventory.items.lootbox.configs.MediumGameLootTable;
import com.csse3200.game.inventory.items.lootbox.rarities.EarlyGameLootBox;
import com.csse3200.game.inventory.items.lootbox.rarities.LateGameLootBox;
import com.csse3200.game.inventory.items.lootbox.rarities.MediumGameLootBox;
import com.csse3200.game.services.ServiceContainer;
import org.slf4j.LoggerFactory;
import org.slf4j.Logger;

import static com.csse3200.game.components.minigames.MiniGameNames.BIRD;
import static com.csse3200.game.components.minigames.MiniGameNames.SNAKE;

/**
 * Makes a new screen when the snake game is over.
 * Displays the stats and add buttons to exit and restart.
 */
public class EndMiniGameScreen extends ScreenAdapter {
    private static final Logger logger = LoggerFactory.getLogger(EndMiniGameScreen.class);
    private final GdxGame game;
    private final int score;
    private final MiniGameNames gameName;
    private final Stage stage;
    private final Skin skin;
    private float scale;

    // fonts
    private final BitmapFont font18;
    private final BitmapFont font26;
    private final BitmapFont font32;
    private final Screen oldScreen;
    private final ServiceContainer oldScreenServices;

    private final Entity player;
    private PlayerInventoryDisplay display;

    public EndMiniGameScreen(GdxGame game, int score, MiniGameNames gameName, Screen screen, ServiceContainer container) {
        this.game = game;
        this.score = score;
        this.gameName = gameName;
        this.scale = 1;
        this.oldScreen = screen;
        this.oldScreenServices = container;

        this.stage = new Stage(new ScreenViewport());
        this.skin = new Skin(Gdx.files.internal("flat-earth/skin/flat-earth-ui.json"));

        this.font18 = new BitmapFont(Gdx.files.internal("flat-earth/skin/fonts/pixel_18.fnt"));
        this.font26 = new BitmapFont(Gdx.files.internal("flat-earth/skin/fonts/pixel_26.fnt"));
        this.font32 = new BitmapFont(Gdx.files.internal("flat-earth/skin/fonts/pixel_32.fnt"));

        if (oldScreen instanceof MainGameScreen) {
            MainGameScreen forestGameArea = (MainGameScreen) oldScreen;
            this.player = MapHandler.getCurrentMap().getPlayer();
            if (player != null) {
                logger.info("Adding loot box to player's inventory.");
                this.display = player.getComponent(PlayerInventoryDisplay.class);
                // Rewarding achievement to player
                logger.info("Achievement trigger {} {}", gameName.name(),getMedal(score).name());
                player.getEvents().trigger("miniGame",gameName,getMedal(score));
            }
        } else {
            this.player = null;
            this.display = null;
        }
        Gdx.input.setInputProcessor(stage);

        setupExitButton();
    }

    /**
     * Puts the exit button in the top right of the screen.
     * Will take the user back to the Main menu screen
     */
    private void setupExitButton() {

        TextButton exitButton = new TextButton("Exit", skin);
        // Scale the button's font
        exitButton.getLabel().setFontScale(scale);

        exitButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                // Return to main menu and original screen colour
                Gdx.gl.glClearColor(248f / 255f, 249f / 255f, 178f / 255f, 1f);
                switch(getMedal(score)) {
                    case BRONZE -> display.getEntity().getEvents().trigger("addItem", new EarlyGameLootBox(
                            new EarlyGameLootTable(),3 , player));
                    case SILVER -> display.getEntity().getEvents().trigger("addItem", new MediumGameLootBox(
                            new MediumGameLootTable(),3 , player));
                    case GOLD -> display.getEntity().getEvents().trigger("addItem", new LateGameLootBox(
                            new LateGameLootTable(),3 , player));
                }
                game.setOldScreen(oldScreen, oldScreenServices);
            }
        });

        // Set up the table for UI layout
        Table table = new Table();
        table.setFillParent(true);
        table.top().right();
        table.add(exitButton).width(exitButton.getWidth() * scale).height(exitButton.getHeight() * scale).center().pad(10 * scale).row();


        // Add the table to the stage
        stage.addActor(table);
    }

    /**
     * Renders the screen. Sets the background colour, draws the UI elements (buttons) and
     * renders the message labels and handles key presses
     * @param delta The time in seconds since the last render.
     */
    @Override
    public void render(float delta) {
        // Set the background color based on the score
        setBackgroundColor();

        // Draw the exit button and other UI elements
        stage.act(Gdx.graphics.getDeltaTime());
        stage.draw();

        // Render the game over messages
        renderEndMessage();

        handleKeyPress();
    }

    /**
     * Changes the screen if escape or R is pressed (to mini-games menu or back to game respectively)
     */
    private void handleKeyPress() {

        // Key functionality for escape and restart
        if (Gdx.input.isKeyJustPressed(Input.Keys.R)) {  // Restart game
            dispose();
            if (gameName == SNAKE) {
                game.setScreen(new SnakeScreen(game, oldScreen, oldScreenServices));
            }
            else if (gameName == BIRD) {
                game.setScreen(new BirdieDashScreen(game, oldScreen, oldScreenServices));
            } else {
                //TODO: add Maze screen
            }
        }

        if (Gdx.input.isKeyJustPressed(Input.Keys.BACKSPACE)) {  // Go to Mini-games menu
            Gdx.gl.glClearColor(248f / 255f, 249f / 255f, 178f / 255f, 1f);
            game.setOldScreen(oldScreen, oldScreenServices);
        }
    }

    /**
     * Renders the labels with score, message and title.
     * Renders the try again and menu buttons
     */
    private void renderEndMessage() {

        Table table = new Table();
        table.setFillParent(true);

        // End of Mini-Game label
        font32.getData().setScale(3f * scale);
        Label.LabelStyle labelStyle = new Label.LabelStyle(font32, Color.WHITE);
        Label endGameLabel = new Label("End of Mini-Game", labelStyle);
        table.add(endGameLabel).center().padBottom(80 * scale).row();
        table.row();

        // Score label
        font26.getData().setScale(2f * scale);
        labelStyle = new Label.LabelStyle(font26, Color.WHITE);
        Label scoreLabel = new Label("Score: " + score, labelStyle);
        table.add(scoreLabel).center().padBottom(50 * scale).row();
        table.row();

        // Medal label
        MiniGameMedals medal = getMedal(score);
        if (medal == MiniGameMedals.FAIL) {
            font26.getData().setScale(2f * scale);
            labelStyle = new Label.LabelStyle(font26, Color.WHITE);
            Label medalLabel = new Label("You FAILED", labelStyle);
            table.add(medalLabel).center().padBottom(150 * scale).row();
            table.row();

        } else {
            font26.getData().setScale(2f * scale);
            labelStyle = new Label.LabelStyle(font26, Color.WHITE);
            Label medalLabel = new Label("You got a " + medal + " Medal :)", labelStyle);
            table.add(medalLabel).center().padBottom(150 * scale).row();
            table.row();
        }

        // Personalised message label
        font18.getData().setScale(2f * scale);
        labelStyle = new Label.LabelStyle(font18, Color.WHITE);
        String scoreMessage = getMessage();
        Label scoreMessageLabel = new Label(scoreMessage, labelStyle);
        table.add(scoreMessageLabel).center().padBottom(100 * scale);
        table.row();

        // Add buttons to the table
        TextButton tryAgainButton = new TextButton("Try Again", skin);
        // Scale the button's font
        tryAgainButton.getLabel().setFontScale(scale);

        // Scale the button's size
        tryAgainButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                dispose();
                if (gameName == SNAKE) {
                    game.setScreen(new SnakeScreen(game, oldScreen, oldScreenServices));
                }
                else if (gameName == BIRD) {
                    game.setScreen(new BirdieDashScreen(game, oldScreen, oldScreenServices));
                } else {
                    //TODO: add Maze screen
                }
            }
        });

        TextButton menuButton = new TextButton("Mini-Game Menu", skin);
        // Scale the button's font
        menuButton.getLabel().setFontScale(scale);

        // Scale the button's size
        menuButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                Gdx.gl.glClearColor(248f / 255f, 249f / 255f, 178f / 255f, 1f);
                game.setOldScreen(oldScreen, oldScreenServices);
            }
        });

        // Add buttons to the table and align them at the bottom
        table.add(tryAgainButton).width(tryAgainButton.getWidth() * scale).height(tryAgainButton.getHeight() * scale).pad(10 * scale).row();
        table.add(menuButton).width(menuButton.getWidth() * scale).height(menuButton.getHeight() * scale).center().pad(10 * scale).row();

        stage.addActor(table);
    }

    /**
     * Get the medal associated with the players score for current game
     * @param score: the players score
     * @return the medal associated with the score
     */
    private MiniGameMedals getMedal(int score) {

        // Get the medal thresholds for each game
        int bronzeThreshold, silverThreshold, goldThreshold;
        switch (gameName) {
            case SNAKE -> {
                bronzeThreshold = MiniGameConstants.SNAKE_BRONZE_THRESHOLD;
                silverThreshold = MiniGameConstants.SNAKE_SILVER_THRESHOLD;
                goldThreshold = MiniGameConstants.SNAKE_GOLD_THRESHOLD;
            }
            case BIRD -> {
                bronzeThreshold = MiniGameConstants.BIRDIE_DASH_BRONZE_THRESHOLD;
                silverThreshold = MiniGameConstants.BIRDIE_DASH_SILVER_THRESHOLD;
                goldThreshold = MiniGameConstants.BIRDIE_DASH_GOLD_THRESHOLD;
            }
            case MAZE -> {
                bronzeThreshold = MiniGameConstants.MAZE_BRONZE_THRESHOLD;
                silverThreshold = MiniGameConstants.MAZE_SILVER_THRESHOLD;
                goldThreshold = MiniGameConstants.MAZE_GOLD_THRESHOLD;
            }
            default -> throw new IllegalArgumentException("Unknown mini-game: " + game);
        }

        // Return the medal
        if (score < bronzeThreshold) {
            // Fail
            return MiniGameMedals.FAIL;
        } else if (score < silverThreshold) {
            // Bronze
            return MiniGameMedals.BRONZE;
        } else if (score < goldThreshold) {
            // Silver
            return MiniGameMedals.SILVER;
        } else {
            // Gold
            return MiniGameMedals.GOLD;
        }
    }

    /**
     * Changes the background colour based on sore/ medals (fail: green, bronze, silver and gold)
     */
    private void setBackgroundColor() {

        switch (getMedal(score)) {
            case FAIL ->
                // Failed
                // Background colour rgb 50, 82, 29, 1
                    Gdx.gl.glClearColor(50f / 255f, 82f / 255f, 29f / 255f, 1f);
            case BRONZE ->
                // Bronze
                // Background colour rgb 169, 113, 66, 1
                    Gdx.gl.glClearColor(169f / 255f, 113f / 255f, 66f / 255f, 1f);
            case SILVER ->
                // Silver
                // Background colour rgb 115, 122, 140, 1
                    Gdx.gl.glClearColor(115f / 255f, 122f / 255f, 140f / 255f, 1f);
            case GOLD ->
                // Gold
                // Background colour rgb 173, 162, 114, 1
                    Gdx.gl.glClearColor(173f / 255f, 162f / 255f, 114f / 255f, 1f);
            default -> throw new IllegalArgumentException("Unknown mini-game");
        }
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
    }

    /**
     * Gets the personalised massage based on score and mini-game
     * @return the message
     */
    private String getMessage() {
        int bronzeMedalThreshold, silverMedalThreshold, goldMedalThreshold;
        String failMessage, bronzeMessage, silverMessage, goldMessage;

        switch (gameName) {
            case SNAKE -> {
                bronzeMedalThreshold = MiniGameConstants.SNAKE_BRONZE_THRESHOLD;
                silverMedalThreshold = MiniGameConstants.SNAKE_SILVER_THRESHOLD;
                goldMedalThreshold = MiniGameConstants.SNAKE_GOLD_THRESHOLD;
                failMessage = "Damn that was a small snake...";
                bronzeMessage = "Nawww, look he's growing";
                silverMessage = "That's a really big snake alright";
                goldMessage = "Snake king!";
            }
            case BIRD -> {
                bronzeMedalThreshold = MiniGameConstants.BIRDIE_DASH_BRONZE_THRESHOLD;
                silverMedalThreshold = MiniGameConstants.BIRDIE_DASH_SILVER_THRESHOLD;
                goldMedalThreshold = MiniGameConstants.BIRDIE_DASH_GOLD_THRESHOLD;
                failMessage = "You're broke, maybe talk to Centerlink?";
                bronzeMessage = "Almost middle class";
                silverMessage = "Damn she rich";
                goldMessage = "Donate to a poor software engineering student? Please :)";
            }
            case MAZE -> {
                bronzeMedalThreshold = MiniGameConstants.MAZE_BRONZE_THRESHOLD;
                silverMedalThreshold = MiniGameConstants.MAZE_SILVER_THRESHOLD;
                goldMedalThreshold = MiniGameConstants.MAZE_GOLD_THRESHOLD;
                failMessage = "Maze message FAIL";
                bronzeMessage = "Maze message BRONZE";
                silverMessage = "Maze message SILVER";
                goldMessage = "Maze message GOLD";
            }
            default -> throw new IllegalArgumentException("Unknown mini-game");
        }

        if (score < bronzeMedalThreshold) {
            return failMessage;
        } else if (score < silverMedalThreshold) {
            return bronzeMessage;
        } else if (score < goldMedalThreshold) {
            return silverMessage;
        } else {
            return goldMessage;
        }
    }

    /**
     * Dispose the fonts, skin and stage.
     */
    @Override
    public void dispose() {
        font18.dispose();
        font26.dispose();
        font32.dispose();
        stage.dispose();
        skin.dispose();
    }

    /**
     * Resize function that automatically gets called when the screen is resized.
     * Resizes all components with a consistent scale to maintain the screen's
     * original design.
     * @param width The width of the resized screen.
     * @param height The height of the resized screen.
     */
    @Override
    public void resize(int width, int height) {
        // Update the stage viewport
        stage.getViewport().update(width, height, true);
        float baseWidth = 1920f;
        float baseHeight = 1200f;
        float scaleWidth = width / baseWidth;
        float scaleHeight = height / baseHeight;
        scale = Math.min(scaleWidth, scaleHeight);
        stage.clear();
        setupExitButton();
        renderEndMessage();
    }
}
