package com.csse3200.game.areas;

import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.math.GridPoint2;
import com.csse3200.game.GdxGame;
import com.csse3200.game.areas.ForestGameAreaConfigs.*;
import com.csse3200.game.areas.terrain.TerrainFactory;
import com.csse3200.game.areas.terrain.TerrainFactory.TerrainType;
import com.csse3200.game.components.ProximityComponent;
import com.csse3200.game.components.quests.QuestPopup;
import com.csse3200.game.components.settingsmenu.UserSettings;
import com.csse3200.game.entities.Entity;
import com.badlogic.gdx.math.Vector2;
import com.csse3200.game.areas.ForestGameAreaConfigs.*;
import com.csse3200.game.areas.MapHandler.MapType;
import com.csse3200.game.areas.terrain.TerrainChunk;
import com.csse3200.game.areas.terrain.TerrainComponent;
import com.csse3200.game.entities.factories.*;
import com.csse3200.game.files.FileLoader;
import com.csse3200.game.areas.terrain.TerrainLoader;
import com.csse3200.game.utils.math.RandomUtils;
import com.csse3200.game.services.AudioManager;
import com.csse3200.game.services.ResourceService;
import com.csse3200.game.services.ServiceLocator;
import com.csse3200.game.components.gamearea.GameAreaDisplay;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.*;
import java.util.function.Supplier;

/////////////////// This is the temporary game area to test map switching //////////////////////
/** Forest area for the demo game with trees, a player, and some enemies. */
public class WaterGameArea extends GameArea {
  private static final Logger logger = LoggerFactory.getLogger(WaterGameArea.class);
  private static final ForestGameAreaConfig config = new ForestGameAreaConfig();
  private static final GridPoint2 MAP_SIZE = new GridPoint2(100, 100);
  private static final GridPoint2 PLAYER_SPAWN = new GridPoint2(2, 2);
  private final TerrainFactory terrainFactory;
  private final List<Entity> enemies;
  private final Map<Integer, Entity> dynamicItems = new HashMap<>();
  private int totalItems = 0;
  private Entity player;

  private final GdxGame game;

  // Boolean to ensure that only a single boss entity is spawned when a trigger happens
  private boolean kangarooBossSpawned = false;

  /**
   * Initialise this ForestGameArea to use the provided TerrainFactory.
   * @param terrainFactory TerrainFactory used to create the terrain for the GameArea.
   * @param game GdxGame needed for creating the player
   * @requires terrainFactory != null
   */
  public WaterGameArea(TerrainFactory terrainFactory, GdxGame game) {
    super();
    this.enemies = new ArrayList<>();
    this.terrainFactory = terrainFactory;
    this.game = game;
  }

  /**
   * Create the game area, including terrain, static entities (trees),
   * dynamic entities (player and NPCs), and ui
   * */
  @Override
  public void create() {
    loadAssets();

    displayUI();

    // Terrain
    spawnTerrain();

    // Player
    player = spawnPlayer();
    //logger.debug("Player is at ({}, {})", player.getPosition().x, player.getPosition().y);
    TerrainLoader.setInitials(player.getPosition(), terrain);

    // Obstacles
    spawnTrees();

    //Enemies
    spawnEnemies();

    // items
    handleItems();

    //Friendlies
    //spawnFriendlyNPCs();

    //playMusic();
    //player.getEvents().addListener("setPosition", this::handleNewChunks);
    player.getEvents().addListener("spawnKangaBoss", this::spawnKangarooBoss);
    kangarooBossSpawned = false;
  }

  private void handleNewChunks(Vector2 playerPos) {
    if (TerrainLoader.movedChunk(playerPos)) {
      logger.info("Player position is: ({}, {})", playerPos.x, playerPos.y);
      handleItems();
    }
  }

  private void handleItems() {
    // Spawn items on new chunks: TODO: ADD THIS TO A LIST OF DYNAMIC ENTITIES IN SPAWNER!
    for (GridPoint2 pos : terrain.getNewChunks()) {
      spawnItems(TerrainLoader.chunktoWorldPos(pos));
    }

    List<Integer> removals = new ArrayList<>();
    for (int key : dynamicItems.keySet()) {
      GridPoint2 chunkPos = TerrainLoader.posToChunk(dynamicItems.get(key).getPosition());
      if (!terrain.getActiveChunks().contains(chunkPos)) {
        removals.add(key);
      }
    }

    for (int i : removals) {
      dynamicItems.remove(i);
    }
  }

  /**
   * gets the player
   * @return player entity
   */
  @Override
  public Entity getPlayer () {
    return player;
  }

