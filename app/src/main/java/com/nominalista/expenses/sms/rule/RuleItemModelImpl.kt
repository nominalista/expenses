package com.nominalista.expenses.sms.rule

import com.nominalista.expenses.data.model.Rule

class RuleItemModelImpl(val rule: Rule) : RuleItemModel {
    val name = rule.keywords

    var editClick: (() -> Unit)? = null
    var deleteClick: (() -> Unit)? = null
    var duplicateClick: (() -> Unit)? = null
    var shareClick: (() -> Unit)? = null
    var useClick: (() -> Unit)? = null
}
