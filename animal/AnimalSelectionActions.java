package com.csse3200.game.components.animal;

import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.Dialog;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.csse3200.game.GdxGame;
import com.csse3200.game.gamestate.GameState;
import com.csse3200.game.screens.LoadingScreen;
import com.csse3200.game.ui.AlertBox;
import com.csse3200.game.ui.PopUpDialogBox.PopUpHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.csse3200.game.components.animal.AnimalSelectionDisplay;

/**
 * Handles actions related to animal selection in the game.
 * This class manages the interactions with animal selection images and buttons and etc.
 */
public class AnimalSelectionActions {
    private static final Logger logger = LoggerFactory.getLogger(AnimalSelectionActions.class);
    final AnimalSelectionDisplay display;
    private final PopUpHelper dialogHelper;
    static Image selectedAnimalImage;
    private final GdxGame game;
    static String selectedAnimalImagePath;

    /**
     * Constructs an instance of the class.
     * @param display the display containing animal images and buttons
     * @param dialogHelper helper for displaying dialogs
     * @param game the game instance to change screens
     */
    public AnimalSelectionActions(AnimalSelectionDisplay display, PopUpHelper dialogHelper, GdxGame game) {
        this.display = display;
        this.dialogHelper = dialogHelper;
        this.game = game;
        addListeners(); // Add listeners to handle user interactions
    }

    /**
     * Gets the path of the currently selected animal image.
     * @return the path of the selected animal image
     */
    public static String getSelectedAnimalImagePath() {
        return selectedAnimalImagePath;
    }

    /**
     * Adds click listeners to animal images and buttons, as well as other interface buttons.
     */
    private void addListeners() {
        Image[] animalImages = display.getAnimalImages();
        TextButton[] animalButtons = display.getAnimalButtons();
        String[] animalImagePaths = {
                "images/dog.png",
                "images/croc.png",
                "images/bird.png"
        };

        // Add listeners to animal images and buttons
        for (int i = 0; i < animalImages.length; i++) {
            final int animalIndex = i;
            final String animalImagePath = animalImagePaths[i];

            animalImages[i].addListener(new ClickListener() {
                @Override
                public void clicked(InputEvent event, float x, float y) {
                    logger.debug("Animal {} image clicked", animalIndex + 1);
                    selectAnimal(animalImages[animalIndex], animalImagePath);
                    showAnimalDialog(animalIndex, animalImagePath);
                }
            });

            animalButtons[i].addListener(new ClickListener() {
                @Override
                public void clicked(InputEvent event, float x, float y) {
                    logger.debug("Animal {} button clicked", animalIndex + 1);
                    selectAnimal(animalImages[animalIndex], animalImagePath);
                    showAnimalDialog(animalIndex, animalImagePath);
                }
            });
        }

        // Add listener to the "Select" button to proceed with the selected animal
        display.getSelectButton().addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                if (selectedAnimalImage != null) {
                    logger.debug("Select button clicked with animal selected");
                    game.setScreen(GdxGame.ScreenType.STORY);
                } else {
                    logger.debug("No animal selected");
                    showSelectionAlert(); // Show an alert if no animal is selected
                }
            }
        });

        // Add listener to the "Back" button to return to the main menu
        display.getBackButton().addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                logger.debug("Go back button clicked");
                game.setScreen(GdxGame.ScreenType.MAIN_MENU);
            }
        });
    }

    /**
     * Updates the currently selected animal and highlights the selected image.
     * @param animalImage the image of the selected animal
     * @param animalImagePath the path to the image of the selected animal
     */


    void selectAnimal(Image animalImage, String animalImagePath) {
        if (selectedAnimalImage != null) {
            selectedAnimalImage.setColor(1, 1, 1, 1); // Reset color of the previously selected image
        }

        selectedAnimalImage = animalImage;
        selectedAnimalImagePath = animalImagePath;
        GameState.player.selectedAnimalPath = animalImagePath;
        selectedAnimalImage.setColor(1, 0, 0, 1); // Highlight the selected image

        logger.debug("Animal selected: {}", animalImage.getName());
    }

    /**
     * Shows an alert dialog when no animal is selected and the select button is clicked.
     */

    private void showSelectionAlert() {
        AlertBox alertBox = new AlertBox("Please select an animal first.", display.getSkin(), 400f, 200f);
        alertBox.display(display.getStage());
    }


    /**
     * Displays a dialog with information about the selected animal.
     * @param animalIndex the index of the selected animal
     * @param animalImagePath the path to the image of the selected animal
     */

    void showAnimalDialog(int animalIndex, String animalImagePath) {
        String title = "Animal " + (animalIndex + 1);
        String content = switch (animalIndex) {
            case 0 -> // Dog
                    "You've selected a Dog. This animal is loyal, brave, and agile. It excels in combat with its speed and determination.";
            case 1 -> // Crocodile
                    "You've selected a Crocodile. This animal is strong, cunning, and resilient. It possesses incredible defensive and offensive capabilities.";
            case 2 -> // Bird
                    "You've selected a Bird. This animal is fast, intelligent, and free. It can outmaneuver opponents and attack from the skies.";
            default -> "You've selected Animal " + (animalIndex + 1) + ".\n" +
                    "This animal has unique characteristics.\n" +
                    "It possesses special abilities.";
        };

        dialogHelper.displayDialog(title, content, animalImagePath, 900f, 500f, animalIndex);
    }

    /**
     * Resets the animal selection state
     * This method clears the currently selected animal by setting the
     * selectedAnimalImage and selectedAnimalImagePath to null. This ensures
     * that no residual selection data is carried over between sessions or
     * screen transitions, prompting the user to select an animal afresh.
     *
     */
    public void resetSelection() {
        selectedAnimalImage = null;
        selectedAnimalImagePath = null;
    }

}
