/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package org.apache.skywalking.apm.plugin.configurable.define;

import net.bytebuddy.description.method.MethodDescription;
import net.bytebuddy.matcher.ElementMatcher;
import net.bytebuddy.matcher.ElementMatchers;
import org.apache.skywalking.apm.agent.core.plugin.interceptor.ConstructorInterceptPoint;
import org.apache.skywalking.apm.agent.core.plugin.interceptor.InstanceMethodsInterceptPoint;
import org.apache.skywalking.apm.agent.core.plugin.interceptor.StaticMethodsInterceptPoint;
import org.apache.skywalking.apm.agent.core.plugin.interceptor.enhance.ClassInstanceMethodsEnhancePluginDefine;
import org.apache.skywalking.apm.agent.core.plugin.match.ClassMatch;
import org.apache.skywalking.apm.agent.core.plugin.match.HierarchyMatch;

import java.util.Arrays;
import java.util.List;

/**
 * Created by weijie.huang on 2018/9/12
 */
public class StandardInstrumentation extends ClassInstanceMethodsEnhancePluginDefine {

    private static final String ENHANCE_CLASS = "java.lang.Runnable";
    private static final String ENHANCE_CLASS2 = "s.com.eoi.common.UserHelp$";
    private static final String[] ENHANCE_CLASS3 = {"s.com.eoi.common.ServiceCommon"};
    private static final String INTERCEPT_CLASS = "org.apache.skywalking.apm.plugin.configurable.StandardInstanceInterceptor";
    private static final String STATIC_INTERCEPT_CLASS = "org.apache.skywalking.apm.plugin.configurable.StandardStaticInterceptor";
    private static final List<String> METHOD_LIST = Arrays.asList(new String[]{"login", "verifyLogin", "setSession"});
    public static final String INTERCEPT_GET_SKYWALKING_DYNAMIC_FIELD_METHOD = "getSkyWalkingDynamicField";
    public static final String INTERCEPT_SET_SKYWALKING_DYNAMIC_FEILD_METHOD = "setSkyWalkingDynamicField";

    @Override
    protected ClassMatch enhanceClass() {
        return HierarchyMatch.byHierarchyMatch(ENHANCE_CLASS3);
    }

    @Override
    protected ConstructorInterceptPoint[] getConstructorsInterceptPoints() {
        return null;
    }

    @Override
    protected InstanceMethodsInterceptPoint[] getInstanceMethodsInterceptPoints() {
        return new InstanceMethodsInterceptPoint[]{
            new InstanceMethodsInterceptPoint() {
                @Override
                public ElementMatcher<MethodDescription> getMethodsMatcher() {
                    return ElementMatchers.not(ElementMatchers.isDeclaredBy(Object.class))
                            .and(ElementMatchers.not(ElementMatchers.isConstructor()))
                            .and(ElementMatchers.not(ElementMatchers.named(INTERCEPT_GET_SKYWALKING_DYNAMIC_FIELD_METHOD)))
                            .and(ElementMatchers.not(ElementMatchers.named(INTERCEPT_SET_SKYWALKING_DYNAMIC_FEILD_METHOD)))
                            .and(ElementMatchers.not(ElementMatchers.named("driver")));
                }

                @Override
                public String getMethodsInterceptor() {
                    return INTERCEPT_CLASS;
                }

                @Override
                public boolean isOverrideArgs() {
                    return false;
                }
            }
        };
    }

    @Override
    protected StaticMethodsInterceptPoint[] getStaticMethodsInterceptPoints() {
        return new StaticMethodsInterceptPoint[]{
            new StaticMethodsInterceptPoint() {
                @Override
                public ElementMatcher<MethodDescription> getMethodsMatcher() {
                    return ElementMatchers.not(ElementMatchers.isDeclaredBy(Object.class))
                            .and(ElementMatchers.not(ElementMatchers.isConstructor()))
                            .and(ElementMatchers.not(ElementMatchers.named(INTERCEPT_GET_SKYWALKING_DYNAMIC_FIELD_METHOD)))
                            .and(ElementMatchers.not(ElementMatchers.named(INTERCEPT_SET_SKYWALKING_DYNAMIC_FEILD_METHOD)))
                            .and(ElementMatchers.not(ElementMatchers.named("driver")));
                }

                @Override
                public String getMethodsInterceptor() {
                    return STATIC_INTERCEPT_CLASS;
                }

                @Override
                public boolean isOverrideArgs() {
                    return false;
                }
            }
        };
    }
}
