package com.csse3200.game.components.maingame;

import com.badlogic.gdx.Screen;
import com.csse3200.game.GdxGame;
import com.csse3200.game.components.Component;
import com.csse3200.game.gamestate.GameState;
import com.csse3200.game.gamestate.SaveHandler;
import com.csse3200.game.services.ServiceContainer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class listens to events relevant to the Main Game Screen and does something when one of the
 * events is triggered.
 */
public class MainGameActions extends Component {
  private static final Logger logger = LoggerFactory.getLogger(MainGameActions.class);
  private final GdxGame game;

  public MainGameActions(GdxGame game) {
    this.game = game;
  }

  @Override
  public void create() {
    entity.getEvents().addListener("returnToMainGame", this::onReturnToMainGame);
  }

  /**
   * Swaps to the Main Menu screen.
   */
  public void onExit() {
    logger.info("Exiting main game screen");
    game.setScreen(GdxGame.ScreenType.MAIN_MENU);
  }

  /**
   * returns to an old screen
   * @param screen Screen object to be returned to
   * @param container ServiceContainer that contains all the services for the
   *                  screen that is being returned to
   */
  private void onReturnToMainGame(Screen screen, ServiceContainer container) {
    logger.info("Returning to main game screen");
    // change to new GDXgame function
    game.setScreen(GdxGame.ScreenType.MAIN_GAME);
  }
}
