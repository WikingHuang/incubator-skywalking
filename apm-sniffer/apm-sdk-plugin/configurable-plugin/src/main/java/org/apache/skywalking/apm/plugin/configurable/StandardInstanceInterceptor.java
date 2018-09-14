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

package org.apache.skywalking.apm.plugin.configurable;


import org.apache.skywalking.apm.agent.core.context.ContextManager;
import org.apache.skywalking.apm.agent.core.context.ContextSnapshot;
import org.apache.skywalking.apm.agent.core.context.trace.AbstractSpan;
import org.apache.skywalking.apm.agent.core.plugin.interceptor.enhance.EnhancedInstance;
import org.apache.skywalking.apm.agent.core.plugin.interceptor.enhance.InstanceMethodsAroundInterceptor;
import org.apache.skywalking.apm.agent.core.plugin.interceptor.enhance.MethodInterceptResult;
import org.apache.skywalking.apm.network.trace.component.ComponentsDefine;
import org.apache.skywalking.apm.plugin.configurable.util.ReflectUtils;

import java.lang.reflect.Method;

/**
 * Created by weijie.huang on 2018/9/12
 */
public class StandardInstanceInterceptor implements InstanceMethodsAroundInterceptor {

    @Override
    public void beforeMethod(EnhancedInstance objInst, Method method, Object[] allArguments, Class<?>[] argumentsTypes, MethodInterceptResult result) {
//        String field1 = "reqContent.requestId";
//        String field1Value = getInstanceValue(objInst, field1);
//        span.tag(field1, field1Value);
        AbstractSpan span;
        if (objInst.getClass().getSimpleName().equals("RequestContextImpl")
                && method.getName().equals("complete")) {
            String strMethod = ReflectUtils.reflect(objInst).field("request").field("method").field("value").toString();
            String strUri = ReflectUtils.reflect(objInst).field("request").field("uri").toString();
            String strHeaders = ReflectUtils.reflect(objInst).field("request").field("headers").toString();
            String strEntity = ReflectUtils.reflect(objInst).field("request").field("entity").toString();

            span = ContextManager.createLocalSpan(strMethod + " " + strUri);
            span.tag("method", strMethod);
            span.tag("uri", strUri);
            span.tag("headers", strHeaders);
            span.tag("entity", strEntity);
        } else {
            span = ContextManager.createLocalSpan(objInst.getClass().getSimpleName() + "/" + method.getName());
        }
        Object contextSnapshot = objInst.getSkyWalkingDynamicField();
        if (contextSnapshot != null) {
            ContextManager.continued((ContextSnapshot) contextSnapshot);
        }
        String app = System.getProperty("skywalking.agent.application_code");
        span.setComponent(ComponentsDefine.DUBBO);

        span.tag("class", objInst.getClass().getName());
        span.tag("operation", method.getName());
        span.tag("app", app);
    }

    public Object afterMethod(EnhancedInstance objInst, Method method, Object[] allArguments, Class<?>[] argumentsTypes, Object ret)
            throws Throwable {
//        if (objInst.getClass().getSimpleName().equals("RequestContextImpl")
//                && method.getName().equals("complete")) {
//
//            ContextManager.activeSpan().tag("ret", ret.toString());
//        }
        ContextManager.stopSpan();
        return ret;
    }

    public void handleMethodException(EnhancedInstance objInst, Method method, Object[] allArguments, Class<?>[] argumentsTypes, Throwable t) {
        dealException(t);
    }

    private void dealException(Throwable throwable) {
        AbstractSpan span = ContextManager.activeSpan();
        span.errorOccurred();
        span.log(throwable);
    }

    private String getInstanceValue(Object objInst, String fieldFullName) {
        try {
            String[] fieldArray = fieldFullName.split("\\.");
            ReflectUtils temp = ReflectUtils.reflect(objInst);
            for (String fieldName : fieldArray) {
                temp = temp.field(fieldName);
            }
            return temp.get().toString();
        } catch (Exception ex) {
            return ex.getMessage();
        }
    }
}
