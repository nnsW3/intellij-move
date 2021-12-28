package org.move.lang.core.psi

import org.move.lang.core.psi.ext.definedAddressRef
import org.move.lang.core.psi.ext.fqName
import org.move.lang.core.psi.ext.toAddress

interface MvImportStatementsOwner : MvElement {
    val importStatements: List<MvImportStatement>

    @JvmDefault
    private fun _moduleImports(): List<MvModuleImport> =
        importStatements.mapNotNull { it.moduleImport }

    @JvmDefault
    fun moduleImports(): List<MvModuleImport> =
        _moduleImports()
            .filter { it.importAlias == null }

    @JvmDefault
    fun selfItemImports(): List<MvItemImport> =
        itemImports()
            .filter { it.importAlias == null && it.text == "Self" }
//            .map { it.parent }
//            .filterIsInstance<MvModuleItemsImport>()
//            .map { it.fqModuleRef }

    @JvmDefault
    fun moduleImportAliases(): List<MvImportAlias> =
        _moduleImports().mapNotNull { it.importAlias }

    @JvmDefault
    private fun itemImports(): List<MvItemImport> =
        importStatements
            .mapNotNull { it.moduleItemsImport }
            .flatMap {
                val item = it.itemImport
                if (item != null) {
                    listOf(item)
                } else
                    it.multiItemImport?.itemImportList.orEmpty()
            }

    @JvmDefault
    fun itemImportsWithoutAliases(): List<MvItemImport> =
        itemImports().filter { it.importAlias == null }

    @JvmDefault
    fun itemImportsAliases(): List<MvImportAlias> =
        itemImports().mapNotNull { it.importAlias }
}

fun MvImportStatementsOwner.shortestPathIdentText(item: MvNamedElement): String? {
    val itemName = item.name ?: return null
    // local
    if (this == item.containingImportsOwner) return itemName

    for (itemImport in this.itemImportsWithoutAliases()) {
        val importedItem = itemImport.reference.resolve() ?: continue
        if (importedItem == item) {
            return itemName
        }
    }
    val module = item.containingModule ?: return null
    val moduleName = module.name ?: return null
    for (moduleImport in this.moduleImports()) {
        val importedModule = moduleImport.fqModuleRef?.reference?.resolve() ?: continue
        if (importedModule == module) {
            return "$moduleName::$itemName"
        }
    }
    val addressName = module.definedAddressRef()?.text ?: return null
    return "$addressName::$moduleName::$itemName"
}
