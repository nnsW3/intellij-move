package org.move.lang.core.psi

import com.intellij.psi.NavigatablePsiElement
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiNamedElement
import org.move.lang.MvElementTypes
import org.move.lang.core.psi.ext.address
import org.move.lang.core.psi.ext.findLastChildByType

interface MvNamedElement : MvElement,
                           PsiNamedElement,
                           NavigatablePsiElement {
    val nameElement: PsiElement?
        get() = findLastChildByType(MvElementTypes.IDENTIFIER)
}

interface MvQualifiedNamedElement: MvNamedElement

data class FqPath(val address: String, val module: String, val item: String?) {
    override fun toString(): String {
        return if (item == null) {
            "$address::$module"
        } else {
            "$address::$module::$item"
        }
    }
}

val MvQualifiedNamedElement.fqPath: FqPath? get() {
    return when (this) {
        is MvModuleDef -> {
            val address = this.address()?.text ?: return null
            val moduleName = this.name ?: return null
            FqPath(address, moduleName, null)
        }
        else -> {
            val module = this.containingModule ?: return null
            val address = module.address()?.text ?: return null
            val moduleName = module.name ?: return null
            val elementName = this.name ?: return null
            FqPath(address, moduleName, elementName)
        }
    }
}
