package com.csse3200.game.areas;

import com.badlogic.gdx.math.GridPoint2;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Disposable;
import com.csse3200.game.areas.terrain.TerrainComponent;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.services.ServiceLocator;
import com.csse3200.game.utils.math.RandomUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents an area in the game, such as a level, indoor area, etc. An area has a terrain and
 * other entities to spawn on that terrain.
 *
 * <p>Support for enabling/disabling game areas could be added by making this a Component instead.
 */
public abstract class GameArea implements Disposable {
  protected TerrainComponent terrain;
  protected List<Entity> areaEntities;

  protected GameArea() {
    areaEntities = new ArrayList<>();
  }

  /**
   * Create the game area in the world.
   */
  public abstract void create();

  /**
   * Dispose of all internal entities in the area
   */
  public void dispose() {
    for (Entity entity : areaEntities) {
      entity.dispose();
    }
  }

  /**
   * Spawn entity at its current position
   *
   * @param entity Entity (not yet registered)
   */
  protected void spawnEntity(Entity entity) {
    areaEntities.add(entity);
    ServiceLocator.getEntityService().register(entity);
  }

  /**
   * Spawn entity on a given tile. Requires the terrain to be set first.
   *
   * @param entity  Entity (not yet registered)
   * @param tilePos tile position to spawn at
   * @param centerX true to center entity X on the tile, false to align the bottom left corner
   * @param centerY true to center entity Y on the tile, false to align the bottom left corner
   */
  protected void spawnEntityAt(
          Entity entity, GridPoint2 tilePos, boolean centerX, boolean centerY) {
    Vector2 worldPos = terrain.tileToWorldPosition(tilePos);
    float tileSize = terrain.getTileSize();

    if (centerX) {
      worldPos.x += (tileSize / 2) - entity.getCenterPosition().x;
    }
    if (centerY) {
      worldPos.y += (tileSize / 2) - entity.getCenterPosition().y;
    }

    entity.setPosition(worldPos);
    spawnEntity(entity);
  }

  public abstract Entity getPlayer();
  public abstract void unloadAssets();

  public abstract void pauseMusic();

  public abstract void playMusic();

  public abstract List<Entity> getEnemies();

  /**
   * Spawns an entity at a specified world position.
   *
   * <p>This method sets the position of the given entity to the specified coordinates in the world
   * and then registers the entity into the game world. The entity should not be registered prior to
   * calling this method.
   *
   * @param entity  The entity to be spawned (not yet registered).
   * @param worldPos The world position where the entity should be placed.
   */
  public void spawnEntityAtVector(Entity entity, Vector2 worldPos) {
    entity.setPosition(worldPos);
    spawnEntity(entity);
  }

}
