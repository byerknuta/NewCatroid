package org.catrobat.catroid.content

object UserVarsManager {
    private val variables = LinkedHashMap<String, String>()

    @Synchronized
    fun setVar(name: String, value: String) {
        variables[name] = value
    }

    @Synchronized
    fun getVar(name: String): String? {
        return variables[name]
    }

    @Synchronized
    fun clearVars() {
        variables.clear()
    }

    @Synchronized
    fun delVar(name: String) {
        variables.remove(name)
    }

    @Synchronized
    fun getVarName(id: Int): String? {
        if (id < 0 || id >= variables.size) return null
        return variables.keys.elementAtOrNull(id)
    }

    @Synchronized
    fun getVarValue(id: Int): String? {
        val key = getVarName(id) ?: return null
        return variables[key]
    }
}
