package com.csse3200.game.components.npc;

import com.csse3200.game.components.Component;
import com.csse3200.game.rendering.AnimationRenderComponent;

/**
 * This class listens to events relevant to a chicken entity's state and plays the animation when one
 * of the events is triggered.
 */
public class ChickenAnimationController extends Component {
  AnimationRenderComponent animator;

  @Override
  public void create() {
    super.create();
    // Get the AnimationRenderComponent associated with the entity and store it in the animator field
    animator = this.entity.getComponent(AnimationRenderComponent.class);
    entity.getEvents().addListener("wanderLeft", this::animateWanderLeft);
    entity.getEvents().addListener("wanderRight", this::animateWanderRight);
    entity.getEvents().addListener("spawnStart", this::animateSpawn);
    entity.getEvents().addListener("chaseLeft", this::animateChaseLeft);
    entity.getEvents().addListener("chaseRight", this::animateChaseRight);
    entity.getEvents().addListener("alert", this::animateAlert);
  }

  private void animateSpawn() {
    animator.startAnimation("spawn");
  }

  private void animateAlert() {
    animator.startAnimation("alert");
  }
  private void animateChaseLeft() {
    animator.setFlipX(true);
    animator.startAnimation("walk");
  }

  private void animateChaseRight() {
    animator.setFlipX(false);
    animator.startAnimation("walk");
  }

  private void animateWanderLeft() {
    animator.setFlipX(true);
    animator.startAnimation("walk");
  }

  private void animateWanderRight() {
    animator.setFlipX(false);
    animator.startAnimation("walk");
  }
}
