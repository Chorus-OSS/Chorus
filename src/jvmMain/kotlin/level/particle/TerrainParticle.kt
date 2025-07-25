package org.chorus_oss.chorus.level.particle

import org.chorus_oss.chorus.block.Block
import org.chorus_oss.chorus.math.Vector3

class TerrainParticle(pos: Vector3, block: Block) : GenericParticle(pos, TYPE_TERRAIN, block.runtimeId)
