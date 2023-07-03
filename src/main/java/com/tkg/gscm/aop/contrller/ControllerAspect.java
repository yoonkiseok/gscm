package com.tkg.gscm.aop.contrller;

import com.tkg.gscm.utils.ThreadLocalUtil;
import org.aspectj.lang.annotation.After;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

@Aspect
@Component
public class ControllerAspect {

    @After(value = "execution(* com.tkg.gscm.*.*Controller.*(..))")
    public void cleanThreadLocal() {
        ThreadLocalUtil.removeThreadLocalValue();
    }
}
