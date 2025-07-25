package org.chorus_oss.chorus.item


class ItemEchoShard @JvmOverloads constructor(meta: Int = 0, count: Int = 1) :
    Item(ItemID.Companion.ECHO_SHARD, meta, count, "Echo Shard")
