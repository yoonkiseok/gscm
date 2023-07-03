package com.tkg.gscm.aop.exception;


import com.tkg.gscm.utils.ThreadLocalUtil;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.springframework.stereotype.Component;

import java.util.Map;

@Aspect
@Component
public class ExceptionHandlerAspect {

    @Before(value = "execution(* com.tkg.gscm.exception.GlobalExceptionHandler.*(..))")
    public void removeThreadLocalValue(JoinPoint joinPoint) {
        Object value = ThreadLocalUtil.getThreadLocalValue();
        if (value != null) {
            if (value instanceof Map) {
                Map<?, ?> mapValue = (Map<?, ?>) value;
                System.out.println("ThreadLocal Value (Map):");
                for (Map.Entry<?, ?> entry : mapValue.entrySet()) {
                    System.out.println("(ExceptionHandlerAspect)" + entry.getKey() + " : " + entry.getValue());
                }
            } else {
                System.out.println("(ExceptionHandlerAspect)" + "Value: " + value);
            }
        }

        ThreadLocalUtil.removeThreadLocalValue();
    }
}
