package org.chorus_oss.chorus.entity.ai.route.finder.impl

import org.chorus_oss.chorus.entity.ai.route.data.Node
import org.chorus_oss.chorus.entity.ai.route.finder.IRouteFinder
import org.chorus_oss.chorus.entity.mob.EntityMob
import org.chorus_oss.chorus.math.Vector3
import java.util.*
import java.util.function.Predicate

class ConditionalAStarRouteFinder(
    private val entity: EntityMob,
    vararg routeFinders: Pair<Predicate<EntityMob>, IRouteFinder>
) :
    IRouteFinder {
    private val routeFinders: MutableMap<Predicate<EntityMob>, IRouteFinder> = mutableMapOf()

    init {
        routeFinders.forEach { pair ->
            this.routeFinders[pair.first] = pair.second
        }
    }

    val routeFinder: Optional<IRouteFinder>
        get() {
            for ((key, value) in routeFinders.entries) {
                if (key.test(this.entity)) {
                    return Optional.ofNullable(value)
                }
            }
            return Optional.empty()
        }

    override val isSearching: Boolean
        get() = routeFinder.map { obj: IRouteFinder -> obj.isSearching }.orElse(false)

    override val isFinished: Boolean
        get() = routeFinder.map { obj: IRouteFinder -> obj.isFinished }.orElse(false)

    override val isInterrupt: Boolean
        get() = routeFinder.map { obj: IRouteFinder -> obj.isInterrupt }.orElse(true)

    override val isReachable: Boolean
        get() = routeFinder.map { obj: IRouteFinder -> obj.isReachable }.orElse(false)

    override fun search(): Boolean {
        return routeFinder.map { obj: IRouteFinder -> obj.search() }.orElse(false)
    }

    override var start: Vector3?
        get() = routeFinder.map { obj: IRouteFinder -> obj.start }.orElse(Vector3.ZERO)
        set(vector3) {
            routeFinder.ifPresent { r: IRouteFinder -> r.start = vector3 }
        }

    override var target: Vector3?
        get() = routeFinder.map { obj: IRouteFinder -> obj.target }.orElse(Vector3.ZERO)
        set(vector3) {
            routeFinder.ifPresent { r: IRouteFinder -> r.target = vector3 }
        }

    override val reachableTarget: Vector3?
        get() = routeFinder.map { obj: IRouteFinder -> obj.reachableTarget }.orElse(Vector3.ZERO)

    override val route: List<Node>
        get() = routeFinder.map { obj: IRouteFinder -> obj.route }
            .orElse(listOf<Node>())

    override fun hasNext(): Boolean {
        return routeFinder.map { obj: IRouteFinder -> obj.hasNext() }.orElse(false)
    }

    override fun next(): Node? {
        return routeFinder.map { obj: IRouteFinder -> obj.next() }.orElse(null)
    }

    override fun hasCurrentNode(): Boolean {
        return routeFinder.map { obj: IRouteFinder -> obj.hasCurrentNode() }.orElse(false)
    }

    override val currentNode: Node?
        get() = routeFinder.map { obj: IRouteFinder -> obj.currentNode }.orElse(null)

    override var nodeIndex: Int
        get() = routeFinder.map { obj: IRouteFinder -> obj.nodeIndex }.orElse(0)
        set(index) {
            routeFinder.ifPresent { r: IRouteFinder ->
                r.nodeIndex =
                    index
            }
        }

    override fun getNode(index: Int): Node? {
        return routeFinder.map { r: IRouteFinder -> r.getNode(index) }.orElse(null)
    }
}
