package com.radziejewskig.todo.extension

import android.transition.*

data class SharedElementsOptions(
    val transitions: List<SharedTransition> = listOf(SharedTransition.CHANGE_BOUNDS),
    val duration: Long = 300,
    val pathMotion: PathMotion = PathMotion.LINEAR
)

enum class SharedTransition {
    CHANGE_BOUNDS,
    CHANGE_IMAGE_TRANSFORM,
    CHANGE_CLIP_BOUNDS,
    CHANGE_TRANSFORM,
    EXPLODE,
    FADE,
}

enum class PathMotion {
    LINEAR,
    ARC
}

fun SharedElementsOptions.toTransitionSet(): TransitionSet = TransitionSet().also { set ->
    this.transitions.map { it.toTransition() }.forEach { set.addTransition(it) }
    set.ordering = TransitionSet.ORDERING_TOGETHER
    set.duration = this.duration
    set.pathMotion = this.pathMotion.toSharedPathMotion()
}

fun SharedTransition.toTransition(): Transition = when(this) {
    SharedTransition.CHANGE_BOUNDS -> ChangeBounds()
    SharedTransition.CHANGE_IMAGE_TRANSFORM -> ChangeImageTransform()
    SharedTransition.CHANGE_CLIP_BOUNDS -> ChangeClipBounds()
    SharedTransition.CHANGE_TRANSFORM -> ChangeTransform()
    SharedTransition.EXPLODE -> Explode()
    SharedTransition.FADE -> Fade()
}

fun PathMotion.toSharedPathMotion(): android.transition.PathMotion? = when(this) {
    PathMotion.LINEAR -> null
    PathMotion.ARC -> ArcMotion()
}
