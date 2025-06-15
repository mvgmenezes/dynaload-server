package io.dynaload.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE) //Apenas classes, interfaces, enums
public @interface DynaloadExport {
    String value(); // exemplo: "v1/account"
    // Permite ao usuário declarar explicitamente outras classes a serem exportadas junto
    Class<?>[] includeDependencies() default {};
}
