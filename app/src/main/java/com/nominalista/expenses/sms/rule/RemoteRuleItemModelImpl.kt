package com.nominalista.expenses.sms.rule

import com.nominalista.expenses.data.model.Rule

class RemoteRuleItemModelImpl(val rule: Rule, var useClick: (() -> Unit)? = null) : RuleItemModel {
    val name = rule.keywords
}
