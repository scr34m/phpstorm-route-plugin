package metasync.site.laravelroute.services

import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.PsiManager
import com.intellij.psi.search.FilenameIndex
import com.intellij.psi.search.GlobalSearchScope
import com.intellij.psi.util.PsiTreeUtil
import com.jetbrains.php.lang.psi.PhpFile
import com.jetbrains.php.lang.psi.elements.*
import metasync.site.laravelroute.models.LaravelRoute

object LaravelRouteService {

    private val ROUTE_FILES = arrayOf("routes.php")
    private val ROUTE_METHODS = setOf("get", "post", "any")

    fun getAllRoutes(project: Project): List<LaravelRoute> {
        val routes = mutableListOf<LaravelRoute>()

        // Find route files
        ROUTE_FILES.forEach { filename ->
            val files = FilenameIndex.getVirtualFilesByName(
                filename,
                GlobalSearchScope.projectScope(project)
            )

            files.forEach { file ->
                val psiFile = PsiManager.getInstance(project).findFile(file)
                if (psiFile is PhpFile) {
                    routes.addAll(parseRoutesFromFile(psiFile))
                }
            }
        }

        return routes
    }

    private fun parseRoutesFromFile(file: PhpFile): List<LaravelRoute> {
        val routes = mutableListOf<LaravelRoute>()

        // Find all method references that could be Route calls
        val methodRefs = PsiTreeUtil.findChildrenOfType(file, MethodReference::class.java)

        methodRefs.forEach { methodRef ->
            parseRouteFromMethodRef(methodRef)?.let { route ->
                routes.add(route)
            }
        }

        return routes
    }

    private fun parseRouteFromMethodRef(methodRef: MethodReference): LaravelRoute? {
        // Check if this is a Route method call
        val classRef = methodRef.classReference
        if (classRef !is ClassReference) return null

        val className = classRef.name
        if (className != "Route") return null

        val methodName = methodRef.name
        if (methodName !in ROUTE_METHODS) return null

        // Extract route name if present
        val routeName = extractRouteName(methodRef) ?: return null

        return LaravelRoute(routeName, methodRef, methodName)
    }

    private fun extractRouteName(methodRef: MethodReference): String? {
        val names = mutableListOf<String>()

        // first parameter is the name
        if (methodRef.parameterList != null && methodRef.parameters.isNotEmpty()) {
            names.add((methodRef.parameters[0] as StringLiteralExpression).contents)
        }

        // walk backward if this is a group
        var parent = methodRef.parent
        while (parent != null) {
            if (parent is MethodReference && parent.name == "group") {
                val parentName = extractGroupAs(parent.parameterList);
                if (parentName != null) {
                    names.add(parentName);
                }
            }
            parent = parent.parent
        }

        return names.reversed().joinToString(separator = ".")
    }

    private fun extractGroupAs(params: ParameterList?): String? {
        if (params == null || params.parameters.isEmpty()) {
            return null
        }
        for (param in params.parameters) {
            if (param is ArrayCreationExpression) {
                for (arrayElement in param.hashElements) {
                    val key = arrayElement.key as? StringLiteralExpression ?: continue
                    if (key.contents == "as") {
                        return (arrayElement.value as? StringLiteralExpression)?.contents
                    }
                }
            }
        }
        return null
    }
}