  public void displayUI() {
    Entity ui = new Entity();
    ui.addComponent(new GameAreaDisplay("Box Forest"));
    ui.addComponent(new QuestPopup());
    spawnEntity(ui);
  }

  private void spawnTerrain() {
    // Background terrain
    terrain = terrainFactory.createTerrain(TerrainType.FOREST_DEMO, PLAYER_SPAWN, MAP_SIZE, MapType.FOREST);
    spawnEntity(new Entity().addComponent(terrain));
  }

  private void spawnTrees() {
    GridPoint2 minPos = new GridPoint2(PLAYER_SPAWN.x - 10, PLAYER_SPAWN.y - 10);
    GridPoint2 maxPos = new GridPoint2(PLAYER_SPAWN.x + 10, PLAYER_SPAWN.y + 10);

    for (int i = 0; i < config.spawns.NUM_TREES; i++) {
      GridPoint2 randomPos = RandomUtils.random(minPos, maxPos);
      Entity tree = ObstacleFactory.createTree();
      spawnEntityAt(tree, randomPos, true, false);
    }
  }

  /**
   * Creates the player entity for this screen
   * @return the player entity
   */
  private Entity spawnPlayer() {
    Entity newPlayer = PlayerFactory.createPlayer(game);
    newPlayer.addComponent(this.terrainFactory.getCameraComponent());
    spawnEntityAt(newPlayer, PLAYER_SPAWN, true, true);
    return newPlayer;
  }

    private void spawnKangarooBoss() {
        if (!kangarooBossSpawned) {
            Entity kangarooBoss = EnemyFactory.createKangaBossEntity(player);
            spawnEntityOnMap(kangarooBoss);
            kangarooBossSpawned = true;
        }
    }

  private void spawnEntityOnMap(Entity entity) {
    GridPoint2 minPos = new GridPoint2(PLAYER_SPAWN.x - 10, PLAYER_SPAWN.y - 10);
    GridPoint2 maxPos = new GridPoint2(PLAYER_SPAWN.x + 10, PLAYER_SPAWN.y + 10);
    GridPoint2 randomPos = RandomUtils.random(minPos, maxPos);
    spawnEntityAt(entity, randomPos, true, true);
  }

  private void spawnItems(GridPoint2 pos) {
    Supplier<Entity> generator;

    // Health Potions
    generator = () -> ItemFactory.createHealthPotion(player);
    spawnRandomItem(pos, generator, config.spawns.NUM_HEALTH_POTIONS);

    // Defense Potions
    generator = () -> ItemFactory.createDefensePotion(player);
    spawnRandomItem(pos, generator, config.spawns.NUM_DEFENSE_POTIONS);

  // Attack potions
    generator = () -> ItemFactory.createAttackPotion(player);
    spawnRandomItem(pos, generator, config.spawns.NUM_ATTACK_POTIONS);

    // Speed potions
    generator = () -> ItemFactory.createSpeedPotion(player);
    spawnRandomItem(pos, generator, config.spawns.NUM_SPEED_POTIONS);

    // Apples
    generator = () -> ItemFactory.createApple(player);
    spawnRandomItem(pos, generator, config.spawns.NUM_APPLES);

    // Carrots
    generator = () -> ItemFactory.createCarrot(player);
    spawnRandomItem(pos, generator, config.spawns.NUM_CARROTS);

    // Meat
    generator = () -> ItemFactory.createMeat(player);
    spawnRandomItem(pos, generator, config.spawns.NUM_MEAT);

    // Chicken legs
    generator = () -> ItemFactory.createChickenLeg(player);
    spawnRandomItem(pos, generator, config.spawns.NUM_CHICKEN_LEGS);

    // Candy
    generator = () -> ItemFactory.createCandy(player);
    spawnRandomItem(pos, generator, config.spawns.NUM_CANDY);
  }

  private void spawnEnemies() {
    Supplier<Entity> generator;

    // Chicken
    generator = () -> EnemyFactory.createChicken(player);
    spawnRandomEnemy(generator, config.spawns.NUM_CHICKENS, 0.05);

    // Monkey
    generator = () -> EnemyFactory.createMonkey(player);
    spawnRandomEnemy(generator, config.spawns.NUM_MONKEYS, 0.04);

    // Frog
    generator = () -> EnemyFactory.createFrog(player);
    spawnRandomEnemy(generator, config.spawns.NUM_FROGS, 0.06);
  }

