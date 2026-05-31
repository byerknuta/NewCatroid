package org.catrobat.catroid.content.actions

import org.catrobat.catroid.content.Scope
import org.catrobat.catroid.content.eventids.UserDefinedBrickEventId
import org.catrobat.catroid.formulaeditor.UserVariable
import org.catrobat.catroid.userbrick.UserDefinedBrickInput
import java.util.UUID

class UserDefinedBrickAction : SingleSpriteEventAction() {
    var scope: Scope? = null
        set(scope) {
            field = scope
            super.sprite = scope?.sprite
        }

    var userDefinedBrickID: UUID? = null
    var userDefinedBrickInputs: MutableList<UserDefinedBrickInput>? = null

    fun setInputs(inputs: MutableList<UserDefinedBrickInput>?) {
        this.userDefinedBrickInputs = mutableListOf()
        inputs?.forEach {
            this.userDefinedBrickInputs?.add(UserDefinedBrickInput(it))
        }
    }

    private fun getInterpretedInputs(): MutableList<Any> {
        val interpretedInputs = mutableListOf<Any>()
        userDefinedBrickInputs?.forEach {
            val param = it.value.interpretObject(scope)
            interpretedInputs.add(UserVariable(it.name, param))
        }
        return interpretedInputs
    }

    override fun getEventId() =
        userDefinedBrickID?.let { id ->
            UserDefinedBrickEventId(id, getInterpretedInputs())
        }
}
