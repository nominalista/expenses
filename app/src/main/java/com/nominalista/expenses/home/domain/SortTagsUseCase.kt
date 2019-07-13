package com.nominalista.expenses.home.domain

import com.nominalista.expenses.data.Tag

class SortTagsUseCase {
    operator fun invoke(tags: List<Tag>): List<Tag> {
        return tags.sortedBy { it.name }
    }
}