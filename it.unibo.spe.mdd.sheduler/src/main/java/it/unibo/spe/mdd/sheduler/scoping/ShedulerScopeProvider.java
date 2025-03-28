/*
 * generated by Xtext 2.32.0
 */
package it.unibo.spe.mdd.sheduler.scoping;


import it.unibo.spe.mdd.sheduler.sheduler.ShedulerPackage;
import it.unibo.spe.mdd.sheduler.sheduler.Task;
import it.unibo.spe.mdd.sheduler.sheduler.TaskPool;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EReference;
import org.eclipse.xtext.EcoreUtil2;
import org.eclipse.xtext.scoping.IScope;
import org.eclipse.xtext.scoping.Scopes;

import java.util.Set;

/**
 * This class contains custom scoping description.
 * 
 * See https://www.eclipse.org/Xtext/documentation/303_runtime_concepts.html#scoping
 * on how and when to use it.
 */
public class ShedulerScopeProvider extends AbstractShedulerScopeProvider {

    @Override
    public IScope getScope(EObject context, EReference reference) {
        if (context instanceof Task) {
            if (Set.of(ShedulerPackage.Literals.TASK__AFTER, ShedulerPackage.Literals.TASK__BEFORE).contains(reference)) {
                TaskPool pool = EcoreUtil2.getContainerOfType(context, TaskPool.class);
                return Scopes.scopeFor(
                        pool.getTasks().stream()
                                .filter(it -> !context.equals(it))
                                .filter(it -> it.getName() != null)
                                .toList()
                );
            }
        }
        return super.getScope(context, reference);
    }
}
