package metasync.site.laravelroute.references

import com.intellij.openapi.project.Project
import com.intellij.openapi.roots.ProjectRootManager
import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiReferenceBase
import com.intellij.psi.util.CachedValueProvider
import com.intellij.psi.util.CachedValuesManager
import com.intellij.psi.util.PsiModificationTracker
import com.jetbrains.php.lang.psi.elements.StringLiteralExpression
import metasync.site.laravelroute.models.LaravelRoute
import metasync.site.laravelroute.services.LaravelRouteService

class LaravelRouteReference(element: StringLiteralExpression) :
    PsiReferenceBase<StringLiteralExpression>(
        element,
        TextRange(1, element.textLength - 1)
    ) {

    data class RouteCache(
        val byName: Map<String, LaravelRoute>
    )

    override fun resolve(): PsiElement? {
        val routeName = getRouteName()?.takeIf { it.isNotEmpty() } ?: return null

        return getCachedRoutes(element.project)
            .byName[routeName]
            ?.psiElement
    }

    override fun getVariants(): Array<Any> {
        return getCachedRoutes(element.project)
            .byName
            .keys
            .toTypedArray()
    }

    private fun getCachedRoutes(project: Project): RouteCache {
        return CachedValuesManager.getManager(project).getCachedValue(project) {
            val routes = LaravelRouteService.getAllRoutes(project)

            CachedValueProvider.Result.create(
                RouteCache(
                    routes
                        .mapNotNull { route ->
                            route.name?.let { name -> name to route }
                        }
                        .toMap()
                ),
                PsiModificationTracker.MODIFICATION_COUNT,
                ProjectRootManager.getInstance(project)
            )
        }
    }

    private fun getRouteName(): String? = element.contents
}
