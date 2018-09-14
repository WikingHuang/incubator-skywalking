package org.apache.skywalking.apm.plugin.configurable.util;

import net.bytebuddy.description.type.TypeDescription;
import net.bytebuddy.matcher.ElementMatcher;
import net.bytebuddy.matcher.ElementMatchers;
import org.apache.skywalking.apm.agent.core.plugin.match.ClassMatch;
import org.apache.skywalking.apm.agent.core.plugin.match.IndirectMatch;

/**
 * Created by weijie.huang on 2018/9/12
 */
public class NameContainsMatch implements IndirectMatch {
    private String[] classNameContains;

    private NameContainsMatch(String[] parentTypes, String[] classNameContains) {
        if ((classNameContains == null) || (classNameContains.length == 0)) {
            throw new IllegalArgumentException("classNameContains is null");
        }
        this.classNameContains = classNameContains;
    }

    @Override
    public ElementMatcher.Junction buildJunction() {
        ElementMatcher.Junction junction = null;
        for (String name : this.classNameContains) {
            if (junction == null) {
                junction = ElementMatchers.nameContains(name);
            } else {
                junction = junction.and(ElementMatchers.nameContains(name));
            }
        }

        return junction.and(ElementMatchers.not(ElementMatchers.isInterface()));
    }

    @Override
    public boolean isMatch(TypeDescription typeDescription) {
        return matchNameContains(typeDescription.getActualName(), this.classNameContains);
    }

    private boolean matchNameContains(String className, String[] classNameContains) {
        if (classNameContains.length == 0) {
            return true;
        }
        Boolean result = true;
        for (String name : classNameContains) {
            if (!className.contains(name)) {
                result = false;
            }
        }
        return result;
    }

    public static ClassMatch match(String[] parentTypes, String[] classNameContains) {
        return new NameContainsMatch(parentTypes, classNameContains);
    }
}