  private void spawnFriendlyNPCs() {
    Supplier<Entity> generator;

    // Cow
    generator = () -> NPCFactory.createCow(player, this.enemies);
    spawnRandomNPC(generator, config.spawns.NUM_COWS);

    // Lion
    generator = () -> NPCFactory.createLion(player, this.enemies);
    spawnRandomNPC(generator, config.spawns.NUM_LIONS);

    // Turtle
    generator = () -> NPCFactory.createTurtle(player, this.enemies);
    spawnRandomNPC(generator, config.spawns.NUM_TURTLES);

    // Eagle
    generator = () -> NPCFactory.createEagle(player, this.enemies);
    spawnRandomNPC(generator, config.spawns.NUM_EAGLES);

    // Snake
    generator = () -> NPCFactory.createSnake(player, this.enemies);
    spawnRandomNPC(generator, config.spawns.NUM_SNAKES);
  }

  private void spawnRandomItem(GridPoint2 pos, Supplier<Entity> creator, int numItems) {
    GridPoint2 minPos = new GridPoint2(pos.x - 20, pos.y - 20);
    GridPoint2 maxPos = new GridPoint2(pos.x + 20, pos.y + 20);

    for (int i = 0; i < numItems; i++) {
      GridPoint2 randomPos = RandomUtils.random(minPos, maxPos);
      Entity item = creator.get();
      spawnEntityAt(item, randomPos, true, false);
      dynamicItems.put(totalItems, item);
      totalItems++;
    }
  }

  private void spawnRandomNPC(Supplier<Entity> creator, int numNPCs) {
    GridPoint2 minPos = new GridPoint2(PLAYER_SPAWN.x - 10, PLAYER_SPAWN.y - 10);
    GridPoint2 maxPos = new GridPoint2(PLAYER_SPAWN.x + 10, PLAYER_SPAWN.y + 10);

    for (int i = 0; i < numNPCs; i++) {
      GridPoint2 randomPos = RandomUtils.random(minPos, maxPos);
      Entity npc = creator.get();
      spawnEntityAt(npc, randomPos, true, false);
    }
  }

  private void spawnRandomEnemy(Supplier<Entity> creator, int numItems, double proximityRange) {
    GridPoint2 minPos = new GridPoint2(PLAYER_SPAWN.x - 20, PLAYER_SPAWN.y - 20);
    GridPoint2 maxPos = new GridPoint2(PLAYER_SPAWN.x + 20, PLAYER_SPAWN.y + 20);

    for (int i = 0; i < numItems; i++) {
      GridPoint2 randomPos = RandomUtils.random(minPos, maxPos);
      Entity enemy = creator.get();
      spawnEntityAt(enemy, randomPos, true, false);
      enemies.add(enemy);
      enemy.addComponent(new ProximityComponent(player, proximityRange)); // Add ProximityComponent
    }
  }

  public void loadAssets() {
    logger.debug("LOADING ASSETS");
    ResourceService resourceService = ServiceLocator.getResourceService();
    resourceService.loadTextures(config.textures.forestTextures);
    resourceService.loadTextureAtlases(config.textures.forestTextureAtlases);
    resourceService.loadSounds(config.sounds.gameSounds);
    resourceService.loadMusic(config.sounds.gameMusic);

    while (!resourceService.loadForMillis(10)) {
      // This could be upgraded to a loading screen
      logger.debug("Loading... {}%", resourceService.getProgress());
    }
  }

  public void unloadAssets() {
    logger.debug("UNLOADING ASSETS");
    ResourceService resourceService = ServiceLocator.getResourceService();
    resourceService.unloadAssets(config.textures.forestTextures);
    resourceService.unloadAssets(config.textures.forestTextureAtlases);
    resourceService.unloadAssets(config.sounds.gameSounds);
    resourceService.unloadAssets(config.sounds.gameMusic);
  }

  @Override
  public void dispose() {
    super.dispose();
    ServiceLocator.getResourceService().getAsset(config.sounds.backgroundMusic, Music.class).stop();
    this.unloadAssets();
  }

  public void playMusic() {
//    Music music = ServiceLocator.getResourceService().getAsset(BACKGROUND_MUSIC, Music.class);
//    music.setLooping(true);
//    music.setVolume(0.5f);
//    music.play();
    // Get the selected music track from the user settings
    UserSettings.Settings settings = UserSettings.get();
    String selectedTrack = settings.selectedMusicTrack;  // This will be "Track 1" or "Track 2"

    if (Objects.equals(selectedTrack, "Track 1")) {
      AudioManager.playMusic("sounds/BGM_03_mp3.mp3", true);
    } else if (Objects.equals(selectedTrack, "Track 2")) {
        AudioManager.playMusic("sounds/track_2.mp3", true);
    }
  }
  public void pauseMusic() {
//    Music music = ServiceLocator.getResourceService().getAsset(BACKGROUND_MUSIC, Music.class);
//    music.pause();
    AudioManager.stopMusic();  // Stop the music
  }

  public List<Entity> getEnemies() {
    return enemies;
  }
}
