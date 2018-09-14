package org.apache.skywalking.apm.plugin.configurable.util;

import net.bytebuddy.description.type.TypeDescription;
import net.bytebuddy.matcher.ElementMatcher;
import org.apache.skywalking.apm.agent.core.plugin.match.ClassMatch;
import org.apache.skywalking.apm.agent.core.plugin.match.IndirectMatch;
import org.apache.skywalking.apm.agent.core.plugin.match.NameMatch;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static net.bytebuddy.matcher.ElementMatchers.named;

/**
 * Created by weijie.huang on 2018/9/12
 */
public class MixMatch implements IndirectMatch {
    private List<ClassMatch> andMatchs;
    private List<ClassMatch> orMatchs;

    private MixMatch(ClassMatch... andMatchs) {
        if (andMatchs == null || andMatchs.length == 0) {
            throw new IllegalArgumentException("andMatches is null");
        }
        this.andMatchs = Arrays.asList(andMatchs);
    }

    public MixMatch orMatch(ClassMatch match) {
        if (orMatchs == null) {
            orMatchs = new ArrayList<ClassMatch>();
        }
        orMatchs.add(match);
        return this;
    }

    @Override
    public ElementMatcher.Junction buildJunction() {
        ElementMatcher.Junction junction = null;
        ElementMatcher.Junction temp = null;
        for (ClassMatch match : andMatchs) {
            if (match instanceof IndirectMatch) {
                temp = ((IndirectMatch) match).buildJunction();
            } else if (match instanceof NameMatch) {
                temp = named(((NameMatch) match).getClassName());
            } else {
                continue;
            }
            if (junction == null) {
                junction = temp;
            } else {
                junction = junction.and(temp);
            }
        }

        if (orMatchs != null && !orMatchs.isEmpty()) {
            for (ClassMatch match : orMatchs) {
                if (match instanceof IndirectMatch) {
                    temp = ((IndirectMatch) match).buildJunction();
                } else if (match instanceof NameMatch) {
                    temp = named(((NameMatch) match).getClassName());
                } else {
                    continue;
                }
                if (junction == null) {
                    junction = temp;
                } else {
                    junction = junction.or(temp);
                }
            }
        }
        return junction;
    }

    @Override
    public boolean isMatch(TypeDescription typeDescription) {
        if (orMatchs != null && !orMatchs.isEmpty()) {
            for (ClassMatch match : orMatchs) {
                if (match instanceof IndirectMatch) {
                    if (((IndirectMatch) match).isMatch(typeDescription)) {
                        return true;
                    }
                } else if (match instanceof NameMatch) {
                    Boolean isMatch = typeDescription.getActualName().equals(((NameMatch) match).getClassName())
                            && !typeDescription.isInterface();
                    if (isMatch) {
                        return true;
                    }
                }
            }
        }

        for (ClassMatch match : andMatchs) {
            if (match instanceof IndirectMatch) {
                if (!((IndirectMatch) match).isMatch(typeDescription)) {
                    return false;
                }
            } else if (match instanceof NameMatch) {
                Boolean isMatch = typeDescription.getActualName().equals(((NameMatch) match).getClassName())
                        && !typeDescription.isInterface();
                if (!isMatch) {
                    return false;
                }
            }
        }
        return true;
    }

    public static MixMatch getMatch(ClassMatch... andMatchs) {
        return new MixMatch(andMatchs);
    }
}
