package org.apache.skywalking.apm.plugin.configurable.util;

import net.bytebuddy.description.type.TypeDescription;
import net.bytebuddy.description.type.TypeList;
import net.bytebuddy.matcher.ElementMatcher;
import net.bytebuddy.matcher.ElementMatchers;
import org.apache.skywalking.apm.agent.core.plugin.match.ClassMatch;
import org.apache.skywalking.apm.agent.core.plugin.match.IndirectMatch;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static net.bytebuddy.matcher.ElementMatchers.*;

/**
 * Created by weijie.huang on 2018/9/12
 */
public class HierarchyAndNameMatch implements IndirectMatch {
    private String[] parentTypes;
    private String[] classNameContains;

    private HierarchyAndNameMatch(String[] parentTypes, String[] classNameContains) {
        if (parentTypes == null || parentTypes.length == 0) {
            throw new IllegalArgumentException("parentTypes is null");
        }
//        if ((classNameContains == null) || (classNameContains.length == 0)) {
//            throw new IllegalArgumentException("classNameContains is null, you can use HierarchyMatch");
//        }
        this.parentTypes = parentTypes;
        this.classNameContains = classNameContains;
    }

    @Override
    public ElementMatcher.Junction buildJunction() {
        ElementMatcher.Junction junctionHierarchyMatch = null;
        ElementMatcher.Junction junctionNameMatch = null;
        for (String superTypeName : this.parentTypes) {
            if (junctionHierarchyMatch == null) {
                junctionHierarchyMatch = buildSuperClassMatcher(superTypeName);
            } else {
                junctionHierarchyMatch = junctionHierarchyMatch.and(buildSuperClassMatcher(superTypeName));
            }
        }
        if (classNameContains != null && classNameContains.length > 0) {
            for (String name : this.classNameContains) {
                if (junctionNameMatch == null) {
                    junctionNameMatch = ElementMatchers.nameContains(name);
                } else {
                    junctionNameMatch = junctionNameMatch.or(ElementMatchers.nameContains(name));
                }
            }
        }

        if (junctionNameMatch != null) {
            return junctionHierarchyMatch.and(junctionNameMatch).and(ElementMatchers.not(ElementMatchers.isInterface()));
        } else {
            return junctionHierarchyMatch.and(ElementMatchers.not(ElementMatchers.isInterface()));

        }
    }

    private ElementMatcher.Junction buildSuperClassMatcher(String superTypeName) {
        return hasSuperType(named(superTypeName));
    }

    @Override
    public boolean isMatch(TypeDescription typeDescription) {
        List<String> parentTypes = new ArrayList<String>(Arrays.asList(this.parentTypes));

        TypeList.Generic implInterfaces = typeDescription.getInterfaces();
        for (TypeDescription.Generic implInterface : implInterfaces) {
            matchHierarchyClass(implInterface, parentTypes);
        }

        if (typeDescription.getSuperClass() != null) {
            matchHierarchyClass(typeDescription.getSuperClass(), parentTypes);
        }

        if (parentTypes.size() == 0) {
            return matchNameContains(typeDescription.getActualName(), this.classNameContains);
        }

        return false;
    }

    private void matchHierarchyClass(TypeDescription.Generic clazz, List<String> parentTypes) {
        parentTypes.remove(clazz.asRawType().getTypeName());
        if (parentTypes.size() == 0) {
            return;
        }

        for (TypeDescription.Generic generic : clazz.getInterfaces()) {
            matchHierarchyClass(generic, parentTypes);
        }

        TypeDescription.Generic superClazz = clazz.getSuperClass();
        if (superClazz != null && !clazz.getTypeName().equals("java.lang.Object")) {
            matchHierarchyClass(superClazz, parentTypes);
        }

    }

    private boolean matchNameContains(String className, String[] classNameContains) {
        if (classNameContains.length == 0) {
            return true;
        }
        for (String name : classNameContains) {
            if (className.contains(name)) {
                return true;
            }
        }
        return false;
    }

    public static ClassMatch match(String[] parentTypes, String[] classNameContains) {
        return new HierarchyAndNameMatch(parentTypes, classNameContains);
    }
}
