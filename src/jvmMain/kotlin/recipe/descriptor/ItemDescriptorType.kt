package org.chorus_oss.chorus.recipe.descriptor


enum class ItemDescriptorType {
    INVALID,
    DEFAULT,
    MOLANG,
    ITEM_TAG,
    DEFERRED,

    /**
     * @since v575
     */
    COMPLEX_ALIAS
}
